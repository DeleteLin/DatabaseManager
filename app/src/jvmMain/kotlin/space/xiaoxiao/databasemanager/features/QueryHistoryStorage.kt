package space.xiaoxiao.databasemanager.features

import space.xiaoxiao.databasemanager.storage.SecureStorage

/**
 * JVM 平台查询历史存储实现
 */
actual class QueryHistoryStorage private constructor(
    private val helper: QueryHistoryStorageHelper
) {
    actual suspend fun addHistory(item: QueryHistoryItem) = helper.addHistory(item)

    actual suspend fun getHistory(limit: Int): List<QueryHistoryItem> = helper.getHistory(limit)

    actual suspend fun deleteHistory(id: String) = helper.deleteHistory(id)

    actual suspend fun clearHistory() = helper.clearHistory()

    actual suspend fun replaceAll(items: List<QueryHistoryItem>) = helper.replaceAll(items)

    companion object {
        fun create(secureStorage: SecureStorage): QueryHistoryStorage {
            return QueryHistoryStorage(QueryHistoryStorageHelper.create(secureStorage))
        }
    }
}
