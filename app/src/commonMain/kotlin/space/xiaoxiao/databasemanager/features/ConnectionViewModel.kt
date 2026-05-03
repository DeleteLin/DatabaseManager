package space.xiaoxiao.databasemanager.features

import androidx.compose.runtime.*
import space.xiaoxiao.databasemanager.core.*

/**
 * Manages the database connection lifecycle and connection-level state.
 *
 * Owns the [DatabaseClient] instance, [DatabaseConfigInfo] reference,
 * and exposes connection status, database type, error messages, etc.
 *
 * Internal properties are accessible by sibling ViewModels in the same package
 * (QueryViewModel, BrowserViewModel, SchemaEditorViewModel).
 */
class ConnectionViewModel {

    // ── Internal: shared state (exposed to sibling VMs) ──

    internal var client: DatabaseClient? = null
    internal var currentConfig: DatabaseConfigInfo? = null

    // ── Public connection state ──

    var connectionState by mutableStateOf(ConnectionUiState.DISCONNECTED)
        private set

    /** Unified operation state – tracks loading / success / error flows. */
    var operationState by mutableStateOf<UiState<Nothing>>(UiState.Idle)
        internal set

    var lastErrorMessage by mutableStateOf<String?>(null)
        internal set

    var databaseType by mutableStateOf(DatabaseType.MYSQL)
        internal set

    var supportedFeatures by mutableStateOf(emptySet<DatabaseFeature>())
        internal set

    /** Current transaction status (true = inside active transaction). */
    var isInTransaction by mutableStateOf(false)
        internal set

    /**
     * The config-id backing the current connection.
     * Used by callers to avoid redundant disconnect/connect cycles.
     */
    val currentConfigId: String? get() = currentConfig?.id

    // ── Public connection lifecycle ──

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

    /**
     * Disconnects the current client and resets connection-level state.
     * Does NOT clear query results or browser metadata – the coordinator
     * handles those cross-cutting concerns in its own [DatabaseViewModel.disconnect].
     */
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
    }

    /** Closes the connection – convenience alias for [disconnect]. */
    suspend fun close() {
        disconnect()
    }

    /** Clears the error banner and resets [operationState] to idle. */
    fun clearError() {
        lastErrorMessage = null
        operationState = UiState.Idle
    }

    /** Resets only [operationState] to idle (keep last error). */
    fun clearOperationState() {
        operationState = UiState.Idle
    }
}
