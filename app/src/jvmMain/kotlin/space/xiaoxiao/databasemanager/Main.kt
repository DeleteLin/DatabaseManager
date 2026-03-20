package space.xiaoxiao.databasemanager

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.remember
import space.xiaoxiao.databasemanager.config.AppConfigStorage
import space.xiaoxiao.databasemanager.features.DatabaseConfigStorage
import space.xiaoxiao.databasemanager.features.QueryHistoryStorage
import space.xiaoxiao.databasemanager.features.QuerySessionStorage
import space.xiaoxiao.databasemanager.storage.AiConfigStorage
import space.xiaoxiao.databasemanager.storage.JvmEncryptionManager
import space.xiaoxiao.databasemanager.storage.JvmSecureStorage

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "小小数据库"
    ) {
        val themeState = remember { space.xiaoxiao.databasemanager.theme.ThemeState() }
        val localizationState = remember { space.xiaoxiao.databasemanager.i18n.LocalizationState() }

        // 初始化安全存储
        val secureStorage = remember { JvmSecureStorage() }
        val encryptionManager = remember { JvmEncryptionManager() }

        // 初始化存储类
        val appConfigStorage = remember { AppConfigStorage.create(secureStorage) }
        val databaseConfigStorage = remember { DatabaseConfigStorage.create(secureStorage, encryptionManager) }
        val queryHistoryStorage = remember { QueryHistoryStorage.create(secureStorage) }
        val querySessionStorage = remember { QuerySessionStorage.create(secureStorage) }
        val aiConfigStorage = remember { AiConfigStorage(secureStorage, encryptionManager) }

        App(
            themeState = themeState,
            localizationState = localizationState,
            appConfigStorage = appConfigStorage,
            databaseConfigStorage = databaseConfigStorage,
            queryHistoryStorage = queryHistoryStorage,
            querySessionStorage = querySessionStorage,
            aiConfigStorage = aiConfigStorage
        )
    }
}
