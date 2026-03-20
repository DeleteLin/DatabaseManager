package space.xiaoxiao.databasemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.util.Properties

/**
 * JDBC 执行上下文：负责 JDBC 连接生命周期与当前 database/schema 状态。
 */
class JdbcExecutionContext(
    override val config: DatabaseConfig
) : DbExecutionContext {

    internal var connection: Connection? = null

    override val databaseType: DatabaseType
        get() = config.type

    override var currentSchema: String? = config.schema

    override var currentDatabase: String? = config.database

    override suspend fun connect(): ConnectionStatus = withContext(Dispatchers.IO) {
        try {
            // 如果已连接且有效，直接返回
            connection?.let { conn ->
                if (!conn.isClosed) {
                    return@withContext ConnectionStatus.Connected
                }
            }

            val jdbcUrl = when (config.type) {
                DatabaseType.MYSQL -> {
                    val baseUrl = "jdbc:mysql://${config.host}:${config.port}/${config.database}"
                    val params = listOf(
                        "useSSL=false",
                        "allowPublicKeyRetrieval=true",
                        "serverTimezone=UTC"
                    ).joinToString("&")
                    "$baseUrl?$params"
                }

                DatabaseType.POSTGRESQL ->
                    "jdbc:postgresql://${config.host}:${config.port}/${config.database}"

                else -> return@withContext ConnectionStatus.Error(
                    "JDBC 不支持的数据库类型：${config.type}",
                    null
                )
            }

            connection = JdbcConnectionFactory.openConnection(config, jdbcUrl)
            currentDatabase = config.database
            ConnectionStatus.Connected
        } catch (e: Exception) {
            e.printStackTrace()
            val reason = e.message ?: e.javaClass.name
            ConnectionStatus.Error("连接失败（${e.javaClass.simpleName}）：$reason", e)
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                connection?.close()
            } catch (_: Exception) {
            } finally {
                connection = null
            }
        }
    }

    override fun isConnected(): Boolean = connection?.isClosed == false
}

/**
 * JDBC 连接工厂（避免使用 Class.forName 反射驱动加载）
 */
internal object JdbcConnectionFactory {
    fun openConnection(config: DatabaseConfig, jdbcUrl: String = buildJdbcUrl(config)): Connection {
        val props = Properties().apply {
            put("user", config.username)
            put("password", config.password)
        }
        return when (config.type) {
            DatabaseType.MYSQL -> {
                val driver = com.mysql.cj.jdbc.Driver()
                driver.connect(jdbcUrl, props)
                    ?: error("MySQL 驱动返回空连接，url=$jdbcUrl")
            }
            DatabaseType.POSTGRESQL -> {
                val driver = org.postgresql.Driver()
                driver.connect(jdbcUrl, props)
                    ?: error("PostgreSQL 驱动返回空连接，url=$jdbcUrl")
            }
            else -> error("JDBC 不支持的数据库类型：${config.type}")
        }
    }

    fun buildJdbcUrl(config: DatabaseConfig): String {
        return when (config.type) {
            DatabaseType.MYSQL -> {
                val baseUrl = "jdbc:mysql://${config.host}:${config.port}/${config.database}"
                val params = listOf(
                    "useSSL=false",
                    "allowPublicKeyRetrieval=true",
                    "serverTimezone=UTC"
                ).joinToString("&")
                "$baseUrl?$params"
            }
            DatabaseType.POSTGRESQL -> "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
            else -> error("JDBC 不支持的数据库类型：${config.type}")
        }
    }
}

/**
 * JDBC 无状态执行器：基于给定的 JdbcExecutionContext 进行命令执行。
 */
