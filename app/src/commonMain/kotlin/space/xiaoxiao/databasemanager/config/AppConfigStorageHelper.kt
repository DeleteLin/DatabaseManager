package space.xiaoxiao.databasemanager.config

import space.xiaoxiao.databasemanager.storage.SecureStorage
import space.xiaoxiao.databasemanager.storage.SerializableAppConfig
import space.xiaoxiao.databasemanager.storage.ConfigSerializer
import space.xiaoxiao.databasemanager.charts.SerializableChartPanel

internal class AppConfigStorageHelper(
    private val secureStorage: SecureStorage
) {
    private val CONFIG_KEY = "app_config_json"

    fun loadConfig(): AppConfig {
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

    fun saveConfig(config: AppConfig) {
        val serializableConfig = SerializableAppConfig.fromAppConfig(config)
        val jsonStr = ConfigSerializer.serializeAppConfig(serializableConfig)
        secureStorage.setString(CONFIG_KEY, jsonStr)
    }

    fun resetToDefault() {
        secureStorage.remove(CONFIG_KEY)
    }

    fun updateColorTheme(colorTheme: String) {
        val config = loadConfig()
        val newConfig = config.copy(colorTheme = colorTheme)
        saveConfig(newConfig)
    }

    fun updateLanguage(lang: String) {
        val config = loadConfig()
        val newConfig = config.copy(language = lang)
        saveConfig(newConfig)
    }

    fun updateSelectedDatabase(id: String?) {
        val config = loadConfig()
        val newConfig = config.copy(selectedDatabaseId = id)
        saveConfig(newConfig)
    }

    fun updateQueryTabs(tabs: List<space.xiaoxiao.databasemanager.storage.SerializableQuerySessionLite>, selectedTabId: String?) {
        val config = loadConfig()
        val newConfig = config.copy(openQueryTabs = tabs, lastSelectedQueryTabId = selectedTabId)
        saveConfig(newConfig)
    }

    fun clearQueryTabs() {
        val config = loadConfig()
        val newConfig = config.copy(openQueryTabs = emptyList(), lastSelectedQueryTabId = null)
        saveConfig(newConfig)
    }

    fun updateChartPanels(panels: List<SerializableChartPanel>, selectedPanelId: String?) {
        val config = loadConfig()
        val newConfig = config.copy(chartPanels = panels, selectedChartPanelId = selectedPanelId)
        saveConfig(newConfig)
    }

    companion object {
        fun create(secureStorage: SecureStorage): AppConfigStorageHelper {
            return AppConfigStorageHelper(secureStorage)
        }
    }
}
