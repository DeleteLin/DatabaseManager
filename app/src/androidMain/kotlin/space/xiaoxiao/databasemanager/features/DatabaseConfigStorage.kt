package space.xiaoxiao.databasemanager.features

import space.xiaoxiao.databasemanager.storage.SecureStorage
import space.xiaoxiao.databasemanager.storage.EncryptionManager

/**
 * Android 平台数据库配置存储实现
 */
actual class DatabaseConfigStorage private constructor(
    private val helper: DatabaseConfigStorageHelper
) {
    actual fun loadConfigs(): List<DatabaseConfigInfo> = helper.loadConfigs()

    actual fun saveConfigs(configs: List<DatabaseConfigInfo>) = helper.saveConfigs(configs)

    actual fun addConfig(config: DatabaseConfigInfo) = helper.addConfig(config)

    actual fun removeConfig(id: String) = helper.removeConfig(id)

    actual fun updateConfig(config: DatabaseConfigInfo) = helper.updateConfig(config)

    companion object {
        fun create(secureStorage: SecureStorage, encryptionManager: EncryptionManager): DatabaseConfigStorage {
            return DatabaseConfigStorage(DatabaseConfigStorageHelper.create(secureStorage, encryptionManager))
        }
    }
}
