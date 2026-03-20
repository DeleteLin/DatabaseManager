package space.xiaoxiao.databasemanager.features

import space.xiaoxiao.databasemanager.core.QueryResult
import java.util.UUID

enum class TransactionMode { AUTO, MANUAL }

enum class TransactionIsolationLevel(val value: Int, val displayNameKey: String) {
    READ_UNCOMMITTED(1, "isolation_read_uncommitted"),
    READ_COMMITTED(2, "isolation_read_committed"),
    REPEATABLE_READ(3, "isolation_repeatable_read"),
    SERIALIZABLE(4, "isolation_serializable");
    companion object { fun fromValue(value: Int) = entries.find { it.value == value } ?: READ_COMMITTED }
}

data class QueryTab(
    val id: String = UUID.randomUUID().toString(),
    var sessionName: String = "",
    var sql: String = "",
    var selectedText: String = "",
    var databaseId: String? = null,
    var databaseConfig: DatabaseConfigInfo? = null,
    var connectionState: ConnectionUiState = ConnectionUiState.DISCONNECTED,
    var queryResult: QueryResult? = null,
    var errorMessage: String? = null,
    var transactionMode: TransactionMode = TransactionMode.AUTO,
    var transactionIsolationLevel: TransactionIsolationLevel = TransactionIsolationLevel.READ_COMMITTED,
    var isInTransaction: Boolean = false,
    var isResultExpanded: Boolean = false,
    var autoExpandResult: Boolean = true
) {
    fun getDisplayTitle(): String {
        val dbName = databaseConfig?.name ?: "未连接"
        return if (sessionName.isNotBlank()) {
            "$dbName · $sessionName"
        } else {
            dbName
        }
    }
}