package space.xiaoxiao.databasemanager.features

import androidx.compose.runtime.*
import space.xiaoxiao.databasemanager.core.*

/**
 * Handles schema browsing: tables, table schema, indexes, statistics,
 * database size, Redis keys, server-database list, and drill-down state.
 *
 * Receives a reference to [ConnectionViewModel] so it can access the
 * shared [DatabaseClient] and error state.
 */
class BrowserViewModel(
    private val conn: ConnectionViewModel
) {

    // ── Tables ──

    private var _tables by mutableStateOf(emptyList<TableInfo>())
    val tables: List<TableInfo> get() = _tables

    // ── Redis keys ──

    private var _redisKeys by mutableStateOf(emptyList<String>())
    val redisKeys: List<String> get() = _redisKeys

    // ── Table schema ──

    private var _tableSchema by mutableStateOf<TableSchema?>(null)
    val tableSchema: TableSchema? get() = _tableSchema

    // ── Table data ──

    private var _currentTableData by mutableStateOf<QueryResult?>(null)
    val currentTableData: QueryResult? get() = _currentTableData

    var isLoadingData by mutableStateOf(false)
        private set

    // ── Indexes ──

    private var _indexes by mutableStateOf(emptyList<IndexInfo>())
    val indexes: List<IndexInfo> get() = _indexes

    // ── Statistics ──

    private var _tableStats by mutableStateOf<TableStats?>(null)
    val tableStats: TableStats? get() = _tableStats

    private var _databaseSize by mutableStateOf<Long?>(null)
    val databaseSize: Long? get() = _databaseSize

    // ── Server databases ──

    private var _serverDatabases by mutableStateOf(emptyList<String>())
    val serverDatabases: List<String> get() = _serverDatabases

    private var _currentDatabaseName by mutableStateOf<String?>(null)
    val currentDatabaseName: String? get() = _currentDatabaseName

    // ── Public methods ──

    /** Immediately clears the server-database list (used before re-fetch). */
    fun clearServerDatabasesList() {
        _serverDatabases = emptyList()
    }

    suspend fun loadTables() {
        when (val current = conn.client) {
            is DatabaseClient.KeyValue -> {
                current.metadata.keys("*").onSuccess { _redisKeys = it }
                    .onFailure { conn.lastErrorMessage = it.message }
            }
            is DatabaseClient.Relational -> {
                current.metadata.listTables().onSuccess { _tables = it }
                    .onFailure { conn.lastErrorMessage = it.message }
            }
            null -> return
        }
    }

    suspend fun loadRedisKeys(pattern: String = "*") {
        val current = conn.client as? DatabaseClient.KeyValue ?: return
        current.metadata.keys(pattern).onSuccess { _redisKeys = it }
            .onFailure { conn.lastErrorMessage = it.message }
    }

    suspend fun loadTableData(tableName: String, offset: Int = 0) {
        val current = conn.client as? DatabaseClient.Relational ?: return
        isLoadingData = true
        current.metadata.getTableData(tableName = tableName, limit = 100, offset = offset)
            .onSuccess { _currentTableData = it }
            .onFailure { conn.lastErrorMessage = it.message }
        isLoadingData = false
    }

    suspend fun loadTableSchema(tableName: String) {
        val current = conn.client as? DatabaseClient.Relational ?: return
        current.metadata.getTableSchema(tableName = tableName)
            .onSuccess { _tableSchema = it }
            .onFailure { conn.lastErrorMessage = it.message }
    }

    suspend fun loadIndexes(tableName: String) {
        val relational = conn.client as? DatabaseClient.Relational ?: return
        relational.metadata.getIndexes(tableName)
            .onSuccess { _indexes = it }
            .onFailure { conn.lastErrorMessage = it.message }
    }

    suspend fun loadTableStats(tableName: String) {
        val relational = conn.client as? DatabaseClient.Relational ?: return
        relational.metadata.getTableStats(tableName)
            .onSuccess { _tableStats = it }
            .onFailure { conn.lastErrorMessage = it.message }
    }

    suspend fun loadDatabaseSize() {
        val relational = conn.client as? DatabaseClient.Relational ?: return
        relational.metadata.getDatabaseSize()
            .onSuccess { _databaseSize = it }
            .onFailure { conn.lastErrorMessage = it.message }
    }

    // ── Server database management ──

    suspend fun loadServerDatabases(): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            _serverDatabases = emptyList()
            _currentDatabaseName = null
            return false
        }
        return relational.metadata.listDatabases()
            .onSuccess { databases ->
                _serverDatabases = databases
                loadCurrentDatabaseName()
            }
            .onFailure { conn.lastErrorMessage = it.message }
            .isSuccess
    }

    suspend fun switchServerDatabase(databaseName: String): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持多库管理"
            return false
        }
        return try {
            _tableSchema = null
            _indexes = emptyList()
            _tableStats = null
            _currentTableData = null
            val result = relational.metadata.switchDatabase(databaseName)
            result.onSuccess {
                _currentDatabaseName = databaseName
                loadTables()
            }.onFailure {
                conn.lastErrorMessage = it.message
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            false
        }
    }

    // ── Internal helpers ──

    /** Clears all cached browser data (called on disconnect). */
    internal fun clearState() {
        _tables = emptyList()
        _tableSchema = null
        _indexes = emptyList()
        _tableStats = null
        _currentTableData = null
        isLoadingData = false
        _serverDatabases = emptyList()
        _currentDatabaseName = null
    }

    private suspend fun loadCurrentDatabaseName() {
        val relational = conn.client as? DatabaseClient.Relational ?: return
        relational.metadata.getCurrentDatabase()
            .onSuccess { _currentDatabaseName = it }
            .onFailure { conn.lastErrorMessage = it.message }
    }
}
