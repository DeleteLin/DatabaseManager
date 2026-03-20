package space.xiaoxiao.databasemanager.features

import space.xiaoxiao.databasemanager.storage.SecureStorage
import space.xiaoxiao.databasemanager.storage.SerializableQueryHistoryItem
import space.xiaoxiao.databasemanager.storage.ConfigSerializer

/**
 * Android 平台查询历史存储实现
 */
actual class QueryHistoryStorage private constructor(
    private val secureStorage: SecureStorage
) {
    private val HISTORY_KEY = "query_history_json"

    actual suspend fun addHistory(item: QueryHistoryItem) {
        val history = getHistory().toMutableList()
        history.add(0, item)
        saveHistory(history.take(50))
    }

    actual suspend fun getHistory(limit: Int): List<QueryHistoryItem> {
        val jsonStr = secureStorage.getString(HISTORY_KEY) ?: return emptyList()

        return try {
            val serializableItems = ConfigSerializer.deserializeQueryHistory(jsonStr)
            serializableItems.take(limit).map { SerializableQueryHistoryItem.toQueryHistoryItem(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual suspend fun deleteHistory(id: String) {
        val history = getHistory().filter { it.id != id }
        saveHistory(history)
    }

    actual suspend fun clearHistory() {
        secureStorage.remove(HISTORY_KEY)
    }

    actual suspend fun replaceAll(items: List<QueryHistoryItem>) {
        saveHistory(items.take(50))
    }

    private fun saveHistory(history: List<QueryHistoryItem>) {
        val serializableItems = history.map { SerializableQueryHistoryItem.fromQueryHistoryItem(it) }
        val jsonStr = ConfigSerializer.serializeQueryHistory(serializableItems)
        secureStorage.setString(HISTORY_KEY, jsonStr)
    }

    companion object {
        fun create(secureStorage: SecureStorage): QueryHistoryStorage {
            return QueryHistoryStorage(secureStorage)
        }
    }
}
