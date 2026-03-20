package space.xiaoxiao.databasemanager.features

import kotlinx.serialization.Serializable
import space.xiaoxiao.databasemanager.storage.ConfigSerializer
import space.xiaoxiao.databasemanager.storage.SerializableDatabaseConfig

/**
 * 可序列化的查询会话
 */
@Serializable
data class SerializableQuerySession(
    val id: String,
    val sessionName: String,
    val sql: String,
    val databaseId: String?,
    val databaseConfig: SerializableDatabaseConfig?,
    val transactionMode: String,
    val transactionIsolationLevel: String,
    val isResultExpanded: Boolean,
    val autoExpandResult: Boolean,
    val timestamp: Long
) {
    companion object {
        fun fromQuerySession(session: QuerySession): SerializableQuerySession {
            return SerializableQuerySession(
                id = session.id,
                sessionName = session.sessionName,
                sql = session.sql,
                databaseId = session.databaseId,
                databaseConfig = session.databaseConfig?.let { SerializableDatabaseConfig.fromDatabaseConfig(it) },
                transactionMode = session.transactionMode.name,
                transactionIsolationLevel = session.transactionIsolationLevel.name,
                isResultExpanded = session.isResultExpanded,
                autoExpandResult = session.autoExpandResult,
                timestamp = session.timestamp
            )
        }

        fun toQuerySession(session: SerializableQuerySession): QuerySession {
            return QuerySession(
                id = session.id,
                sessionName = session.sessionName,
                sql = session.sql,
                databaseId = session.databaseId,
                databaseConfig = session.databaseConfig?.let { config ->
                    SerializableDatabaseConfig.toDatabaseConfig(
                        config,
                        config.plainPassword ?: config.encryptedPassword ?: ""
                    )
                },
                transactionMode = TransactionMode.valueOf(session.transactionMode),
                transactionIsolationLevel = TransactionIsolationLevel.valueOf(session.transactionIsolationLevel),
                isResultExpanded = session.isResultExpanded,
                autoExpandResult = session.autoExpandResult,
                timestamp = session.timestamp
            )
        }
    }
}

/**
 * 查询会话
 */
data class QuerySession(
    val id: String,
    val sessionName: String,
    val sql: String,
    val databaseId: String?,
    val databaseConfig: DatabaseConfigInfo?,
    val transactionMode: TransactionMode,
    val transactionIsolationLevel: TransactionIsolationLevel,
    val isResultExpanded: Boolean,
    val autoExpandResult: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 查询会话持久化接口
 */
expect class QuerySessionStorage {
    suspend fun saveSession(session: QuerySession)
    suspend fun loadSessions(): List<QuerySession>
    suspend fun deleteSession(id: String)
    suspend fun clearSessions()
}
