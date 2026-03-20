package space.xiaoxiao.databasemanager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.config.AppConfigStorage
import space.xiaoxiao.databasemanager.features.DatabaseConfigInfo
import space.xiaoxiao.databasemanager.features.DatabaseConfigScreen
import space.xiaoxiao.databasemanager.features.DatabaseConfigStorage
import space.xiaoxiao.databasemanager.features.DatabaseManageScreen
import space.xiaoxiao.databasemanager.features.QueryHistoryStorage
import space.xiaoxiao.databasemanager.features.QuerySessionStorage
import space.xiaoxiao.databasemanager.features.StyledDatabaseListScreen
import space.xiaoxiao.databasemanager.features.StyledQueryScreen
import space.xiaoxiao.databasemanager.features.StyledTableBrowserScreen
import space.xiaoxiao.databasemanager.features.DesignSystemScreen
import space.xiaoxiao.databasemanager.features.AboutScreen
import space.xiaoxiao.databasemanager.features.MoreScreen
import space.xiaoxiao.databasemanager.features.ConfigBackupScreen
import space.xiaoxiao.databasemanager.features.AiConfigScreen
import space.xiaoxiao.databasemanager.charts.ChartScreen
import space.xiaoxiao.databasemanager.charts.ChartEditorScreen
import space.xiaoxiao.databasemanager.charts.ChartPanelManager
import space.xiaoxiao.databasemanager.charts.ChartConfig
import space.xiaoxiao.databasemanager.charts.rememberChartPanelManager
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.LocalizationState
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.theme.ThemeState
import space.xiaoxiao.databasemanager.components.NavIcon
import space.xiaoxiao.databasemanager.components.SvgNavIcon
import space.xiaoxiao.databasemanager.storage.AiConfigStorage
import space.xiaoxiao.databasemanager.storage.SecureStorage
import space.xiaoxiao.databasemanager.storage.EncryptionManager
import space.xiaoxiao.databasemanager.backup.BackupCrypto
import space.xiaoxiao.databasemanager.backup.BackupPayload
import space.xiaoxiao.databasemanager.storage.ConfigSerializer
import space.xiaoxiao.databasemanager.storage.SerializableAppConfig
import space.xiaoxiao.databasemanager.storage.SerializableDatabaseConfig
import space.xiaoxiao.databasemanager.storage.SerializableQueryHistoryItem
import space.xiaoxiao.databasemanager.features.SerializableQuerySession
import space.xiaoxiao.databasemanager.utils.AppExit
import space.xiaoxiao.databasemanager.utils.FileUtils

/**
 * 导航目标枚举
 */
enum class NavDestination {
    DATABASE_LIST,
    TABLE_BROWSER,
    QUERY,
    CHART,
    CHART_EDITOR,
    MORE,
    ABOUT,
    DESIGN_SYSTEM,
    DATABASE_CONFIG,
    DATABASE_MANAGE,
    AI_CONFIG,
    CONFIG_BACKUP
}

/**
 * 导航项目数据类
 */
data class NavItem(
    val icon: NavIcon,
    val labelKey: String,
    val destination: NavDestination
)

/**
 * 应用导航状态
 */
class AppNavigationState {
    var currentDestination by mutableStateOf(NavDestination.DATABASE_LIST)
        private set

    var isAppReady by mutableStateOf(false)
        private set

    var selectedDatabaseId by mutableStateOf<String?>(null)
        private set

    var configToEdit by mutableStateOf<DatabaseConfigInfo?>(null)
        private set

    var currentManagingDatabase by mutableStateOf<DatabaseConfigInfo?>(null)
        private set

    // 图表编辑器状态
    var chartPanelId by mutableStateOf<String?>(null)
        private set

    var chartToEdit by mutableStateOf<ChartConfig?>(null)
        private set

    fun navigateTo(destination: NavDestination) {
        currentDestination = destination
    }

    fun markAppAsReady(ready: Boolean) {
        isAppReady = ready
    }

    fun navigateToDatabaseList() {
        currentDestination = NavDestination.DATABASE_LIST
    }

    fun navigateToTableBrowser() {
        currentDestination = NavDestination.TABLE_BROWSER
    }

    fun navigateToQuery() {
        currentDestination = NavDestination.QUERY
    }

    fun navigateToChart() {
        currentDestination = NavDestination.CHART
    }

    fun navigateToMore() {
        currentDestination = NavDestination.MORE
    }

    fun navigateToAbout() {
        currentDestination = NavDestination.ABOUT
    }

    fun navigateToDesignSystem() {
        currentDestination = NavDestination.DESIGN_SYSTEM
    }

