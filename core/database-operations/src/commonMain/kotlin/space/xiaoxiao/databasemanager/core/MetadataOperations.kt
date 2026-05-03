package space.xiaoxiao.databasemanager.core

// ============================================================================
// Interface Segregation Principle: RelationalMetadataOperations 拆分为 6 个聚焦接口
// 每个接口对应单一职责领域，便于独立实现与测试
// ============================================================================

/**
 * 表数据操作：查询表结构、数据、统计信息
 */
interface TableOperations {
    suspend fun listTables(schema: String? = null): Result<List<TableInfo>>
    suspend fun getTableSchema(tableName: String, schema: String? = null): Result<TableSchema>
    suspend fun getTableData(
        tableName: String,
        schema: String? = null,
        limit: Int = 100,
        offset: Int = 0
    ): Result<QueryResult>
    suspend fun getTableStats(tableName: String, schema: String? = null): Result<TableStats>
}

/**
 * 表管理操作：创建、删除、重命名、清空表
 */
interface TableAdminOperations {
    suspend fun createTable(definition: TableDefinition, schema: String? = null): Result<Unit>
    suspend fun dropTable(tableName: String, schema: String? = null): Result<Unit>
    suspend fun renameTable(oldName: String, newName: String, schema: String? = null): Result<Unit>
    suspend fun truncateTable(tableName: String, schema: String? = null): Result<Unit>
}

/**
 * 字段管理操作：添加、修改、删除列
 */
interface ColumnOperations {
    suspend fun addColumn(tableName: String, column: ColumnDefinition, schema: String? = null): Result<Unit>
    suspend fun modifyColumn(tableName: String, modification: ColumnModification, schema: String? = null): Result<Unit>
    suspend fun dropColumn(tableName: String, columnName: String, schema: String? = null): Result<Unit>
}

/**
 * 索引管理操作：查询、创建、删除索引
 */
interface IndexOperations {
    suspend fun getIndexes(tableName: String, schema: String? = null): Result<List<IndexInfo>>
    suspend fun createIndex(
        tableName: String,
        indexName: String,
        columns: List<String>,
        isUnique: Boolean,
        schema: String? = null
    ): Result<Unit>
    suspend fun dropIndex(tableName: String, indexName: String, schema: String? = null): Result<Unit>
}

/**
 * 事务管理操作：开启、提交、回滚事务及设置隔离级别
 */
interface TransactionOperations {
    suspend fun beginTransaction(): Result<Unit>
    suspend fun commitTransaction(): Result<Unit>
    suspend fun rollbackTransaction(): Result<Unit>
    suspend fun setTransactionIsolation(level: Int): Result<Unit>
}

/**
 * 数据库管理操作：列出、切换、创建、删除数据库及大小查询
 */
interface DatabaseAdminOperations {
    suspend fun listDatabases(): Result<List<String>>
    suspend fun switchDatabase(database: String): Result<Unit>
    suspend fun getCurrentDatabase(): Result<String>
    suspend fun createDatabase(name: String, charset: String? = null): Result<Unit>
    suspend fun dropDatabase(name: String): Result<Unit>
    suspend fun getDatabaseSize(): Result<Long>
}
