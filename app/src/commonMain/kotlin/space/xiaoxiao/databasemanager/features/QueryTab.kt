package space.xiaoxiao.databasemanager.features

import androidx.compose.runtime.Immutable
import space.xiaoxiao.databasemanager.core.QueryResult
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.getString
import java.util.UUID

enum class TransactionMode { AUTO, MANUAL }

enum class TransactionIsolationLevel(val value: Int, val displayNameKey: String) {
    READ_UNCOMMITTED(1, "isolation_read_uncommitted"),
    READ_COMMITTED(2, "isolation_read_committed"),
    REPEATABLE_READ(3, "isolation_repeatable_read"),
    SERIALIZABLE(4, "isolation_serializable");
    companion object { fun fromValue(value: Int) = entries.find { it.value == value } ?: READ_COMMITTED }
}

@Immutable
data class QueryTab(
    val id: String = UUID.randomUUID().toString(),
    val sessionName: String = "",
    val sql: String = "",
    val selectedText: String = "",
    val databaseId: String? = null,
    val databaseConfig: DatabaseConfigInfo? = null,
    val connectionState: ConnectionUiState = ConnectionUiState.DISCONNECTED,
    val queryResult: QueryResult? = null,
    val errorMessage: String? = null,
    val transactionMode: TransactionMode = TransactionMode.AUTO,
    val transactionIsolationLevel: TransactionIsolationLevel = TransactionIsolationLevel.READ_COMMITTED,
    val isInTransaction: Boolean = false,
    val isResultExpanded: Boolean = false,
    val autoExpandResult: Boolean = true
) {
    fun getDisplayTitle(language: Language = Language.CHINESE): String {
        val dbName = databaseConfig?.name ?: getString("disconnected", language)
        return if (sessionName.isNotBlank()) {
            "$dbName · $sessionName"
        } else {
            dbName
        }
    }
}