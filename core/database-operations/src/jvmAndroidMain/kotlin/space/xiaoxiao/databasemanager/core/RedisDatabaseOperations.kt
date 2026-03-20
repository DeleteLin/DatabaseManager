package space.xiaoxiao.databasemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import redis.clients.jedis.Jedis
import redis.clients.jedis.Protocol
import redis.clients.jedis.commands.ProtocolCommand
import redis.clients.jedis.util.SafeEncoder
import redis.clients.jedis.exceptions.JedisException

/**
 * Redis 元数据实现
 * 仅依赖 RedisExecutionContext 暴露的 Jedis 与当前逻辑 DB
 */
class RedisDatabaseOperations(
    private val context: RedisExecutionContext
) : KeyValueMetadataOperations {

    override suspend fun getDatabaseSize(): Result<Long> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try { Result.success(conn.dbSize()) } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun listLogicalDatabases(): Result<List<String>> = withContext(Dispatchers.IO) {
        Result.success((0..15).map { "DB $it" })
    }

    override suspend fun switchLogicalDatabase(database: String): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try {
            val dbIndex = database.removePrefix("DB ").trim().toIntOrNull() ?: return@withContext Result.failure(IllegalArgumentException("无效的数据库编号"))
            conn.select(dbIndex)
            context.currentDbIndex = dbIndex
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun getCurrentLogicalDatabase(): Result<String> = withContext(Dispatchers.IO) {
        Result.success("DB ${context.currentDbIndex}")
    }

    override suspend fun keys(pattern: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try { Result.success(conn.keys(pattern).toList()) } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun keyType(key: String): Result<String> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try { Result.success(conn.type(key).toString()) } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun redisGet(key: String): Result<String?> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try { Result.success(conn.get(key)) } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun redisSet(key: String, value: String, ttlSeconds: Int?): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try {
            if (ttlSeconds != null && ttlSeconds > 0) {
                conn.setex(key, ttlSeconds.toLong(), value)
            } else {
                conn.set(key, value)
            }
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun redisDel(vararg keys: String): Result<Int> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try { Result.success(conn.del(*keys).toInt()) } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun redisHGetAll(key: String): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try { Result.success(conn.hgetAll(key)) } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun redisHSet(key: String, field: String, value: String): Result<Int> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try { Result.success(conn.hset(key, field, value).toInt()) } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun redisLRange(key: String, start: Int, stop: Int): Result<List<String>> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try { Result.success(conn.lrange(key, start.toLong(), stop.toLong()).toList()) } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun redisSMembers(key: String): Result<Set<String>> = withContext(Dispatchers.IO) {
        val conn = context.jedis ?: return@withContext Result.failure(IllegalStateException("未连接"))
        try { Result.success(conn.smembers(key)) } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * 通用命令执行接口（供 UI 查询页面使用）
     * 执行任意 Redis 命令，返回原始结果
     *
     * @param command Redis 命令名称（如 "GET", "HGETALL", "FT.SEARCH" 等）
     * @param args 命令参数列表
     * @return 命令执行结果
     *
     * 使用示例：
     * ```kotlin
     * // 简单命令
     * executeCommand("GET", "mykey")
     *
     * // 带参数命令
     * executeCommand("LRANGE", "mylist", "0", "-1")
     *
     * // Redis Search 模块命令
     * executeCommand("FT.SEARCH", "idx:*", "@price:[0 100]")
     *
     * // RedisJSON 模块命令
     * executeCommand("JSON.GET", "mydoc", "$.name")
     * ```
     */
    suspend fun executeCommand(command: String, vararg args: String): Result<Any?> =
        Result.failure(UnsupportedOperationException("通用 Redis 命令执行已迁移到 RedisCommandExecutor"))
}
