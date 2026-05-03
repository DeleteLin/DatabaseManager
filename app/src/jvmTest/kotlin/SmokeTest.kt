package space.xiaoxiao.databasemanager

import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import space.xiaoxiao.databasemanager.config.AppConfigStorage
import space.xiaoxiao.databasemanager.features.BackupService
import space.xiaoxiao.databasemanager.features.DatabaseConfigStorage
import space.xiaoxiao.databasemanager.features.QueryHistoryStorage
import space.xiaoxiao.databasemanager.features.QuerySessionStorage
import space.xiaoxiao.databasemanager.i18n.LocalizationState
import space.xiaoxiao.databasemanager.storage.AiConfigStorage
import space.xiaoxiao.databasemanager.storage.JvmEncryptionManager
import space.xiaoxiao.databasemanager.storage.JvmSecureStorage
import space.xiaoxiao.databasemanager.core.registerPlatformDrivers
import space.xiaoxiao.databasemanager.theme.ThemeState

/**
 * Minimal smoke test: verify App() composable instantiates without crashing.
 */
class SmokeTest {

    @get:Rule
    val rule = createComposeRule()

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun appDoesNotCrash() {
        rule.setContent {
            val themeState = remember { ThemeState() }
            val localizationState = remember { LocalizationState() }
            val secureStorage = remember { JvmSecureStorage() }
            val encryptionManager = remember { JvmEncryptionManager() }

            val appConfigStorage = remember { AppConfigStorage.create(secureStorage) }
            val databaseConfigStorage = remember { DatabaseConfigStorage.create(secureStorage, encryptionManager) }
            val queryHistoryStorage = remember { QueryHistoryStorage.create(secureStorage) }
            val querySessionStorage = remember { QuerySessionStorage.create(secureStorage) }
            val aiConfigStorage = remember { AiConfigStorage(secureStorage, encryptionManager) }

            registerPlatformDrivers()
            val backupService = remember {
                BackupService(
                    appConfigStorage,
                    databaseConfigStorage,
                    aiConfigStorage,
                    queryHistoryStorage,
                    querySessionStorage
                )
            }

            App(
                themeState = themeState,
                localizationState = localizationState,
                appConfigStorage = appConfigStorage,
                databaseConfigStorage = databaseConfigStorage,
                queryHistoryStorage = queryHistoryStorage,
                querySessionStorage = querySessionStorage,
                aiConfigStorage = aiConfigStorage,
                backupService = backupService
            )
        }
        // If setContent completes without throwing, App() instantiated successfully
    }
}
