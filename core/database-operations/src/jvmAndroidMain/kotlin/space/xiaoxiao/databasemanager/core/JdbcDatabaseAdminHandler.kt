package space.xiaoxiao.databasemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.DriverManager

class JdbcDatabaseAdminHandler(
    private val context: JdbcExecutionContext
) : DatabaseAdminOperations {

    override suspend fun listDatabases(): Result<List<String>> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            val dbType = context.config.type
            when (dbType) {
                DatabaseType.MYSQL -> {
                    withStatement(conn) { statement ->
                        statement.executeQuery("SHOW DATABASES").useAndClose { resultSet ->
                            val databases = mutableListOf<String>()
                            while (resultSet.next()) {
                                val dbName = resultSet.getString(1)
                                if (dbName !in listOf("information_schema", "mysql", "performance_schema", "sys")) {
                                    databases.add(dbName)
                                }
                            }
                            Result.success(databases)
                        }
                    }
                }
                DatabaseType.POSTGRESQL -> {
                    withStatement(conn) { statement ->
                        statement.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY datname")
                            .useAndClose { resultSet ->
                                val databases = mutableListOf<String>()
                                while (resultSet.next()) {
                                    databases.add(resultSet.getString("datname"))
                                }
                                Result.success(databases)
                            }
                    }
                }
                else -> Result.failure(IllegalArgumentException("不支持的数据库类型：$dbType"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun switchDatabase(database: String): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            val dbType = context.config.type
            when (dbType) {
                DatabaseType.MYSQL -> {
                    withStatement(conn) { it.execute("USE ${quoteIdentifier(database, dbType)}") }
                    context.currentDatabase = database
                    Result.success(Unit)
                }
                DatabaseType.POSTGRESQL -> {
                    val newConfig = context.config.copy(database = database)

                    try { conn.close() } catch (_: Exception) {}

                    val jdbcUrl = "jdbc:postgresql://${newConfig.host}:${newConfig.port}/${newConfig.database}"
                    try {
                        Class.forName("org.postgresql.Driver")
                        context.connection = DriverManager.getConnection(jdbcUrl, newConfig.username, newConfig.password)
                        context.currentDatabase = database
                        Result.success(Unit)
                    } catch (e: Exception) {
                        Result.failure(Exception("切换到数据库 $database 失败：${e.message}"))
                    }
                }
                else -> Result.failure(IllegalArgumentException("不支持的数据库类型：$dbType"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentDatabase(): Result<String> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            val dbType = context.config?.type
            when (dbType) {
                DatabaseType.MYSQL -> {
                    withStatement(conn) { statement ->
                        statement.executeQuery("SELECT DATABASE() as current_db").useAndClose { resultSet ->
                            if (resultSet.next()) {
                                Result.success(resultSet.getString("current_db") ?: context.config?.database ?: "")
                            } else {
                                Result.success(context.config?.database ?: "")
                            }
                        }
                    }
                }
                DatabaseType.POSTGRESQL -> {
                    withStatement(conn) { statement ->
                        statement.executeQuery("SELECT current_database() as current_db").useAndClose { resultSet ->
                            if (resultSet.next()) {
                                Result.success(resultSet.getString("current_db") ?: context.config?.database ?: "")
                            } else {
                                Result.success(context.config?.database ?: "")
                            }
                        }
                    }
                }
                else -> Result.success(context.config?.database ?: "")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDatabase(name: String, charset: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))

        if (name.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("数据库名称不能为空"))
        }

        if (!name.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))) {
            return@withContext Result.failure(IllegalArgumentException("数据库名称只能包含字母、数字和下划线，且必须以字母或下划线开头"))
        }

        try {
            val dbType = context.config?.type
            when (dbType) {
                DatabaseType.MYSQL -> {
                    val charsetClause = if (charset != null) {
                        requireSafeCharsetName(charset)
                        " DEFAULT CHARACTER SET ${charset}"
                    } else {
                        ""
                    }
                    withStatement(conn) { it.execute("CREATE DATABASE IF NOT EXISTS ${quoteIdentifier(name, dbType)}$charsetClause") }
                    Result.success(Unit)
                }
                DatabaseType.POSTGRESQL -> {
                    val encodingClause = if (charset != null) {
                        requireSafeCharsetName(charset)
                        " ENCODING '${escapeSqlStringLiteral(charset)}' TEMPLATE template0 LC_COLLATE 'C' LC_CTYPE 'C'"
                    } else {
                        ""
                    }
                    withStatement(conn) { it.execute("CREATE DATABASE ${quoteIdentifier(name, dbType)}$encodingClause") }
                    Result.success(Unit)
                }
                else -> Result.failure(IllegalArgumentException("不支持的数据库类型：$dbType"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("创建数据库失败：${e.message}"))
        }
    }

    override suspend fun dropDatabase(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))

        if (name.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("数据库名称不能为空"))
        }

        if (!name.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))) {
            return@withContext Result.failure(IllegalArgumentException("数据库名称只能包含字母、数字和下划线，且必须以字母或下划线开头"))
        }

        try {
            val dbType = context.config?.type
            when (dbType) {
                DatabaseType.MYSQL -> {
                    withStatement(conn) { it.execute("DROP DATABASE ${quoteIdentifier(name, dbType)}") }
                    Result.success(Unit)
                }
                DatabaseType.POSTGRESQL -> {
                    withStatement(conn) { it.execute("DROP DATABASE ${quoteIdentifier(name, dbType)}") }
                    Result.success(Unit)
                }
                else -> Result.failure(IllegalArgumentException("不支持的数据库类型：$dbType"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("删除数据库失败：${e.message}"))
        }
    }

    override suspend fun getDatabaseSize(): Result<Long> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            when (context.config.type) {
                DatabaseType.MYSQL -> {
                    val sql = "SELECT SUM(DATA_LENGTH + INDEX_LENGTH) FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?"
                    withPreparedStatement(
                        conn,
                        sql,
                        binder = { ps -> ps.setString(1, conn.catalog) }
                    ) { ps ->
                        ps.executeQuery().useAndClose { resultSet ->
                            if (resultSet.next()) Result.success(resultSet.getLong(1)) else Result.success(0L)
                        }
                    }
                }
                DatabaseType.POSTGRESQL -> {
                    val sql = "SELECT pg_database_size(current_database())"
                    withStatement(conn) { statement ->
                        statement.executeQuery(sql).useAndClose { resultSet ->
                            if (resultSet.next()) Result.success(resultSet.getLong(1)) else Result.success(0L)
                        }
                    }
                }
                else -> Result.success(0L)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