    fun navigateToDatabaseConfig(config: DatabaseConfigInfo? = null) {
        configToEdit = config
        currentDestination = NavDestination.DATABASE_CONFIG
    }

    fun navigateToDatabaseManage(database: DatabaseConfigInfo) {
        currentManagingDatabase = database
        currentDestination = NavDestination.DATABASE_MANAGE
    }

    fun navigateToChartEditor(panelId: String, chart: ChartConfig? = null) {
        chartPanelId = panelId
        chartToEdit = chart
        currentDestination = NavDestination.CHART_EDITOR
    }

    fun navigateToAiConfig() {
        currentDestination = NavDestination.AI_CONFIG
    }

    fun setSelectedDatabase(databaseId: String?) {
        selectedDatabaseId = databaseId
    }

    fun clearConfigToEdit() {
        configToEdit = null
    }

    fun clearCurrentManagingDatabase() {
        currentManagingDatabase = null
    }

    fun clearChartEditorState() {
        chartPanelId = null
        chartToEdit = null
    }

    fun isMainScreen(): Boolean = currentDestination in listOf(
        NavDestination.DATABASE_LIST,
        NavDestination.TABLE_BROWSER,
        NavDestination.QUERY,
        NavDestination.CHART,
        NavDestination.MORE
    )
}

/**
 * 创建导航状态
 */
@Composable
fun rememberAppNavigationState(): AppNavigationState {
    return remember { AppNavigationState() }
}

/**
 * 应用主导航界面
 */
