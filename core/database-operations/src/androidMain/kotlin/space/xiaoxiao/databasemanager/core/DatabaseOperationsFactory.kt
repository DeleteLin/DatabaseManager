package space.xiaoxiao.databasemanager.core

/**
 * Android 平台数据库客户端工厂
 * 使用统一的 JDBC 实现支持 MySQL 和 PostgreSQL，使用 Jedis 支持 Redis
 */
actual fun createDatabaseClient(config: DatabaseConfig): DatabaseClient =
    when (config.type) {
        DatabaseType.MYSQL, DatabaseType.POSTGRESQL -> {
            val ctx = JdbcExecutionContext(config)
            val executor = JdbcCommandExecutor(config.type)
            val metadata = JdbcDatabaseOperations(ctx)
            DatabaseClient.Relational(
                context = ctx,
                executor = executor,
                metadata = metadata
            )
        }

        DatabaseType.REDIS -> {
            val ctx = RedisExecutionContext(config)
            val executor = RedisCommandExecutor()
            val metadata = RedisDatabaseOperations(ctx)
            DatabaseClient.KeyValue(
                context = ctx,
                executor = executor,
                metadata = metadata
            )
        }
    }
