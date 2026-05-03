package space.xiaoxiao.databasemanager.features

import androidx.compose.runtime.*
import space.xiaoxiao.databasemanager.core.*
import kotlinx.coroutines.CoroutineScope

enum class ConnectionUiState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED
}

/**
 * Coordinator/facade over 4 focused sub-ViewModels:
 *   - [ConnectionViewModel] – connection lifecycle & connection-level state
 *   - [QueryViewModel]       – query execution, results, history, transactions
 *   - [BrowserViewModel]     – schema browsing, tables, indexes, drill-down
 *   - [SchemaEditorViewModel] – DDL operations (table/column/index/database)
 *
 * Preserves the exact public API that screens depend on.
 */
class DatabaseViewModel(
    historyStorage: QueryHistoryStorage? = null
) {
    // ── Sub-ViewModels ──

    internal val connection = ConnectionViewModel()
    internal val query = QueryViewModel(connection, historyStorage)
    internal val browser = BrowserViewModel(connection)
    internal val schema = SchemaEditorViewModel(connection, browser)

    // ═══════════════════════════════════════════════════════════
    //  Delegated public API — identical to original DatabaseViewModel
    // ═══════════════════════════════════════════════════════════

    // -- ConnectionViewModel --

    val connectionState: ConnectionUiState get() = connection.connectionState

    val operationState: UiState<Nothing> get() = connection.operationState

    var lastErrorMessage: String?
        get() = connection.lastErrorMessage
        internal set(value) { connection.lastErrorMessage = value }

    val databaseType: DatabaseType get() = connection.databaseType

    val supportedFeatures: Set<DatabaseFeature> get() = connection.supportedFeatures

    val currentConfigId: String? get() = connection.currentConfigId

    val isInTransaction: Boolean get() = connection.isInTransaction

    suspend fun connect(config: DatabaseConfigInfo) = connection.connect(config)

    suspend fun disconnect() {
        connection.disconnect()
        query.clearResults()
        browser.clearState()
        schema.reset()
    }

    suspend fun close() = disconnect()

    fun clearError() = connection.clearError()

    fun clearOperationState() = connection.clearOperationState()

    fun clearServerDatabasesList() = browser.clearServerDatabasesList()

    // -- QueryViewModel --

    val isExecuting: Boolean get() = query.isExecuting

    val lastQueryResult: QueryResult? get() = query.lastQueryResult

    val lastUpdateResult: UpdateResult? get() = query.lastUpdateResult

    val historyItems: List<QueryHistoryItem> get() = query.historyItems

    val transactionMode: TransactionMode get() = query.transactionMode

    val transactionIsolationLevel: TransactionIsolationLevel get() = query.transactionIsolationLevel

    suspend fun executeCommand(command: String) = query.executeCommand(command)

    suspend fun loadHistory() = query.loadHistory()

    fun saveQueryHistory(
        scope: CoroutineScope,
        sql: String,
        rowCount: Int,
        executionTimeMs: Long,
        isSuccess: Boolean
    ) = query.saveQueryHistory(scope, sql, rowCount, executionTimeMs, isSuccess)

    suspend fun clearHistory() = query.clearHistory()

    suspend fun deleteHistory(id: String) = query.deleteHistory(id)

    fun updateTransactionMode(mode: TransactionMode) = query.updateTransactionMode(mode)

    suspend fun setTransactionIsolationLevel(level: TransactionIsolationLevel) =
        query.setTransactionIsolationLevel(level)

    suspend fun beginTransaction() = query.beginTransaction()

    suspend fun commitTransaction() = query.commitTransaction()

    suspend fun rollbackTransaction() = query.rollbackTransaction()

    // -- BrowserViewModel --

    val tables: List<TableInfo> get() = browser.tables

    val redisKeys: List<String> get() = browser.redisKeys

    val tableSchema: TableSchema? get() = browser.tableSchema

    val currentTableData: QueryResult? get() = browser.currentTableData

    val isLoadingData: Boolean get() = browser.isLoadingData

    val indexes: List<IndexInfo> get() = browser.indexes

    val tableStats: TableStats? get() = browser.tableStats

    val databaseSize: Long? get() = browser.databaseSize

    val serverDatabases: List<String> get() = browser.serverDatabases

    val currentDatabaseName: String? get() = browser.currentDatabaseName

    suspend fun loadTables() = browser.loadTables()

    suspend fun loadRedisKeys(pattern: String = "*") = browser.loadRedisKeys(pattern)

    suspend fun loadTableData(tableName: String, offset: Int = 0) =
        browser.loadTableData(tableName, offset)

    suspend fun loadTableSchema(tableName: String) = browser.loadTableSchema(tableName)

    suspend fun loadIndexes(tableName: String) = browser.loadIndexes(tableName)

    suspend fun loadTableStats(tableName: String) = browser.loadTableStats(tableName)

    suspend fun loadDatabaseSize() = browser.loadDatabaseSize()

    suspend fun loadServerDatabases() = browser.loadServerDatabases()

    suspend fun switchServerDatabase(databaseName: String) =
        browser.switchServerDatabase(databaseName)

    // -- SchemaEditorViewModel --

    val isManagingTable: Boolean get() = schema.isManagingTable

    suspend fun createTable(definition: TableDefinition) = schema.createTable(definition)

    suspend fun dropTable(tableName: String) = schema.dropTable(tableName)

    suspend fun renameTable(oldName: String, newName: String) =
        schema.renameTable(oldName, newName)

    suspend fun truncateTable(tableName: String) = schema.truncateTable(tableName)

    suspend fun addColumn(tableName: String, column: ColumnDefinition) =
        schema.addColumn(tableName, column)

    suspend fun modifyColumn(tableName: String, modification: ColumnModification) =
        schema.modifyColumn(tableName, modification)

    suspend fun dropColumn(tableName: String, columnName: String) =
        schema.dropColumn(tableName, columnName)

    suspend fun createIndex(
        tableName: String,
        indexName: String,
        columns: List<String>,
        isUnique: Boolean
    ) = schema.createIndex(tableName, indexName, columns, isUnique)

    suspend fun dropIndex(tableName: String, indexName: String) =
        schema.dropIndex(tableName, indexName)

    suspend fun createDatabase(name: String, charset: String? = null) =
        schema.createDatabase(name, charset)

    suspend fun dropDatabase(name: String) = schema.dropDatabase(name)
}
