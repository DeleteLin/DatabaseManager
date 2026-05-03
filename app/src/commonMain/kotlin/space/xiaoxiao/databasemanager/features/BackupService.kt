package space.xiaoxiao.databasemanager.features

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import space.xiaoxiao.databasemanager.backup.BackupCrypto
import space.xiaoxiao.databasemanager.backup.BackupPayload
import space.xiaoxiao.databasemanager.config.AppConfigStorage
import space.xiaoxiao.databasemanager.core.DatabaseType
import space.xiaoxiao.databasemanager.storage.AiConfigStorage
import space.xiaoxiao.databasemanager.storage.ConfigSerializer
import space.xiaoxiao.databasemanager.storage.SerializableAppConfig
import space.xiaoxiao.databasemanager.storage.SerializableDatabaseConfig
import space.xiaoxiao.databasemanager.storage.SerializableQueryHistoryItem
import space.xiaoxiao.databasemanager.utils.AppExit
import space.xiaoxiao.databasemanager.utils.FileUtils

class BackupService(
    private val appConfigStorage: AppConfigStorage,
    private val databaseConfigStorage: DatabaseConfigStorage,
    private val aiConfigStorage: AiConfigStorage,
    private val queryHistoryStorage: QueryHistoryStorage,
    private val querySessionStorage: QuerySessionStorage
) {
    fun exportConfig(password: String): Result<Unit> = runBlocking {
        runCatching {
            val payload = BackupPayload(
                exportedAtEpochMillis = System.currentTimeMillis(),
                appConfig = SerializableAppConfig.fromAppConfig(appConfigStorage.loadConfig()),
                databaseConfigs = databaseConfigStorage.loadConfigs().map { db ->
                    SerializableDatabaseConfig(id = db.id, name = db.name, type = db.type.name, host = db.host, port = db.port, database = db.database, username = db.username, plainPassword = db.password)
                },
                aiConfig = aiConfigStorage.loadConfig(),
                queryHistory = queryHistoryStorage.getHistory(50).map { SerializableQueryHistoryItem.fromQueryHistoryItem(it) },
                querySessions = querySessionStorage.loadSessions().map { SerializableQuerySession.fromQuerySession(it) }
            )
            val plaintext = ConfigSerializer.json.encodeToString(BackupPayload.serializer(), payload)
            val encryptedFileJson = BackupCrypto.encryptToFileJson(plaintext, password)
            val ok = FileUtils.saveFile(encryptedFileJson, defaultName = "dbm-config-backup", extension = "dbmconf")
            if (!ok) error("Save failed")
        }
    }

    fun importConfig(password: String, fileContent: String): Result<Unit> = runBlocking {
        runCatching {
            val plaintext = BackupCrypto.decryptFromFileJson(fileContent, password)
            val payload = ConfigSerializer.json.decodeFromString(BackupPayload.serializer(), plaintext)
            databaseConfigStorage.saveConfigs(emptyList())
            appConfigStorage.resetToDefault()
            aiConfigStorage.deleteConfig()
            queryHistoryStorage.clearHistory()
            querySessionStorage.clearSessions()
            val restoredDb = payload.databaseConfigs.map { cfg ->
                DatabaseConfigInfo(id = cfg.id, name = cfg.name, type = DatabaseType.valueOf(cfg.type), host = cfg.host, port = cfg.port, database = cfg.database, username = cfg.username, password = cfg.plainPassword ?: (cfg.encryptedPassword ?: ""), charset = null)
            }
            databaseConfigStorage.saveConfigs(restoredDb)
            appConfigStorage.saveConfig(SerializableAppConfig.toAppConfig(payload.appConfig))
            payload.aiConfig?.let { aiConfigStorage.saveConfig(it) }
            queryHistoryStorage.replaceAll(payload.queryHistory.map { SerializableQueryHistoryItem.toQueryHistoryItem(it) })
            payload.querySessions.forEach { querySessionStorage.saveSession(SerializableQuerySession.toQuerySession(it)) }
            AppExit.exitApp()
        }
    }

    fun clearConfigAndExit() = runBlocking {
        databaseConfigStorage.saveConfigs(emptyList())
        appConfigStorage.resetToDefault()
        aiConfigStorage.deleteConfig()
        queryHistoryStorage.clearHistory()
        querySessionStorage.clearSessions()
        AppExit.exitApp()
    }
}
