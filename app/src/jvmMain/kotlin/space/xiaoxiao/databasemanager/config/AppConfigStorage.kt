package space.xiaoxiao.databasemanager.config

import space.xiaoxiao.databasemanager.storage.SecureStorage
import space.xiaoxiao.databasemanager.storage.SerializableQuerySessionLite
import space.xiaoxiao.databasemanager.charts.SerializableChartPanel

/**
 * JVM 平台应用配置存储实现
 */
actual class AppConfigStorage private constructor(
    private val helper: AppConfigStorageHelper
) {
    actual fun loadConfig(): AppConfig = helper.loadConfig()

    actual fun saveConfig(config: AppConfig) = helper.saveConfig(config)

    actual fun resetToDefault() = helper.resetToDefault()

    actual fun updateColorTheme(colorTheme: String) = helper.updateColorTheme(colorTheme)

    actual fun updateLanguage(lang: String) = helper.updateLanguage(lang)

    actual fun updateSelectedDatabase(id: String?) = helper.updateSelectedDatabase(id)

    actual fun updateQueryTabs(tabs: List<SerializableQuerySessionLite>, selectedTabId: String?) =
        helper.updateQueryTabs(tabs, selectedTabId)

    actual fun clearQueryTabs() = helper.clearQueryTabs()

    actual fun updateChartPanels(panels: List<SerializableChartPanel>, selectedPanelId: String?) =
        helper.updateChartPanels(panels, selectedPanelId)

    companion object {
        fun create(secureStorage: SecureStorage): AppConfigStorage {
            return AppConfigStorage(AppConfigStorageHelper.create(secureStorage))
        }
    }
}
