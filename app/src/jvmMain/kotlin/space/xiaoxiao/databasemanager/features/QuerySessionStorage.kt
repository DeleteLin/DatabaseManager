package space.xiaoxiao.databasemanager.features

import space.xiaoxiao.databasemanager.storage.SecureStorage
import space.xiaoxiao.databasemanager.storage.ConfigSerializer

/**
 * JVM 平台查询会话存储实现
 */
actual class QuerySessionStorage private constructor(
    private val secureStorage: SecureStorage
) {
    private val SESSIONS_KEY = "query_sessions_json"

    actual suspend fun saveSession(session: QuerySession) {
        val sessions = loadSessions().toMutableList()
        // 如果存在相同 ID 的会话，先删除
        sessions.removeAll { it.id == session.id }
        sessions.add(session)
        saveSessions(sessions)
    }

    actual suspend fun loadSessions(): List<QuerySession> {
        val jsonStr = secureStorage.getString(SESSIONS_KEY) ?: return emptyList()

        return try {
            val serializableItems = ConfigSerializer.json.decodeFromString(
                kotlinx.serialization.builtins.ListSerializer(SerializableQuerySession.serializer()),
                jsonStr
            )
            serializableItems.map { SerializableQuerySession.toQuerySession(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual suspend fun deleteSession(id: String) {
        val sessions = loadSessions().filter { it.id != id }
        saveSessions(sessions)
    }

    actual suspend fun clearSessions() {
        secureStorage.remove(SESSIONS_KEY)
    }

    private fun saveSessions(sessions: List<QuerySession>) {
        val serializableItems = sessions.map { SerializableQuerySession.fromQuerySession(it) }
        val jsonStr = ConfigSerializer.json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(SerializableQuerySession.serializer()),
            serializableItems
        )
        secureStorage.setString(SESSIONS_KEY, jsonStr)
    }

    companion object {
        fun create(secureStorage: SecureStorage): QuerySessionStorage {
            return QuerySessionStorage(secureStorage)
        }
    }
}
