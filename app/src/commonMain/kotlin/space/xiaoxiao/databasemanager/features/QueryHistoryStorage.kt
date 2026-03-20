package space.xiaoxiao.databasemanager.features

/**
 * 查询历史项
 */
data class QueryHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sql: String,
    val databaseId: String,
    val databaseName: String,
    val rowCount: Int,
    val executionTimeMs: Long,
    val isSuccess: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

expect class QueryHistoryStorage {
    suspend fun addHistory(item: QueryHistoryItem)
    suspend fun getHistory(limit: Int = 50): List<QueryHistoryItem>
    suspend fun deleteHistory(id: String)
    suspend fun clearHistory()
    suspend fun replaceAll(items: List<QueryHistoryItem>)
}