class JdbcCommandExecutor(
    override val databaseType: DatabaseType
) : CommandExecutor {

    private fun requireConnection(ctx: DbExecutionContext): Connection {
        val jdbcCtx = ctx as? JdbcExecutionContext
            ?: throw IllegalArgumentException("DbExecutionContext 必须是 JdbcExecutionContext")
        return jdbcCtx.connection
            ?: throw IllegalStateException("未连接到数据库")
    }

    private fun bindParams(ps: PreparedStatement, params: List<DbParam>) {
        params.forEachIndexed { idx, param ->
            val i = idx + 1
            when (param) {
                DbParam.Null -> ps.setObject(i, null)
                is DbParam.Str -> ps.setString(i, param.value)
                is DbParam.Int32 -> ps.setInt(i, param.value)
                is DbParam.Int64 -> ps.setLong(i, param.value)
                is DbParam.Float64 -> ps.setDouble(i, param.value)
                is DbParam.Bool -> ps.setBoolean(i, param.value)
                is DbParam.Bytes -> ps.setBytes(i, param.value)
            }
        }
    }

    override suspend fun executeQuery(
        ctx: DbExecutionContext,
        channel: DbExecutionChannel
    ): Result<QueryResult> = withContext(Dispatchers.IO) {
        val conn = try {
            requireConnection(ctx)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
        try {
            val startTime = System.currentTimeMillis()
            val (statement, resultSet) = when (val cmd = channel.command) {
                is DbCommand.RawSql -> {
                    val st = conn.createStatement()
                    st to st.executeQuery(cmd.sql)
                }
                is DbCommand.PreparedSql -> {
                    val ps = conn.prepareStatement(cmd.sql)
                    bindParams(ps, cmd.params)
                    ps to ps.executeQuery()
                }
                is DbCommand.RedisArgv -> throw IllegalArgumentException("JDBC 不支持 Redis 命令")
            }
            val columns = mutableListOf<Column>()
            val rows = mutableListOf<Row>()
            val metaData = resultSet.metaData
            val columnCount = metaData.columnCount
            for (i in 1..columnCount) {
                columns.add(
                    Column(
                        name = metaData.getColumnName(i),
                        typeName = metaData.getColumnTypeName(i),
                        isNullable = metaData.isNullable(i) != java.sql.DatabaseMetaData.columnNoNulls
                    )
                )
            }
            while (resultSet.next()) {
                val values = mutableListOf<Any?>()
                for (i in 1..columnCount) {
                    values.add(resultSet.getObject(i))
                }
                rows.add(Row(values))
            }
            val executionTime = System.currentTimeMillis() - startTime
            resultSet.close()
            statement.close()
            Result.success(QueryResult(columns, rows, rows.size, executionTime))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun executeUpdate(
        ctx: DbExecutionContext,
        channel: DbExecutionChannel
    ): Result<UpdateResult> = withContext(Dispatchers.IO) {
        val conn = try {
            requireConnection(ctx)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
        try {
            val startTime = System.currentTimeMillis()
            val (statement, affectedRows) = when (val cmd = channel.command) {
                is DbCommand.RawSql -> {
                    val st = conn.createStatement()
                    st to st.executeUpdate(cmd.sql)
                }
                is DbCommand.PreparedSql -> {
                    val ps = conn.prepareStatement(cmd.sql)
                    bindParams(ps, cmd.params)
                    ps to ps.executeUpdate()
                }
                is DbCommand.RedisArgv -> throw IllegalArgumentException("JDBC 不支持 Redis 命令")
            }
            val executionTime = System.currentTimeMillis() - startTime
            statement.close()
            Result.success(UpdateResult(affectedRows, executionTime))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun execute(
        ctx: DbExecutionContext,
        channel: DbExecutionChannel
    ): Result<ExecutionResult> = withContext(Dispatchers.IO) {
        val conn = try {
            requireConnection(ctx)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
        try {
            val startTime = System.currentTimeMillis()
            val (statement, hasResultSet) = when (val cmd = channel.command) {
                is DbCommand.RawSql -> {
                    val st = conn.createStatement()
                    st to st.execute(cmd.sql)
                }
                is DbCommand.PreparedSql -> {
                    val ps = conn.prepareStatement(cmd.sql)
                    bindParams(ps, cmd.params)
                    ps to ps.execute()
                }
                is DbCommand.RedisArgv -> throw IllegalArgumentException("JDBC 不支持 Redis 命令")
            }
            val executionTime = System.currentTimeMillis() - startTime

            if (hasResultSet) {
                val resultSet = statement.resultSet
                val columns = mutableListOf<Column>()
                val rows = mutableListOf<Row>()
                val metaData = resultSet.metaData
                val columnCount = metaData.columnCount
                for (i in 1..columnCount) {
                    columns.add(
                        Column(
                            name = metaData.getColumnName(i),
                            typeName = metaData.getColumnTypeName(i),
                            isNullable = metaData.isNullable(i) != java.sql.DatabaseMetaData.columnNoNulls
                        )
                    )
                }
                while (resultSet.next()) {
                    val values = mutableListOf<Any?>()
                    for (i in 1..columnCount) {
                        values.add(resultSet.getObject(i))
                    }
                    rows.add(Row(values))
                }
                resultSet.close()
                statement.close()
                Result.success(
                    ExecutionResult.Query(
                        QueryResult(columns, rows, rows.size, executionTime)
                    )
                )
            } else {
                val affectedRows = statement.updateCount
                statement.close()
                Result.success(
                    ExecutionResult.Update(
                        UpdateResult(affectedRows, executionTime)
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun openCursor(
        ctx: DbExecutionContext,
        channel: DbExecutionChannel
    ): Result<CommandCursor> = withContext(Dispatchers.IO) {
        val conn = try {
            requireConnection(ctx)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
        try {
            val (statement, resultSet) = when (val cmd = channel.command) {
                is DbCommand.RawSql -> {
                    val st = conn.createStatement(
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY
                    )
                    st.fetchSize = 500
                    st to st.executeQuery(cmd.sql)
                }
                is DbCommand.PreparedSql -> {
                    val ps = conn.prepareStatement(
                        cmd.sql,
                        ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY
                    )
                    ps.fetchSize = 500
                    bindParams(ps, cmd.params)
                    ps to ps.executeQuery()
                }
                is DbCommand.RedisArgv -> throw IllegalArgumentException("JDBC 不支持 Redis 命令")
            }
            Result.success(JdbcCommandCursor(resultSet, statement))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private class JdbcCommandCursor(
        private val resultSet: ResultSet,
        private val statement: Statement,
        private val batchSize: Int = 500
    ) : CommandCursor {
        private var closed = false

        override suspend fun hasNext(): Boolean = withContext(Dispatchers.IO) {
            if (closed) return@withContext false
            !resultSet.isClosed && !resultSet.isAfterLast
        }

        override suspend fun nextBatch(): Result<CommandBatch> =
            withContext(Dispatchers.IO) {
                if (closed) {
                    return@withContext Result.success(CommandBatch(emptyList(), emptyList()))
                }
                try {
                    val metaData = resultSet.metaData
                    val columnCount = metaData.columnCount
                    val columns = (1..columnCount).map { i ->
                        Column(
                            name = metaData.getColumnName(i),
                            typeName = metaData.getColumnTypeName(i),
                            isNullable = metaData.isNullable(i) != java.sql.DatabaseMetaData.columnNoNulls
                        )
                    }
                    val rows = mutableListOf<Row>()
                    var count = 0
                    while (count < batchSize && resultSet.next()) {
                        val values = mutableListOf<Any?>()
                        for (i in 1..columnCount) {
                            values.add(resultSet.getObject(i))
                        }
                        rows.add(Row(values))
                        count++
                    }
                    if (count == 0) {
                        close()
                    }
                    Result.success(CommandBatch(columns, rows))
                } catch (e: Exception) {
                    close()
                    Result.failure(e)
                }
            }

        override suspend fun close(): Result<Unit> = withContext(Dispatchers.IO) {
            if (!closed) {
                try {
                    resultSet.close()
                } catch (_: Exception) {
                }
                try {
                    statement.close()
                } catch (_: Exception) {
                }
                closed = true
            }
            Result.success(Unit)
        }
    }
}

