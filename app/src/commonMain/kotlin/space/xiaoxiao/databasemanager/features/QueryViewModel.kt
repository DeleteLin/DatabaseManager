package space.xiaoxiao.databasemanager.features

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import space.xiaoxiao.databasemanager.core.*

/**
 * Handles query execution, result tracking, query history, and
 * transaction management.
 *
 * Receives a reference to [ConnectionViewModel] so it can access the
 * shared [DatabaseClient] and error/operation state.
 */
class QueryViewModel(
    private val conn: ConnectionViewModel,
    private val historyStorage: QueryHistoryStorage? = null
) {

    // ── Query execution state ──

    var isExecuting by mutableStateOf(false)
        private set

    var lastQueryResult by mutableStateOf<QueryResult?>(null)
        internal set

    var lastUpdateResult by mutableStateOf<UpdateResult?>(null)
        internal set

    // ── Transaction state ──

    var transactionMode by mutableStateOf(TransactionMode.AUTO)
        private set

    var transactionIsolationLevel by mutableStateOf(TransactionIsolationLevel.READ_COMMITTED)
        private set

    // ── History ──

    private var _historyItems by mutableStateOf(emptyList<QueryHistoryItem>())
    val historyItems: List<QueryHistoryItem> get() = _historyItems

    // ── Query execution ──

    /**
     * Executes a SQL command or Redis command against the current connection.
     * Updates [lastQueryResult] / [lastUpdateResult] accordingly.
     * Returns true on success.
     */
    suspend fun executeCommand(command: String): Boolean {
        val currentClient = conn.client ?: run {
            conn.lastErrorMessage = "未连接"
            conn.operationState = UiState.error("未连接", errorCode = ErrorCode.CONNECTION_FAILED)
            return false
        }
        isExecuting = true
        conn.operationState = UiState.Loading
        conn.lastErrorMessage = null
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
                syncTransactionState(command)
                conn.operationState = UiState.Idle
            }.onFailure {
                conn.lastErrorMessage = it.message
                conn.operationState = UiState.error(it.message ?: "执行失败", it, ErrorCode.QUERY_FAILED)
            }
            result.isSuccess
        } catch (e: Exception) {
            conn.lastErrorMessage = e.message
            conn.operationState = UiState.error(e.message ?: "执行失败", e)
            false
        } finally {
            isExecuting = false
        }
    }

    // ── Transaction management ──

    fun updateTransactionMode(mode: TransactionMode) {
        transactionMode = mode
    }

    suspend fun setTransactionIsolationLevel(level: TransactionIsolationLevel) {
        val relational = conn.client as? DatabaseClient.Relational ?: return
        relational.metadata.setTransactionIsolation(level.value)
            .onSuccess { transactionIsolationLevel = level }
            .onFailure { conn.lastErrorMessage = it.message }
    }

    suspend fun beginTransaction(): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持事务"
            return false
        }
        return relational.metadata.beginTransaction()
            .onSuccess { conn.isInTransaction = true }
            .isSuccess
    }

    suspend fun commitTransaction(): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持事务"
            return false
        }
        return relational.metadata.commitTransaction()
            .onSuccess { conn.isInTransaction = false }
            .isSuccess
    }

    suspend fun rollbackTransaction(): Boolean {
        val relational = conn.client as? DatabaseClient.Relational ?: run {
            conn.lastErrorMessage = "未连接或当前数据库不支持事务"
            return false
        }
        return relational.metadata.rollbackTransaction()
            .onSuccess { conn.isInTransaction = false }
            .isSuccess
    }

    // ── Query history ──

    suspend fun loadHistory() {
        historyStorage?.let { _historyItems = it.getHistory(50) }
    }

    fun saveQueryHistory(
        scope: CoroutineScope,
        sql: String,
        rowCount: Int,
        executionTimeMs: Long,
        isSuccess: Boolean
    ) {
        conn.currentConfig?.let { config ->
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

    // ── Internal helpers ──

    /** Clears execution results (called on disconnect). */
    internal fun clearResults() {
        lastQueryResult = null
    }

    /**
     * Detects transaction-related SQL statements and synchronises
     * [ConnectionViewModel.isInTransaction] accordingly.
     */
    private fun syncTransactionState(command: String) {
        val upperCommand = command.trim().uppercase()
        if (upperCommand.startsWith("BEGIN") ||
            upperCommand.startsWith("START TRANSACTION") ||
            upperCommand.startsWith("SET AUTOCOMMIT=0")
        ) {
            conn.isInTransaction = true
        } else if (upperCommand.startsWith("COMMIT") ||
            upperCommand.startsWith("ROLLBACK")
        ) {
            conn.isInTransaction = false
        } else if (upperCommand.startsWith("SET AUTOCOMMIT=1")) {
            conn.isInTransaction = false
        }
    }
}
