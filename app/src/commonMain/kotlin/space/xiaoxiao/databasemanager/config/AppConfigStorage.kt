package space.xiaoxiao.databasemanager.config

import space.xiaoxiao.databasemanager.storage.SerializableQuerySessionLite
import space.xiaoxiao.databasemanager.charts.SerializableChartPanel

expect class AppConfigStorage {
    fun loadConfig(): AppConfig
    fun saveConfig(config: AppConfig)
    fun resetToDefault()
    fun updateColorTheme(colorTheme: String)
    fun updateLanguage(lang: String)
    fun updateSelectedDatabase(id: String?)
    fun updateQueryTabs(tabs: List<SerializableQuerySessionLite>, selectedTabId: String?)
    fun clearQueryTabs()
    fun updateChartPanels(panels: List<SerializableChartPanel>, selectedPanelId: String?)
}