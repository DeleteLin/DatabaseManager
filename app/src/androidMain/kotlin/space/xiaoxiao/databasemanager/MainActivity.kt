package space.xiaoxiao.databasemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import space.xiaoxiao.databasemanager.config.AppConfigStorage
import space.xiaoxiao.databasemanager.features.DatabaseConfigStorage
import space.xiaoxiao.databasemanager.features.QueryHistoryStorage
import space.xiaoxiao.databasemanager.features.QuerySessionStorage
import space.xiaoxiao.databasemanager.i18n.LocalizationState
import space.xiaoxiao.databasemanager.storage.AiConfigStorage
import space.xiaoxiao.databasemanager.storage.AndroidEncryptionManager
import space.xiaoxiao.databasemanager.storage.AndroidSecureStorage
import space.xiaoxiao.databasemanager.theme.ThemeState
import space.xiaoxiao.databasemanager.utils.FileUtils
import space.xiaoxiao.databasemanager.utils.AppExit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 FileUtils
        FileUtils.init(this)
        AppExit.init(this)

        setContent {
            val themeState = remember { ThemeState() }
            val localizationState = remember { LocalizationState() }

            // 初始化安全存储
            val secureStorage = remember { AndroidSecureStorage(this) }
            val encryptionManager = remember { AndroidEncryptionManager() }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        FileUtils.onActivityResult(requestCode, resultCode, data)
    }
}
