package space.xiaoxiao.databasemanager.storage

import kotlinx.serialization.Serializable
import space.xiaoxiao.databasemanager.core.DatabaseType
import space.xiaoxiao.databasemanager.features.DatabaseConfigInfo
import space.xiaoxiao.databasemanager.features.QueryHistoryItem
import space.xiaoxiao.databasemanager.config.AppConfig
import space.xiaoxiao.databasemanager.charts.SerializableChartPanel

/**
 * 可序列化的数据库配置信息
 */
@Serializable
data class SerializableDatabaseConfig(
    val id: String,
    val name: String,
    val type: String,  // 使用字符串存储枚举
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val encryptedPassword: String? = null,  // 加密后的密码
    val plainPassword: String? = null  // 仅用于迁移，之后会被移除
) {
    companion object {
        fun fromDatabaseConfig(config: DatabaseConfigInfo): SerializableDatabaseConfig {
            return SerializableDatabaseConfig(
                id = config.id,
                name = config.name,
                type = config.type.name,
                host = config.host,
                port = config.port,
                database = config.database,
                username = config.username,
                plainPassword = config.password  // 初始时保存为明文，后续由 Storage 加密
            )
        }

        fun toDatabaseConfig(config: SerializableDatabaseConfig, password: String): DatabaseConfigInfo {
            return DatabaseConfigInfo(
                id = config.id,
                name = config.name,
                type = DatabaseType.valueOf(config.type),
                host = config.host,
                port = config.port,
                database = config.database,
                username = config.username,
                password = password
            )
        }
    }
}

/**
 * 可序列化的查询历史项
 */
@Serializable
data class SerializableQueryHistoryItem(
    val id: String,
    val sql: String,
    val databaseId: String,
    val databaseName: String,
    val rowCount: Int,
    val executionTimeMs: Long,
    val isSuccess: Boolean,
    val timestamp: Long
) {
    companion object {
        fun fromQueryHistoryItem(item: QueryHistoryItem): SerializableQueryHistoryItem {
            return SerializableQueryHistoryItem(
                id = item.id,
                sql = item.sql,
                databaseId = item.databaseId,
                databaseName = item.databaseName,
                rowCount = item.rowCount,
                executionTimeMs = item.executionTimeMs,
                isSuccess = item.isSuccess,
                timestamp = item.timestamp
            )
        }

        fun toQueryHistoryItem(item: SerializableQueryHistoryItem): QueryHistoryItem {
            return QueryHistoryItem(
                id = item.id,
                sql = item.sql,
                databaseId = item.databaseId,
                databaseName = item.databaseName,
                rowCount = item.rowCount,
                executionTimeMs = item.executionTimeMs,
                isSuccess = item.isSuccess,
                timestamp = item.timestamp
            )
        }
    }
}

/**
 * 可序列化的查询会话（简化版，用于 AppConfig 存储）
 */
@Serializable
data class SerializableQuerySessionLite(
    val id: String,
    val sessionName: String,
    val sql: String,
    val databaseId: String?,
    val transactionMode: String,
    val transactionIsolationLevel: String,
    val isResultExpanded: Boolean,
    val autoExpandResult: Boolean
)

/**
 * 可序列化的应用配置
 */
@Serializable
data class SerializableAppConfig(
    val colorTheme: String? = null,
    val language: String? = null,
    val selectedDatabaseId: String? = null,
    val openQueryTabs: List<SerializableQuerySessionLite> = emptyList(),
    val lastSelectedQueryTabId: String? = null,
    val chartPanels: List<SerializableChartPanel> = emptyList(),
    val selectedChartPanelId: String? = null,
    val isFirstLaunch: Boolean = true
) {
    companion object {
        fun fromAppConfig(config: AppConfig): SerializableAppConfig {
            return SerializableAppConfig(
                colorTheme = config.colorTheme,
                language = config.language,
                selectedDatabaseId = config.selectedDatabaseId,
                openQueryTabs = config.openQueryTabs,
                lastSelectedQueryTabId = config.lastSelectedQueryTabId,
                chartPanels = config.chartPanels,
                selectedChartPanelId = config.selectedChartPanelId,
                isFirstLaunch = config.isFirstLaunch
            )
        }

        fun toAppConfig(config: SerializableAppConfig): AppConfig {
            return AppConfig(
                colorTheme = config.colorTheme,
                language = config.language,
                selectedDatabaseId = config.selectedDatabaseId,
                openQueryTabs = config.openQueryTabs,
                lastSelectedQueryTabId = config.lastSelectedQueryTabId,
                chartPanels = config.chartPanels,
                selectedChartPanelId = config.selectedChartPanelId,
                isFirstLaunch = config.isFirstLaunch
            )
        }
    }
}
