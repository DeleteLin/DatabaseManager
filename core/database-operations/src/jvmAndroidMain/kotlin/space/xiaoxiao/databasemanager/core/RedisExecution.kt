package space.xiaoxiao.databasemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.Jedis
import redis.clients.jedis.Protocol
import redis.clients.jedis.commands.ProtocolCommand
import redis.clients.jedis.util.SafeEncoder

/**
 * Redis 执行上下文：负责 Jedis 连接生命周期与当前逻辑 DB 状态。
 */
class RedisExecutionContext(
    override val config: DatabaseConfig
) : DbExecutionContext {

    internal var jedis: Jedis? = null
    internal var currentDbIndex: Int = 0

    override val databaseType: DatabaseType = DatabaseType.REDIS

    override var currentSchema: String? = null

    override var currentDatabase: String?
        get() = "DB $currentDbIndex"
        set(_) {
            // 逻辑 DB 通过 index 表示，这里不直接设置
        }

    override suspend fun connect(): ConnectionStatus = withContext(Dispatchers.IO) {
        try {
            jedis?.close()

            val j = if (config.password.isNotEmpty()) {
                Jedis(config.host, config.port).also { it.auth(config.password) }
            } else {
                Jedis(config.host, config.port)
            }

            val dbIndex = config.database.toIntOrNull() ?: 0
            j.select(dbIndex)

            jedis = j
            currentDbIndex = dbIndex
            ConnectionStatus.Connected
        } catch (e: Exception) {
            jedis = null
            ConnectionStatus.Error("连接失败：${e.message}", e)
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                jedis?.close()
            } catch (_: Exception) {
            } finally {
                jedis = null
            }
        }
    }

    override fun isConnected(): Boolean = jedis != null
}

/**
 * Redis 无状态执行器：基于 RedisExecutionContext 解析和执行命令。
 */
class RedisCommandExecutor : CommandExecutor {

    override val databaseType: DatabaseType = DatabaseType.REDIS

    private fun requireJedis(ctx: DbExecutionContext): Jedis {
        val redisCtx = ctx as? RedisExecutionContext
            ?: throw IllegalArgumentException("DbExecutionContext 必须是 RedisExecutionContext")
        return redisCtx.jedis
            ?: throw IllegalStateException("未连接到 Redis")
    }

