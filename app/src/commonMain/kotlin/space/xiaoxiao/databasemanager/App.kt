package space.xiaoxiao.databasemanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import space.xiaoxiao.databasemanager.config.AppConfigStorage
import space.xiaoxiao.databasemanager.features.DatabaseConfigStorage
import space.xiaoxiao.databasemanager.features.QueryHistoryStorage
import space.xiaoxiao.databasemanager.features.QuerySessionStorage
import space.xiaoxiao.databasemanager.features.SplashScreen
import space.xiaoxiao.databasemanager.i18n.LocalizationState
import space.xiaoxiao.databasemanager.theme.DatabaseManagerTheme
import space.xiaoxiao.databasemanager.theme.ThemeState

/**
 * App 主入口
 *
 * 负责：
 * 1. 应用主题配置
 * 2. 导航状态管理
 * 3. 依赖注入到导航层
 */
@Composable
fun App(
    themeState: ThemeState,
    localizationState: LocalizationState,
    appConfigStorage: AppConfigStorage,
    databaseConfigStorage: DatabaseConfigStorage,
    queryHistoryStorage: QueryHistoryStorage,
    querySessionStorage: QuerySessionStorage,
    aiConfigStorage: space.xiaoxiao.databasemanager.storage.AiConfigStorage
) {
    val colorTheme by themeState.colorTheme.collectAsState()

    DatabaseManagerTheme(colorTheme = colorTheme) {
        val navigationState = rememberAppNavigationState()
        val language by localizationState.language.collectAsState()

        // 启动状态管理
        if (!navigationState.isAppReady) {
            // 显示启动屏
            SplashScreen(
                language = language,
                onNavigateToMain = {
                    navigationState.markAppAsReady(true)
                }
            )
        } else {
            // 显示主应用
            AppNavigation(
                navigationState = navigationState,
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
}