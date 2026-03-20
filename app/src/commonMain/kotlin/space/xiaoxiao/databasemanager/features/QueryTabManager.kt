package space.xiaoxiao.databasemanager.features

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import space.xiaoxiao.databasemanager.config.AppConfig
import space.xiaoxiao.databasemanager.storage.SerializableQuerySessionLite

class QueryTabManager(
    private val historyStorage: QueryHistoryStorage? = null,
    private val sessionStorage: QuerySessionStorage? = null
) {
    private val _tabs = MutableStateFlow<List<QueryTab>>(emptyList())
    val tabs: StateFlow<List<QueryTab>> = _tabs.asStateFlow()

    private val _selectedTabId = MutableStateFlow<String?>(null)
    val selectedTabId: StateFlow<String?> = _selectedTabId.asStateFlow()

    private val tabViewModels = mutableMapOf<String, DatabaseViewModel>()
    private val cleanupScope = CoroutineScope(Dispatchers.Default)

    fun createTab(
        databaseId: String? = null,
        databaseConfig: DatabaseConfigInfo? = null,
        sessionName: String = ""
    ): QueryTab {
        val tabIndex = _tabs.value.size + 1
        val name = if (sessionName.isNotBlank()) sessionName else "Query-$tabIndex"
        val tab = QueryTab(sessionName = name, databaseId = databaseId, databaseConfig = databaseConfig)
        _tabs.value = _tabs.value + tab
        tabViewModels[tab.id] = historyStorage?.let { DatabaseViewModel(it) } ?: DatabaseViewModel()
        _selectedTabId.value = tab.id
        return tab
    }

    /**
     * 保存所有会话到 AppConfig
     */
    fun toAppConfigTabs(): List<SerializableQuerySessionLite> {
        return _tabs.value.map { tab ->
            SerializableQuerySessionLite(
                id = tab.id,
                sessionName = tab.sessionName,
                sql = tab.sql,
                databaseId = tab.databaseId,
                transactionMode = tab.transactionMode.name,
                transactionIsolationLevel = tab.transactionIsolationLevel.name,
                isResultExpanded = tab.isResultExpanded,
                autoExpandResult = tab.autoExpandResult
            )
        }
    }

    /**
     * 从 AppConfig 恢复会话
     */
    fun restoreFromAppConfig(config: AppConfig) {
        // 先清理现有会话
        cleanup()

        // 从 AppConfig 恢复会话
        if (config.openQueryTabs.isNotEmpty()) {
            config.openQueryTabs.forEach { session ->
                val tab = QueryTab(
                    id = session.id,
                    sessionName = session.sessionName,
                    sql = session.sql,
                    databaseId = session.databaseId,
                    databaseConfig = null, // 配置从 databases 列表中获取
                    transactionMode = TransactionMode.valueOf(session.transactionMode),
                    transactionIsolationLevel = TransactionIsolationLevel.valueOf(session.transactionIsolationLevel),
                    isResultExpanded = session.isResultExpanded,
                    autoExpandResult = session.autoExpandResult
                )
                _tabs.value = _tabs.value + tab
                tabViewModels[tab.id] = historyStorage?.let { DatabaseViewModel(it) } ?: DatabaseViewModel()
            }
            // 恢复选中的 Tab
            _selectedTabId.value = config.lastSelectedQueryTabId ?: _tabs.value.firstOrNull()?.id
        }
    }

    /**
     * 保存到持久化存储（用于备份）
     */
    suspend fun saveAllSessions() {
        sessionStorage?.let { storage ->
            _tabs.value.forEach { tab ->
                val session = QuerySession(
                    id = tab.id,
                    sessionName = tab.sessionName,
                    sql = tab.sql,
                    databaseId = tab.databaseId,
                    databaseConfig = tab.databaseConfig,
                    transactionMode = tab.transactionMode,
                    transactionIsolationLevel = tab.transactionIsolationLevel,
                    isResultExpanded = tab.isResultExpanded,
                    autoExpandResult = tab.autoExpandResult,
                    timestamp = System.currentTimeMillis()
                )
                storage.saveSession(session)
            }
        }
    }

    /**
     * 从持久化存储恢复会话（旧版本兼容）
     */
    suspend fun restoreSessions() {
        val sessions = sessionStorage?.loadSessions() ?: return
        sessions.forEach { session ->
            val tab = QueryTab(
                id = session.id,
                sessionName = session.sessionName,
                sql = session.sql,
                databaseId = session.databaseId,
                databaseConfig = session.databaseConfig,
                transactionMode = session.transactionMode,
                transactionIsolationLevel = session.transactionIsolationLevel,
                isResultExpanded = session.isResultExpanded,
                autoExpandResult = session.autoExpandResult
            )
            _tabs.value = _tabs.value + tab
            tabViewModels[tab.id] = historyStorage?.let { DatabaseViewModel(it) } ?: DatabaseViewModel()
        }
        if (_tabs.value.isNotEmpty()) {
            _selectedTabId.value = _tabs.value.first().id
        }
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value.toMutableList()
        val tabIndex = currentTabs.indexOfFirst { it.id == tabId }
        if (tabIndex >= 0) {
            // 同步清理 ViewModel 资源
            tabViewModels[tabId]?.let { vm ->
                runBlocking {
                    vm.close()
                }
            }
            tabViewModels.remove(tabId)
            currentTabs.removeAt(tabIndex)
            _tabs.value = currentTabs
            if (_selectedTabId.value == tabId) {
                _selectedTabId.value = if (currentTabs.isEmpty()) null else currentTabs[minOf(tabIndex, currentTabs.size - 1)].id
            }
        }
    }

    fun selectTab(tabId: String) { if (_tabs.value.any { it.id == tabId }) _selectedTabId.value = tabId }
    fun updateTab(tabId: String, update: (QueryTab) -> QueryTab) { _tabs.value = _tabs.value.map { if (it.id == tabId) update(it) else it } }
    fun getTab(tabId: String): QueryTab? = _tabs.value.find { it.id == tabId }
    fun getSelectedTab(): QueryTab? = _selectedTabId.value?.let { getTab(it) }
    fun getTabViewModel(tabId: String): DatabaseViewModel? = tabViewModels[tabId]
    fun cleanup() {
        tabViewModels.values.forEach { cleanupScope.launch { it.close() } }
        tabViewModels.clear()
        _tabs.value = emptyList()
        _selectedTabId.value = null
    }
}

@Composable
fun rememberQueryTabManager(
    historyStorage: QueryHistoryStorage? = null,
    sessionStorage: QuerySessionStorage? = null
) = remember { QueryTabManager(historyStorage, sessionStorage) }