    override suspend fun executeQuery(
        ctx: DbExecutionContext,
        channel: DbExecutionChannel
    ): Result<QueryResult> = withContext(Dispatchers.IO) {
        val conn = try {
            requireJedis(ctx)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
        try {
            val startTime = System.currentTimeMillis()
            val (command, args) = when (val cmd = channel.command) {
                is DbCommand.RawSql -> parseCommand(cmd.sql)
                is DbCommand.RedisArgv -> cmd.command to cmd.args.map { it.toRedisString() }
                is DbCommand.PreparedSql -> throw IllegalArgumentException("Redis 不支持 PreparedSql")
            }
            val result = executeGenericCommand(conn, command, args)
            val executionTime = System.currentTimeMillis() - startTime
            val (columns, rows) = parseRedisResult(result)
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
            requireJedis(ctx)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
        try {
            val startTime = System.currentTimeMillis()
            val (command, args) = when (val cmd = channel.command) {
                is DbCommand.RawSql -> parseCommand(cmd.sql)
                is DbCommand.RedisArgv -> cmd.command to cmd.args.map { it.toRedisString() }
                is DbCommand.PreparedSql -> throw IllegalArgumentException("Redis 不支持 PreparedSql")
            }
            val result = executeGenericCommand(conn, command, args)
            val executionTime = System.currentTimeMillis() - startTime
            val affectedRows = toAffectedRows(result)
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
            requireJedis(ctx)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
        try {
            val startTime = System.currentTimeMillis()
            val (command, args) = when (val cmd = channel.command) {
                is DbCommand.RawSql -> parseCommand(cmd.sql)
                is DbCommand.RedisArgv -> cmd.command to cmd.args.map { it.toRedisString() }
                is DbCommand.PreparedSql -> throw IllegalArgumentException("Redis 不支持 PreparedSql")
            }
            val result = executeGenericCommand(conn, command, args)
            val executionTime = System.currentTimeMillis() - startTime

            val isQueryCommand = setOf(
                "GET", "MGET", "KEYS", "HGET", "HGETALL", "HKEYS", "HVALS", "HLEN",
                "LRANGE", "LLEN", "LINDEX", "SMEMBERS", "SCARD", "SRANDMEMBER",
                "ZCARD", "ZCOUNT", "ZRANGE", "ZREVRANGE", "ZSCORE", "ZRANK",
                "TYPE", "TTL", "PTTL", "EXISTS", "DBSIZE", "INFO", "CONFIG"
            ).contains(command.uppercase())

            if (isQueryCommand) {
                val (columns, rows) = parseRedisResult(result)
                Result.success(
                    ExecutionResult.Query(
                        QueryResult(columns, rows, rows.size, executionTime)
                    )
                )
            } else {
                val affectedRows = toAffectedRows(result)
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
    ): Result<CommandCursor> =
        Result.failure(UnsupportedOperationException("Redis 不支持游标查询"))

    // ======== 以下为内部解析与执行工具 ========

    private fun parseCommand(sql: String): Pair<String, List<String>> {
        val trimmedSql = sql.trim()
        val parts = trimmedSql.split("\\s+".toRegex()).filter { it.isNotBlank() }
        require(parts.isNotEmpty()) { "命令不能为空" }

        val command = parts[0].uppercase()
        val args = parts.drop(1).map { it.trim('`', '"', '\'') }
        return command to args
    }

    private fun DbArg.toRedisString(): String = when (this) {
        is DbArg.Literal -> value
        is DbArg.Param -> value.toRedisString()
    }

    private fun DbParam.toRedisString(): String = when (this) {
        DbParam.Null -> error("Redis 参数不支持 NULL")
        is DbParam.Str -> value
        is DbParam.Int32 -> value.toString()
        is DbParam.Int64 -> value.toString()
        is DbParam.Float64 -> value.toString()
        is DbParam.Bool -> if (value) "1" else "0"
        is DbParam.Bytes -> String(value, Charsets.UTF_8)
    }

    private fun toAffectedRows(result: Any?): Int {
        return when (result) {
            is Long -> if (result > 0L) 1 else 0
            is Int -> if (result > 0) 1 else 0
            is String -> if (result.uppercase() == "OK") 1 else 0
            is Boolean -> if (result) 1 else 0
            null -> 0
            else -> 1
        }
    }

    private fun executeGenericCommand(conn: Jedis, commandName: String, args: List<String>): Any? {
        val trimmedCommand = commandName.uppercase()

        val protocolCommand = try {
            Protocol.Command.valueOf(trimmedCommand)
        } catch (_: IllegalArgumentException) {
            return executeCustomCommand(conn, trimmedCommand, args)
        }

        return try {
            val response = conn.sendCommand(protocolCommand, *args.toTypedArray())
            parseResponse(response)
        } catch (e: Exception) {
            throw RuntimeException("执行命令 $commandName 失败：${e.message}", e)
        }
    }

    private fun executeCustomCommand(conn: Jedis, command: String, args: List<String>): Any? {
        return try {
            val response = conn.sendCommand(
                object : ProtocolCommand {
                    override fun getRaw(): ByteArray = SafeEncoder.encode(command)
                },
                *args.toTypedArray()
            )
            parseResponse(response)
        } catch (e: Exception) {
            throw RuntimeException("执行自定义命令 $command 失败：${e.message}", e)
        }
    }

    private fun parseResponse(response: Any?): Any? {
        return when (response) {
            is redis.clients.jedis.Response<*> -> {
                val rawValue = response.get()
                parseRawValue(rawValue)
            }

            else -> parseRawValue(response)
        }
    }

    private fun parseRawValue(value: Any?): Any? {
        return when (value) {
            null -> null
            is String -> value
            is Long -> value
            is Double -> value
            is Boolean -> value
            is Int -> value
            is ByteArray -> String(value, Charsets.UTF_8)
            is List<*> -> value.map { parseRawValue(it) }
            is Array<*> -> value.toList().map { parseRawValue(it) }
            is Set<*> -> value.map { parseRawValue(it) }
            is Map<*, *> -> value.mapKeys { parseRawValue(it.key) }
                .mapValues { parseRawValue(it.value) }
            else -> value.toString()
        }
    }

    private fun parseRedisResult(result: Any?): Pair<List<Column>, List<Row>> {
        return when {
            result == null -> listOf(Column("result", "NIL")) to emptyList()
            result is String -> listOf(Column("result", "STRING")) to listOf(Row(listOf(result)))
            result is Long -> listOf(Column("result", "INTEGER")) to listOf(Row(listOf(result)))
            result is Int -> listOf(Column("result", "INTEGER")) to listOf(Row(listOf(result)))
            result is Double -> listOf(Column("result", "FLOAT")) to listOf(Row(listOf(result)))
            result is Boolean -> listOf(Column("result", "BOOLEAN")) to listOf(Row(listOf(result)))
            result is List<*> -> {
                if (result.isEmpty()) {
                    listOf(Column("result", "LIST")) to emptyList()
                } else if (result.all { it is String }) {
                    listOf(Column("result", "STRING")) to result.map { Row(listOf(it as String)) }
                } else {
                    listOf(Column("result", "LIST")) to result.map { Row(listOf(it?.toString())) }
                }
            }

            result is Map<*, *> -> {
                listOf(Column("field", "STRING"), Column("value", "STRING")) to
                    result.map {
                        Row(
                            listOf(
                                it.key?.toString() ?: "",
                                it.value?.toString() ?: ""
                            )
                        )
                    }
            }

            result is Set<*> -> {
                if (result.isEmpty()) {
                    listOf(Column("result", "SET")) to emptyList()
                } else {
                    listOf(Column("result", "STRING")) to result.map { Row(listOf(it?.toString())) }
                }
            }

            result is ByteArray -> listOf(Column("result", "BINARY")) to listOf(Row(listOf(result.toString(Charsets.UTF_8))))
            else -> listOf(Column("result", "STRING")) to listOf(Row(listOf(result.toString())))
        }
    }
}

