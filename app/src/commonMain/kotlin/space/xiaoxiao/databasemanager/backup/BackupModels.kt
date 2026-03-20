package space.xiaoxiao.databasemanager.backup

import kotlinx.serialization.Serializable
import space.xiaoxiao.databasemanager.storage.AiConfig
import space.xiaoxiao.databasemanager.storage.SerializableAppConfig
import space.xiaoxiao.databasemanager.storage.SerializableDatabaseConfig
import space.xiaoxiao.databasemanager.storage.SerializableQueryHistoryItem
import space.xiaoxiao.databasemanager.features.SerializableQuerySession

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val exportedAtEpochMillis: Long,
    val appConfig: SerializableAppConfig,
    val databaseConfigs: List<SerializableDatabaseConfig>,
    val aiConfig: AiConfig?,
    val queryHistory: List<SerializableQueryHistoryItem>,
    val querySessions: List<SerializableQuerySession>
)

@Serializable
data class EncryptedBackupFile(
    val format: String = "dbmconf",
    val version: Int = 1,
    val kdf: KdfParams,
    val cipher: CipherParams,
    val ciphertextBase64: String
)

@Serializable
data class KdfParams(
    val name: String = "PBKDF2-HMAC-SHA256",
    val iterations: Int,
    val saltBase64: String
)

@Serializable
data class CipherParams(
    val name: String = "AES-256-GCM",
    val ivBase64: String,
    val tagBits: Int = 128
)

