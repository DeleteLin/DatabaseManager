package space.xiaoxiao.databasemanager.core

/**
 * JDBC 关系型数据库元数据操作门面
 *
 * 组合 6 个聚焦的处理器以替代原有的 god class：
 * - JdbcTableHandler           表数据操作（listTables, getTableSchema, getTableData, getTableStats）
 * - JdbcTableAdminHandler      表管理操作（createTable, dropTable, renameTable, truncateTable）
 * - JdbcColumnHandler          列管理操作（addColumn, modifyColumn, dropColumn）
 * - JdbcIndexHandler           索引管理操作（getIndexes, createIndex, dropIndex）
 * - JdbcTransactionHandler     事务管理操作（begin/commit/rollback/隔离级别）
 * - JdbcDatabaseAdminHandler   数据库管理操作（list/switch/create/drop database, getDatabaseSize）
 *
 * 共享工具函数位于 JdbcUtils.kt，由所有处理器及本门面共同使用。
 */
class JdbcDatabaseOperations(
    context: JdbcExecutionContext
) : RelationalMetadataOperations {

    // ==================== 处理器组合 ====================

    private val tableHandler = JdbcTableHandler(context)
    private val tableAdminHandler = JdbcTableAdminHandler(context)
    private val columnHandler = JdbcColumnHandler(context)
    private val indexHandler = JdbcIndexHandler(context)
    private val transactionHandler = JdbcTransactionHandler(context)
    private val databaseAdminHandler = JdbcDatabaseAdminHandler(context)

    // ==================== TableOperations ====================

    override suspend fun listTables(schema: String?) = tableHandler.listTables(schema)
    override suspend fun getTableSchema(tableName: String, schema: String?) = tableHandler.getTableSchema(tableName, schema)
    override suspend fun getTableData(tableName: String, schema: String?, limit: Int, offset: Int) =
        tableHandler.getTableData(tableName, schema, limit, offset)
    override suspend fun getTableStats(tableName: String, schema: String?) = tableHandler.getTableStats(tableName, schema)

    // ==================== TableAdminOperations ====================

    override suspend fun createTable(definition: TableDefinition, schema: String?) = tableAdminHandler.createTable(definition, schema)
    override suspend fun dropTable(tableName: String, schema: String?) = tableAdminHandler.dropTable(tableName, schema)
    override suspend fun renameTable(oldName: String, newName: String, schema: String?) = tableAdminHandler.renameTable(oldName, newName, schema)
    override suspend fun truncateTable(tableName: String, schema: String?) = tableAdminHandler.truncateTable(tableName, schema)

    // ==================== ColumnOperations ====================

    override suspend fun addColumn(tableName: String, column: ColumnDefinition, schema: String?) = columnHandler.addColumn(tableName, column, schema)
    override suspend fun modifyColumn(tableName: String, modification: ColumnModification, schema: String?) = columnHandler.modifyColumn(tableName, modification, schema)
    override suspend fun dropColumn(tableName: String, columnName: String, schema: String?) = columnHandler.dropColumn(tableName, columnName, schema)

    // ==================== IndexOperations ====================

    override suspend fun getIndexes(tableName: String, schema: String?) = indexHandler.getIndexes(tableName, schema)
    override suspend fun createIndex(tableName: String, indexName: String, columns: List<String>, isUnique: Boolean, schema: String?) =
        indexHandler.createIndex(tableName, indexName, columns, isUnique, schema)
    override suspend fun dropIndex(tableName: String, indexName: String, schema: String?) = indexHandler.dropIndex(tableName, indexName, schema)

    // ==================== TransactionOperations ====================

    override suspend fun beginTransaction() = transactionHandler.beginTransaction()
    override suspend fun commitTransaction() = transactionHandler.commitTransaction()
    override suspend fun rollbackTransaction() = transactionHandler.rollbackTransaction()
    override suspend fun setTransactionIsolation(level: Int) = transactionHandler.setTransactionIsolation(level)

    // ==================== DatabaseAdminOperations ====================

    override suspend fun listDatabases() = databaseAdminHandler.listDatabases()
    override suspend fun switchDatabase(database: String) = databaseAdminHandler.switchDatabase(database)
    override suspend fun getCurrentDatabase() = databaseAdminHandler.getCurrentDatabase()
    override suspend fun createDatabase(name: String, charset: String?) = databaseAdminHandler.createDatabase(name, charset)
    override suspend fun dropDatabase(name: String) = databaseAdminHandler.dropDatabase(name)
    override suspend fun getDatabaseSize() = databaseAdminHandler.getDatabaseSize()
}
