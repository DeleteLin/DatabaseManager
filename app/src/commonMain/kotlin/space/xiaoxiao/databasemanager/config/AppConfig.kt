package space.xiaoxiao.databasemanager.config

import space.xiaoxiao.databasemanager.storage.SerializableQuerySessionLite
import space.xiaoxiao.databasemanager.charts.SerializableChartPanel

data class AppConfig(
    val colorTheme: String? = null,
    val language: String? = null,
    val selectedDatabaseId: String? = null,
    val openQueryTabs: List<SerializableQuerySessionLite> = emptyList(),
    val lastSelectedQueryTabId: String? = null,
    val chartPanels: List<SerializableChartPanel> = emptyList(),
    val selectedChartPanelId: String? = null,
    val isFirstLaunch: Boolean = true
)