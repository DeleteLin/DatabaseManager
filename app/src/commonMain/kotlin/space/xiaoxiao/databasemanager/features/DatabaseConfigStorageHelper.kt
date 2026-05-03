package space.xiaoxiao.databasemanager.features

import space.xiaoxiao.databasemanager.storage.SecureStorage
import space.xiaoxiao.databasemanager.storage.EncryptionManager
import space.xiaoxiao.databasemanager.storage.SerializableDatabaseConfig
import space.xiaoxiao.databasemanager.storage.ConfigSerializer

internal class DatabaseConfigStorageHelper(
    private val secureStorage: SecureStorage,
    private val encryptionManager: EncryptionManager
) {
    private val CONFIGS_KEY = "database_configs_json"

    fun loadConfigs(): List<DatabaseConfigInfo> {
        val jsonStr = secureStorage.getString(CONFIGS_KEY) ?: return emptyList()

        return try {
            val serializableConfigs = ConfigSerializer.deserializeDatabaseConfigs(jsonStr)
            serializableConfigs.mapNotNull { config ->
                val encryptedPassword = config.encryptedPassword ?: return@mapNotNull null
                val password = encryptionManager.decrypt(encryptedPassword)
                SerializableDatabaseConfig.toDatabaseConfig(config, password)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveConfigs(configs: List<DatabaseConfigInfo>) {
        val serializableConfigs = configs.map { config ->
            val encryptedPassword = encryptionManager.encrypt(config.password)
            SerializableDatabaseConfig(
                id = config.id,
                name = config.name,
                type = config.type.name,
                host = config.host,
                port = config.port,
                database = config.database,
                username = config.username,
                encryptedPassword = encryptedPassword
            )
        }

        val jsonStr = ConfigSerializer.serializeDatabaseConfigs(serializableConfigs)
        secureStorage.setString(CONFIGS_KEY, jsonStr)
    }

    fun addConfig(config: DatabaseConfigInfo) {
        val configs = loadConfigs().toMutableList()
        configs.add(config)
        saveConfigs(configs)
    }

    fun removeConfig(id: String) {
        val configs = loadConfigs().filter { it.id != id }
        saveConfigs(configs)
    }

    fun updateConfig(config: DatabaseConfigInfo) {
        val configs = loadConfigs().toMutableList()
        val index = configs.indexOfFirst { it.id == config.id }
        if (index >= 0) {
            configs[index] = config
            saveConfigs(configs)
        }
    }

    companion object {
        fun create(secureStorage: SecureStorage, encryptionManager: EncryptionManager): DatabaseConfigStorageHelper {
            return DatabaseConfigStorageHelper(secureStorage, encryptionManager)
        }
    }
}
