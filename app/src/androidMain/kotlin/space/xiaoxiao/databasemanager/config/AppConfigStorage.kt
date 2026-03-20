package space.xiaoxiao.databasemanager.config

import space.xiaoxiao.databasemanager.storage.SecureStorage
import space.xiaoxiao.databasemanager.storage.SerializableAppConfig
import space.xiaoxiao.databasemanager.storage.ConfigSerializer
import space.xiaoxiao.databasemanager.charts.SerializableChartPanel

/**
 * Android 平台应用配置存储实现
 */
actual class AppConfigStorage private constructor(
    private val secureStorage: SecureStorage
) {
    private val CONFIG_KEY = "app_config_json"

    actual fun loadConfig(): AppConfig {
        val jsonStr = secureStorage.getString(CONFIG_KEY)
        return if (jsonStr != null) {
            try {
                val serializableConfig = ConfigSerializer.deserializeAppConfig(jsonStr)
                SerializableAppConfig.toAppConfig(serializableConfig)
            } catch (e: Exception) {
                AppConfig()
            }
        } else {
            AppConfig()
        }
    }

    actual fun saveConfig(config: AppConfig) {
        val serializableConfig = SerializableAppConfig.fromAppConfig(config)
        val jsonStr = ConfigSerializer.serializeAppConfig(serializableConfig)
        secureStorage.setString(CONFIG_KEY, jsonStr)
    }

    actual fun resetToDefault() {
        secureStorage.remove(CONFIG_KEY)
    }

    actual fun updateColorTheme(colorTheme: String) {
        val config = loadConfig()
        val newConfig = config.copy(colorTheme = colorTheme)
        saveConfig(newConfig)
    }

    actual fun updateLanguage(lang: String) {
        val config = loadConfig()
        val newConfig = config.copy(language = lang)
        saveConfig(newConfig)
    }

    actual fun updateSelectedDatabase(id: String?) {
        val config = loadConfig()
        val newConfig = config.copy(selectedDatabaseId = id)
        saveConfig(newConfig)
    }

    actual fun updateQueryTabs(tabs: List<space.xiaoxiao.databasemanager.storage.SerializableQuerySessionLite>, selectedTabId: String?) {
        val config = loadConfig()
        val newConfig = config.copy(openQueryTabs = tabs, lastSelectedQueryTabId = selectedTabId)
        saveConfig(newConfig)
    }

    actual fun clearQueryTabs() {
        val config = loadConfig()
        val newConfig = config.copy(openQueryTabs = emptyList(), lastSelectedQueryTabId = null)
        saveConfig(newConfig)
    }

    actual fun updateChartPanels(panels: List<SerializableChartPanel>, selectedPanelId: String?) {
        val config = loadConfig()
        val newConfig = config.copy(chartPanels = panels, selectedChartPanelId = selectedPanelId)
        saveConfig(newConfig)
    }

    companion object {
        fun create(secureStorage: SecureStorage): AppConfigStorage {
            return AppConfigStorage(secureStorage)
        }
    }
}