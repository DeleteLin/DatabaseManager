package space.xiaoxiao.databasemanager.core

object RedisDriver : DatabaseDriver {
    override val driverId = "redis-jedis"
    override val databaseType = DatabaseType.REDIS
    override val capabilities = DatabaseCapabilities(
        supportsTransactions = true,
        supportsMultipleDatabases = true,
        supportedFeatures = setOf(
            DatabaseFeature.EXECUTE_QUERY,
            DatabaseFeature.EXECUTE_UPDATE,
            DatabaseFeature.EXECUTE_ANY,
            DatabaseFeature.GET_TABLE_DATA,
            DatabaseFeature.LIST_DATABASES,
            DatabaseFeature.GET_DATABASE_SIZE,
            DatabaseFeature.REDIS_KEY_OPERATIONS,
            DatabaseFeature.REDIS_HASH_OPERATIONS,
            DatabaseFeature.REDIS_LIST_OPERATIONS,
            DatabaseFeature.REDIS_SET_OPERATIONS,
            DatabaseFeature.REDIS_ZSET_OPERATIONS
        )
    )

    override suspend fun createClient(config: DatabaseConfig): DatabaseClient {
        val ctx = RedisExecutionContext(config)
        val executor = RedisCommandExecutor()
        val metadata = RedisDatabaseOperations(ctx)
        return DatabaseClient.KeyValue(ctx, executor, metadata)
    }
}