@Composable
fun AppNavigation(
    navigationState: AppNavigationState,
    themeState: ThemeState,
    localizationState: LocalizationState,
    appConfigStorage: AppConfigStorage,
    databaseConfigStorage: DatabaseConfigStorage,
    queryHistoryStorage: QueryHistoryStorage,
    querySessionStorage: QuerySessionStorage,
    aiConfigStorage: space.xiaoxiao.databasemanager.storage.AiConfigStorage
) {
    val language by localizationState.language.collectAsState()

    // 数据库配置管理
    var databases by remember { mutableStateOf(emptyList<DatabaseConfigInfo>()) }
    var isLoaded by remember { mutableStateOf(false) }

    // 图表面板管理器
    val panelManager = rememberChartPanelManager()

    // 导航项目
    val navItems = remember {
        listOf(
            NavItem(NavIcon.DATABASE, "nav_database_list", NavDestination.DATABASE_LIST),
            NavItem(NavIcon.TABLE, "nav_table_browser", NavDestination.TABLE_BROWSER),
            NavItem(NavIcon.QUERY, "nav_query", NavDestination.QUERY),
            NavItem(NavIcon.CHART, "nav_chart", NavDestination.CHART),
            NavItem(NavIcon.MORE, "nav_more", NavDestination.MORE)
        )
    }

    // 从存储加载配置
    LaunchedEffect(Unit) {
        val appConfig = appConfigStorage.loadConfig()
        themeState.loadFromConfig(appConfig.colorTheme)
        localizationState.loadFromConfig(appConfig.language)
        databases = databaseConfigStorage.loadConfigs()
        navigationState.setSelectedDatabase(appConfig.selectedDatabaseId)
        panelManager.restoreFromAppConfig(appConfig)

        // 首次启动时直接进入「更多」页，便于先选择语言
        if (appConfig.isFirstLaunch) {
            navigationState.navigateToMore()
            appConfigStorage.saveConfig(appConfig.copy(isFirstLaunch = false))
        }

        isLoaded = true
    }

    // 保存选中的数据库
    LaunchedEffect(navigationState.selectedDatabaseId, isLoaded) {
        if (isLoaded && themeState.isLoaded && localizationState.isLoaded) {
            appConfigStorage.updateSelectedDatabase(navigationState.selectedDatabaseId)
        }
    }

    // 保存数据库配置（包括清空列表的场景）
    LaunchedEffect(databases, isLoaded) {
        if (isLoaded) {
            databaseConfigStorage.saveConfigs(databases)
        }
    }

    // 保存图表面板配置：单独收集状态，避免在 key 中直接调用 collectAsState
    val panelState by panelManager.panels.collectAsState()
    LaunchedEffect(panelState, isLoaded) {
        if (isLoaded) {
            val (panels, selectedId) = panelManager.toAppConfig()
            appConfigStorage.updateChartPanels(panels, selectedId)
        }
    }

    // 根据目标显示不同界面
    val currentDestination = navigationState.currentDestination
    val showBottomBar = navigationState.isMainScreen()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 3.dp
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                SvgNavIcon(
                                    icon = item.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(stringResource(item.labelKey, language)) },
                            selected = currentDestination == item.destination,
                            onClick = { navigationState.navigateTo(item.destination) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentDestination) {
                NavDestination.DATABASE_LIST -> StyledDatabaseListScreen(
                    language = language,
                    databases = databases,
                    onDatabaseAdd = { configInfo ->
                        databases = databases + configInfo
                    },
                    onDatabaseEdit = { configInfo ->
                        databases = databases.map {
                            if (it.id == configInfo.id) configInfo else it
                        }
                    },
                    onDatabaseDelete = { configInfo ->
                        // 级联删除关联的图表
                        deleteChartsByDatabase(panelManager, configInfo.id)
                        databases = databases.filter { it.id != configInfo.id }
                    },
                    onDatabaseConnect = { _ ->
                        // 连接测试由卡片内部处理
                    },
                    onNavigateToAddDatabase = {
                        navigationState.navigateToDatabaseConfig(null)
                    },
                    onNavigateToEditDatabase = { configInfo ->
                        navigationState.navigateToDatabaseConfig(configInfo)
                    },
                    onNavigateToDatabaseManage = { configInfo ->
                        navigationState.navigateToDatabaseManage(configInfo)
                    }
                )

                NavDestination.TABLE_BROWSER -> StyledTableBrowserScreen(
                    language = language,
                    databases = databases,
                    selectedDatabaseId = navigationState.selectedDatabaseId,
                    onDatabaseSelected = { configId ->
                        navigationState.setSelectedDatabase(configId)
                    }
                )

                NavDestination.QUERY -> StyledQueryScreen(
                    language = language,
                    databases = databases,
                    queryHistoryStorage = queryHistoryStorage,
                    querySessionStorage = querySessionStorage,
                    appConfigStorage = appConfigStorage,
                    aiConfigStorage = aiConfigStorage,
                    onNavigateToAiConfig = { navigationState.navigateToAiConfig() }
                )

                NavDestination.CHART -> ChartScreen(
                    language = language,
                    databases = databases,
                    panelManager = panelManager,
                    onNavigateToEditor = { panelId, chart ->
                        navigationState.navigateToChartEditor(panelId, chart)
                    }
                )

                NavDestination.CHART_EDITOR -> {
                    val panelId = navigationState.chartPanelId
                    if (panelId != null) {
                        ChartEditorScreen(
                            language = language,
                            databases = databases,
                            panelId = panelId,
                            chartToEdit = navigationState.chartToEdit,
                            onNavigateBack = {
                                navigationState.clearChartEditorState()
                                navigationState.navigateToChart()
                            },
                            onSave = { _, chart ->
                                if (navigationState.chartToEdit != null) {
                                    panelManager.updateChart(panelId, chart.id) { chart }
                                } else {
                                    panelManager.addChart(panelId, chart)
                                }
                                navigationState.clearChartEditorState()
                                navigationState.navigateToChart()
                            }
                        )
                    }
                }

                NavDestination.MORE -> MoreScreen(
                    themeState = themeState,
                    localizationState = localizationState,
                    onNavigateToAbout = { navigationState.navigateToAbout() },
                    onNavigateToDesignSystem = { navigationState.navigateToDesignSystem() },
                    onNavigateToAiConfig = { navigationState.navigateToAiConfig() },
                    onNavigateToConfigBackup = { navigationState.navigateTo(NavDestination.CONFIG_BACKUP) },
                    onColorThemeChanged = { theme ->
                        themeState.setColorTheme(theme)
                        appConfigStorage.updateColorTheme(theme.id)
                    },
                    onLanguageChanged = { lang ->
                        localizationState.setLanguage(lang)
                        val code = when (lang) {
                            Language.CHINESE -> "zh"
                            Language.ENGLISH -> "en"
                        }
                        appConfigStorage.updateLanguage(code)
                    }
                )

                NavDestination.ABOUT -> AboutScreen(
                    language = language,
                    onNavigateBack = { navigationState.navigateToMore() }
                )

                NavDestination.DESIGN_SYSTEM -> DesignSystemScreen(
                    language = language,
                    onNavigateBack = { navigationState.navigateToMore() }
                )

                NavDestination.DATABASE_CONFIG -> DatabaseConfigScreen(
                    language = language,
                    configToEdit = navigationState.configToEdit,
                    onNavigateBack = {
                        navigationState.clearConfigToEdit()
                        navigationState.navigateToDatabaseList()
                    },
                    onSave = { configInfo ->
                        if (navigationState.configToEdit != null) {
                            databases = databases.map {
                                if (it.id == configInfo.id) configInfo else it
                            }
                        } else {
                            databases = databases + configInfo
                        }
                        navigationState.clearConfigToEdit()
                        navigationState.navigateToDatabaseList()
                    }
                )

                NavDestination.DATABASE_MANAGE -> {
                    val managingDb = navigationState.currentManagingDatabase
                    if (managingDb != null) {
                        DatabaseManageScreen(
                            language = language,
                            databaseConfig = managingDb,
                            onNavigateBack = {
                                navigationState.clearCurrentManagingDatabase()
                                navigationState.navigateToDatabaseList()
                            }
                        )
                    }
                }

                NavDestination.AI_CONFIG -> AiConfigScreen(
                    language = language,
                    aiConfigStorage = aiConfigStorage,
                    onNavigateBack = { navigationState.navigateToMore() }
                )

                NavDestination.CONFIG_BACKUP -> ConfigBackupScreen(
                    language = language,
                    onNavigateBack = { navigationState.navigateToMore() },
                    onExportConfig = { password ->
                        runCatching {
                            val payload = BackupPayload(
                                exportedAtEpochMillis = System.currentTimeMillis(),
                                appConfig = SerializableAppConfig.fromAppConfig(appConfigStorage.loadConfig()),
                                databaseConfigs = databaseConfigStorage.loadConfigs().map { db ->
                                    SerializableDatabaseConfig(
                                        id = db.id,
                                        name = db.name,
                                        type = db.type.name,
                                        host = db.host,
                                        port = db.port,
                                        database = db.database,
                                        username = db.username,
                                        plainPassword = db.password
                                    )
                                },
                                aiConfig = aiConfigStorage.loadConfig(),
                                queryHistory = queryHistoryStorage.getHistory(50).map { SerializableQueryHistoryItem.fromQueryHistoryItem(it) },
                                querySessions = querySessionStorage.loadSessions().map { SerializableQuerySession.fromQuerySession(it) }
                            )
                            val plaintext = ConfigSerializer.json.encodeToString(BackupPayload.serializer(), payload)
                            val encryptedFileJson = BackupCrypto.encryptToFileJson(plaintext, password)
                            val ok = FileUtils.saveFile(encryptedFileJson, defaultName = "dbm-config-backup", extension = "dbmconf")
                            if (!ok) error("Save failed")
                        }
                    },
                    onImportConfig = { password, fileContent ->
                        runCatching {
                            val plaintext = BackupCrypto.decryptFromFileJson(fileContent, password)
                            val payload = ConfigSerializer.json.decodeFromString(BackupPayload.serializer(), plaintext)

                            // Clear existing
                            databaseConfigStorage.saveConfigs(emptyList())
                            appConfigStorage.resetToDefault()
                            aiConfigStorage.deleteConfig()
                            queryHistoryStorage.clearHistory()
                            querySessionStorage.clearSessions()

                            // Restore
                            val restoredDb = payload.databaseConfigs.map { cfg ->
                                DatabaseConfigInfo(
                                    id = cfg.id,
                                    name = cfg.name,
                                    type = space.xiaoxiao.databasemanager.core.DatabaseType.valueOf(cfg.type),
                                    host = cfg.host,
                                    port = cfg.port,
                                    database = cfg.database,
                                    username = cfg.username,
                                    password = cfg.plainPassword ?: (cfg.encryptedPassword ?: ""),
                                    charset = null
                                )
                            }
                            databaseConfigStorage.saveConfigs(restoredDb)
                            appConfigStorage.saveConfig(SerializableAppConfig.toAppConfig(payload.appConfig))
                            payload.aiConfig?.let { aiConfigStorage.saveConfig(it) }
                            queryHistoryStorage.replaceAll(payload.queryHistory.map { SerializableQueryHistoryItem.toQueryHistoryItem(it) })
                            payload.querySessions.forEach { querySessionStorage.saveSession(SerializableQuerySession.toQuerySession(it)) }

                            // 导入成功后直接退出应用，下次启动重新按新配置加载
                            AppExit.exitApp()
                        }
                    },
                    onClearConfigAndExit = {
                        databaseConfigStorage.saveConfigs(emptyList())
                        appConfigStorage.resetToDefault()
                        aiConfigStorage.deleteConfig()
                        queryHistoryStorage.clearHistory()
                        querySessionStorage.clearSessions()
                        AppExit.exitApp()
                    },
                    pickFile = { FileUtils.pickFile(listOf("dbmconf", "json")) }
                )
            }
        }
    }
}

/**
 * 删除指定数据库关联的所有图表（级联删除）
 * 只删除图表，不删除面板
 */
private fun deleteChartsByDatabase(panelManager: ChartPanelManager, databaseId: String) {
    val panels = panelManager.panels.value
    panels.forEach { panel ->
        val chartsToDelete = panel.charts.filter { it.databaseId == databaseId }
        chartsToDelete.forEach { chart ->
            panelManager.deleteChart(panel.id, chart.id)
        }
    }
}