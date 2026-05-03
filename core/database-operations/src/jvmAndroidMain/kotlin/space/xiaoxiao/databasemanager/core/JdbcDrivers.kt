package space.xiaoxiao.databasemanager.core

object MysqlJdbcDriver : DatabaseDriver {
    override val driverId = "mysql-jdbc"
    override val databaseType = DatabaseType.MYSQL
    override val capabilities = DatabaseCapabilities(
        supportsTransactions = true,
        supportsIndexes = true,
        supportsForeignKeys = true,
        supportsViews = true,
        supportsTriggers = true,
        supportsMultipleDatabases = true,
        supportsTableCreation = true,
        supportsTableAlteration = true,
        supportedFeatures = setOf(
            DatabaseFeature.EXECUTE_QUERY,
            DatabaseFeature.EXECUTE_UPDATE,
            DatabaseFeature.EXECUTE_ANY,
            DatabaseFeature.CURSOR_QUERY,
            DatabaseFeature.PAGED_QUERY,
            DatabaseFeature.LIST_TABLES,
            DatabaseFeature.GET_TABLE_SCHEMA,
            DatabaseFeature.GET_TABLE_DATA,
            DatabaseFeature.CREATE_TABLE,
            DatabaseFeature.DROP_TABLE,
            DatabaseFeature.RENAME_TABLE,
            DatabaseFeature.TRUNCATE_TABLE,
            DatabaseFeature.ADD_COLUMN,
            DatabaseFeature.MODIFY_COLUMN,
            DatabaseFeature.DROP_COLUMN,
            DatabaseFeature.GET_INDEXES,
            DatabaseFeature.CREATE_INDEX,
            DatabaseFeature.DROP_INDEX,
            DatabaseFeature.GET_TABLE_STATS,
            DatabaseFeature.GET_DATABASE_SIZE,
            DatabaseFeature.LIST_DATABASES,
            DatabaseFeature.SWITCH_DATABASE,
            DatabaseFeature.CREATE_DATABASE,
            DatabaseFeature.DROP_DATABASE,
            DatabaseFeature.TRANSACTION
        )
    )

    override suspend fun createClient(config: DatabaseConfig): DatabaseClient {
        val ctx = JdbcExecutionContext(config)
        val executor = JdbcCommandExecutor(DatabaseType.MYSQL)
        val metadata = JdbcDatabaseOperations(ctx)
        return DatabaseClient.Relational(ctx, executor, metadata)
    }
}

object PostgresJdbcDriver : DatabaseDriver {
    override val driverId = "postgresql-jdbc"
    override val databaseType = DatabaseType.POSTGRESQL
    override val capabilities = DatabaseCapabilities(
        supportsTransactions = true,
        supportsIndexes = true,
        supportsForeignKeys = true,
        supportsViews = true,
        supportsTriggers = true,
        supportsSchemas = true,
        supportsMultipleDatabases = true,
        supportsTableCreation = true,
        supportsTableAlteration = true,
        supportedFeatures = setOf(
            DatabaseFeature.EXECUTE_QUERY,
            DatabaseFeature.EXECUTE_UPDATE,
            DatabaseFeature.EXECUTE_ANY,
            DatabaseFeature.CURSOR_QUERY,
            DatabaseFeature.PAGED_QUERY,
            DatabaseFeature.LIST_TABLES,
            DatabaseFeature.GET_TABLE_SCHEMA,
            DatabaseFeature.GET_TABLE_DATA,
            DatabaseFeature.CREATE_TABLE,
            DatabaseFeature.DROP_TABLE,
            DatabaseFeature.RENAME_TABLE,
            DatabaseFeature.TRUNCATE_TABLE,
            DatabaseFeature.ADD_COLUMN,
            DatabaseFeature.MODIFY_COLUMN,
            DatabaseFeature.DROP_COLUMN,
            DatabaseFeature.GET_INDEXES,
            DatabaseFeature.CREATE_INDEX,
            DatabaseFeature.DROP_INDEX,
            DatabaseFeature.GET_TABLE_STATS,
            DatabaseFeature.GET_DATABASE_SIZE,
            DatabaseFeature.LIST_DATABASES,
            DatabaseFeature.SWITCH_DATABASE,
            DatabaseFeature.CREATE_DATABASE,
            DatabaseFeature.DROP_DATABASE,
            DatabaseFeature.TRANSACTION
        )
    )

    override suspend fun createClient(config: DatabaseConfig): DatabaseClient {
        val ctx = JdbcExecutionContext(config)
        val executor = JdbcCommandExecutor(DatabaseType.POSTGRESQL)
        val metadata = JdbcDatabaseOperations(ctx)
        return DatabaseClient.Relational(ctx, executor, metadata)
    }
}
