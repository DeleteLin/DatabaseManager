package space.xiaoxiao.databasemanager.features

import androidx.compose.runtime.*
import space.xiaoxiao.databasemanager.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class ConnectionUiState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED
}

class DatabaseViewModel(
    private val historyStorage: QueryHistoryStorage? = null
) {
    private var client: DatabaseClient? = null
    private var currentConfig: DatabaseConfigInfo? = null

    var connectionState by mutableStateOf(ConnectionUiState.DISCONNECTED)
        private set

    /**
     * 当前操作状态 - 用于统一跟踪加载/成功/错误状态
     */
    var operationState by mutableStateOf<UiState<Nothing>>(UiState.Idle)
        private set

    var lastErrorMessage by mutableStateOf<String?>(null)
        internal set

    var lastQueryResult by mutableStateOf<QueryResult?>(null)
        private set

    var lastUpdateResult by mutableStateOf<UpdateResult?>(null)
        private set

    var isExecuting by mutableStateOf(false)
        private set

    var transactionMode by mutableStateOf(TransactionMode.AUTO)
        private set

    var transactionIsolationLevel by mutableStateOf(TransactionIsolationLevel.READ_COMMITTED)
        private set

    var isInTransaction by mutableStateOf(false)
        private set

    private var _historyItems by mutableStateOf(emptyList<QueryHistoryItem>())
    val historyItems: List<QueryHistoryItem> get() = _historyItems

    private var _tables by mutableStateOf(emptyList<TableInfo>())
    val tables: List<TableInfo> get() = _tables

    private var _redisKeys by mutableStateOf(emptyList<String>())
    val redisKeys: List<String> get() = _redisKeys

    private var _tableSchema by mutableStateOf<TableSchema?>(null)
    val tableSchema: TableSchema? get() = _tableSchema

    private var _currentTableData by mutableStateOf<QueryResult?>(null)
    val currentTableData: QueryResult? get() = _currentTableData

    var isLoadingData by mutableStateOf(false)
        private set

    private var _indexes by mutableStateOf(emptyList<IndexInfo>())
    val indexes: List<IndexInfo> get() = _indexes

    private var _tableStats by mutableStateOf<TableStats?>(null)
    val tableStats: TableStats? get() = _tableStats

    private var _databaseSize by mutableStateOf<Long?>(null)
    val databaseSize: Long? get() = _databaseSize

    private var _serverDatabases by mutableStateOf(emptyList<String>())
    val serverDatabases: List<String> get() = _serverDatabases

    private var _currentDatabaseName by mutableStateOf<String?>(null)
    /**
     * 当前数据库支持的特性列表
     */
    var supportedFeatures by mutableStateOf(emptySet<DatabaseFeature>())
        private set

    /**
     * 当前数据库类型
     */
    var databaseType by mutableStateOf(DatabaseType.MYSQL)
        private set

    val currentDatabaseName: String? get() = _currentDatabaseName

    /**
     * 当前 ViewModel 已连接的数据配置 ID（来自 DatabaseConfigInfo.id）。
     * 用于复杂交互（弹窗先预览后确认）时避免重复 disconnect/connect。
     */
    val currentConfigId: String? get() = currentConfig?.id

    /**
     * 立即清空「服务器库」列表（切换实例预览时先清空再拉取，避免短暂显示上一连接的数据）。
     */
    fun clearServerDatabasesList() {
        _serverDatabases = emptyList()
    }

    var isManagingTable by mutableStateOf(false)
        private set

    suspend fun loadHistory() {
        historyStorage?.let { _historyItems = it.getHistory(50) }
    }

    /**
     * 保存查询历史（异步，不阻塞调用者）
     * @param scope 用于执行异步操作的协程作用域
     */
    fun saveQueryHistory(scope: CoroutineScope, sql: String, rowCount: Int, executionTimeMs: Long, isSuccess: Boolean) {
        currentConfig?.let { config ->
            historyStorage?.let { storage ->
                scope.launch {
                    storage.addHistory(
                        QueryHistoryItem(
                            sql = sql,
                            databaseId = config.id,
                            databaseName = config.name,
                            rowCount = rowCount,
                            executionTimeMs = executionTimeMs,
                            isSuccess = isSuccess
                        )
                    )
                    _historyItems = storage.getHistory(50)
                }
            }
        }
    }

    suspend fun clearHistory() {
        historyStorage?.let {
            it.clearHistory()
            _historyItems = emptyList()
        }
    }

    suspend fun deleteHistory(id: String) {
        historyStorage?.let {
            it.deleteHistory(id)
            _historyItems = _historyItems.filter { item -> item.id != id }
        }
    }

    suspend fun connect(config: DatabaseConfigInfo) {
        connectionState = ConnectionUiState.CONNECTING
        lastErrorMessage = null
        try {
            val dbConfig = config.toDatabaseConfig()
            val created = createDatabaseClient(dbConfig)
            val status = created.context.connect()
            when (status) {
                is ConnectionStatus.Connected -> {
                    client = created
                    currentConfig = config
                    connectionState = ConnectionUiState.CONNECTED
                    supportedFeatures = emptySet()
                    databaseType = dbConfig.type
                }
                is ConnectionStatus.Disconnected -> {
                    connectionState = ConnectionUiState.DISCONNECTED
                    lastErrorMessage = "连接已断开"
                }
                is ConnectionStatus.Error -> {
                    connectionState = ConnectionUiState.FAILED
                    lastErrorMessage = status.message
                }
            }
        } catch (e: Exception) {
            connectionState = ConnectionUiState.FAILED
            lastErrorMessage = e.message ?: "未知错误"
        }
    }

    suspend fun disconnect() {
        if (isInTransaction) {
            (client as? DatabaseClient.Relational)?.metadata?.rollbackTransaction()
            isInTransaction = false
        }
        client?.context?.disconnect()
        client = null
        currentConfig = null
        connectionState = ConnectionUiState.DISCONNECTED
        supportedFeatures = emptySet()
        databaseType = DatabaseType.MYSQL
        lastQueryResult = null

        // 清空当前页面缓存的元数据/表数据，避免切换连接后仍显示旧表详情
        _tables = emptyList()
        _tableSchema = null
        _indexes = emptyList()
        _tableStats = null
        _currentTableData = null
        isLoadingData = false
        isManagingTable = false
        _serverDatabases = emptyList()
        _currentDatabaseName = null
    }

    /**
     * 关闭 ViewModel 并释放资源
     * 应在 DisposableEffect.onDispose 中调用
     */
    suspend fun close() {
        disconnect()
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        lastErrorMessage = null
        operationState = UiState.Idle
    }

    /**
     * 清除操作状态
     */
    fun clearOperationState() {
        operationState = UiState.Idle
    }

    fun updateTransactionMode(mode: TransactionMode) {
        transactionMode = mode
    }

    suspend fun setTransactionIsolationLevel(level: TransactionIsolationLevel) {
        val relational = client as? DatabaseClient.Relational ?: return
        relational.metadata.setTransactionIsolation(level.value)
            .onSuccess { transactionIsolationLevel = level }
            .onFailure { lastErrorMessage = it.message }
    }

    suspend fun beginTransaction(): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持事务"
            return false
        }
        return relational.metadata.beginTransaction().onSuccess { isInTransaction = true }.isSuccess
    }

    suspend fun commitTransaction(): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持事务"
            return false
        }
        return relational.metadata.commitTransaction().onSuccess { isInTransaction = false }.isSuccess
    }

    suspend fun rollbackTransaction(): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持事务"
            return false
        }
        return relational.metadata.rollbackTransaction().onSuccess { isInTransaction = false }.isSuccess
    }

    suspend fun executeCommand(command: String): Boolean {
        val currentClient = client ?: run {
            lastErrorMessage = "未连接"
            operationState = UiState.error("未连接", errorCode = ErrorCode.CONNECTION_FAILED)
            return false
        }
        isExecuting = true
        operationState = UiState.Loading
        lastErrorMessage = null
        return try {
            val result = currentClient.executor.execute(
                currentClient.context,
                DbExecutionChannel(
                    sql = command,
                    purpose = ExecutionPurpose.USER
                )
            )
            result.onSuccess { executionResult ->
                when (executionResult) {
                    is ExecutionResult.Query -> {
                        lastQueryResult = executionResult.result
                        lastUpdateResult = null
                    }
                    is ExecutionResult.Update -> {
                        lastUpdateResult = executionResult.result
                        lastQueryResult = null
                    }
                }
                // 检测事务相关的 SQL 命令并同步状态
                syncTransactionState(command)
                operationState = UiState.Idle
            }.onFailure {
                lastErrorMessage = it.message
                operationState = UiState.error(it.message ?: "执行失败", it, ErrorCode.QUERY_FAILED)
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            operationState = UiState.error(e.message ?: "执行失败", e)
            false
        } finally {
            isExecuting = false
        }
    }

    /**
     * 检测 SQL 命令中的事务相关语句并同步 isInTransaction 状态
     */
    private fun syncTransactionState(command: String) {
        val upperCommand = command.trim().uppercase()
        // 检测事务开始语句
        if (upperCommand.startsWith("BEGIN") ||
            upperCommand.startsWith("START TRANSACTION") ||
            upperCommand.startsWith("SET AUTOCOMMIT=0")) {
            isInTransaction = true
        }
        // 检测事务结束语句
        else if (upperCommand.startsWith("COMMIT") ||
                 upperCommand.startsWith("ROLLBACK")) {
            isInTransaction = false
        }
        // 检测自动提交恢复
        else if (upperCommand.startsWith("SET AUTOCOMMIT=1")) {
            // 如果当前在事务中，数据库会自动提交，只需更新状态
            isInTransaction = false
        }
    }

    suspend fun loadTables() {
        when (val current = client) {
            is DatabaseClient.KeyValue -> {
                current.metadata.keys("*").onSuccess { _redisKeys = it }
                    .onFailure { lastErrorMessage = it.message }
            }
            is DatabaseClient.Relational -> {
                current.metadata.listTables().onSuccess { _tables = it }
                    .onFailure { lastErrorMessage = it.message }
            }
            null -> return
        }
    }

    /**
     * 加载 Redis Keys（支持模式匹配）
     */
    suspend fun loadRedisKeys(pattern: String = "*") {
        val current = client as? DatabaseClient.KeyValue ?: return
        current.metadata.keys(pattern).onSuccess { _redisKeys = it }
            .onFailure { lastErrorMessage = it.message }
    }

    suspend fun loadTableData(tableName: String, offset: Int = 0) {
        val current = client as? DatabaseClient.Relational ?: return
        isLoadingData = true
        current.metadata.getTableData(tableName = tableName, limit = 100, offset = offset)
            .onSuccess { _currentTableData = it }
            .onFailure { lastErrorMessage = it.message }
        isLoadingData = false
    }

    suspend fun loadTableSchema(tableName: String) {
        val current = client as? DatabaseClient.Relational ?: return
        current.metadata.getTableSchema(tableName = tableName)
            .onSuccess { _tableSchema = it }
            .onFailure { lastErrorMessage = it.message }
    }

    // ==================== 表管理 ====================

    suspend fun createTable(definition: TableDefinition): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持表管理"
            return false
        }
        isManagingTable = true
        lastErrorMessage = null
        return try {
            val result = relational.metadata.createTable(definition)
            if (result.isSuccess) {
                loadTables()
            } else {
                lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun dropTable(tableName: String): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持表管理"
            return false
        }
        isManagingTable = true
        lastErrorMessage = null
        return try {
            val result = relational.metadata.dropTable(tableName)
            if (result.isSuccess) {
                loadTables()
                if (_tableSchema?.tableName == tableName) {
                    _tableSchema = null
                }
            } else {
                lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun renameTable(oldName: String, newName: String): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持表管理"
            return false
        }
        isManagingTable = true
        lastErrorMessage = null
        return try {
            val result = relational.metadata.renameTable(oldName, newName)
            if (result.isSuccess) {
                loadTables()
            } else {
                lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun truncateTable(tableName: String): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持表管理"
            return false
        }
        isManagingTable = true
        lastErrorMessage = null
        return try {
            val result = relational.metadata.truncateTable(tableName)
            if (!result.isSuccess) {
                lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    // ==================== 字段管理 ====================

    suspend fun addColumn(tableName: String, column: ColumnDefinition): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持字段管理"
            return false
        }
        isManagingTable = true
        lastErrorMessage = null
        return try {
            val result = relational.metadata.addColumn(tableName, column)
            if (result.isSuccess) {
                loadTableSchema(tableName)
            } else {
                lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun modifyColumn(tableName: String, modification: ColumnModification): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持字段管理"
            return false
        }
        isManagingTable = true
        lastErrorMessage = null
        return try {
            val result = relational.metadata.modifyColumn(tableName, modification)
            if (result.isSuccess) {
                loadTableSchema(tableName)
            } else {
                lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun dropColumn(tableName: String, columnName: String): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持字段管理"
            return false
        }
        isManagingTable = true
        lastErrorMessage = null
        return try {
            val result = relational.metadata.dropColumn(tableName, columnName)
            if (result.isSuccess) {
                loadTableSchema(tableName)
            } else {
                lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    // ==================== 索引管理 ====================

    suspend fun loadIndexes(tableName: String) {
        val relational = client as? DatabaseClient.Relational ?: return
        relational.metadata.getIndexes(tableName)
            .onSuccess { _indexes = it }
            .onFailure { lastErrorMessage = it.message }
    }

    suspend fun createIndex(tableName: String, indexName: String, columns: List<String>, isUnique: Boolean): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持索引管理"
            return false
        }
        isManagingTable = true
        lastErrorMessage = null
        return try {
            val result = relational.metadata.createIndex(tableName, indexName, columns, isUnique)
            if (result.isSuccess) {
                loadIndexes(tableName)
            } else {
                lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    suspend fun dropIndex(tableName: String, indexName: String): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持索引管理"
            return false
        }
        isManagingTable = true
        lastErrorMessage = null
        return try {
            val result = relational.metadata.dropIndex(tableName, indexName)
            if (result.isSuccess) {
                loadIndexes(tableName)
            } else {
                lastErrorMessage = result.exceptionOrNull()?.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        } finally {
            isManagingTable = false
        }
    }

    // ==================== 统计信息 ====================

    suspend fun loadTableStats(tableName: String) {
        val relational = client as? DatabaseClient.Relational ?: return
        relational.metadata.getTableStats(tableName)
            .onSuccess { _tableStats = it }
            .onFailure { lastErrorMessage = it.message }
    }

    suspend fun loadDatabaseSize() {
        val relational = client as? DatabaseClient.Relational ?: return
        relational.metadata.getDatabaseSize()
            .onSuccess { _databaseSize = it }
            .onFailure { lastErrorMessage = it.message }
    }

    // ==================== 数据库管理 ====================

    suspend fun loadServerDatabases(): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            // Redis 等非关系型无 listDatabases，必须清空列表，否则会一直显示上一 JDBC 实例的库名
            _serverDatabases = emptyList()
            _currentDatabaseName = null
            return false
        }
        return relational.metadata.listDatabases()
            .onSuccess { databases ->
                _serverDatabases = databases
                loadCurrentDatabaseName()
            }
            .onFailure { lastErrorMessage = it.message }
            .isSuccess
    }

    suspend fun switchServerDatabase(databaseName: String): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持多库管理"
            return false
        }
        return try {
            // 清空当前表详情，避免切库后仍显示旧表元数据
            _tableSchema = null
            _indexes = emptyList()
            _tableStats = null
            _currentTableData = null
            val result = relational.metadata.switchDatabase(databaseName)
            result.onSuccess {
                _currentDatabaseName = databaseName
                loadTables()
            }.onFailure {
                lastErrorMessage = it.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        }
    }

    private suspend fun loadCurrentDatabaseName() {
        val relational = client as? DatabaseClient.Relational ?: return
        relational.metadata.getCurrentDatabase()
            .onSuccess { _currentDatabaseName = it }
            .onFailure { lastErrorMessage = it.message }
    }

    suspend fun createDatabase(name: String, charset: String? = null): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持创建数据库"
            return false
        }
        lastErrorMessage = null
        return try {
            val result = relational.metadata.createDatabase(name, charset)
            result.onSuccess {
                loadServerDatabases()
            }.onFailure {
                lastErrorMessage = it.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        }
    }

    suspend fun dropDatabase(name: String): Boolean {
        val relational = client as? DatabaseClient.Relational ?: run {
            lastErrorMessage = "未连接或当前数据库不支持删除数据库"
            return false
        }
        lastErrorMessage = null
        return try {
            val result = relational.metadata.dropDatabase(name)
            result.onSuccess {
                loadServerDatabases()
                if (_currentDatabaseName == name) {
                    _currentDatabaseName = null
                }
            }.onFailure {
                lastErrorMessage = it.message
            }
            result.isSuccess
        } catch (e: Exception) {
            lastErrorMessage = e.message
            false
        }
    }
}
