package space.xiaoxiao.databasemanager.charts

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import space.xiaoxiao.databasemanager.config.AppConfig

/**
 * 图表面板管理器
 * 管理多个面板和面板内的图表配置
 */
class ChartPanelManager {
    private val _panels = MutableStateFlow<List<ChartPanel>>(emptyList())
    val panels: StateFlow<List<ChartPanel>> = _panels.asStateFlow()

    private val _selectedPanelId = MutableStateFlow<String?>(null)
    val selectedPanelId: StateFlow<String?> = _selectedPanelId.asStateFlow()

    // 图表运行时状态缓存
    private val chartStates = mutableMapOf<String, ChartState>()

    // ==================== 面板操作 ====================

    /**
     * 创建新面板
     */
    fun createPanel(name: String): ChartPanel {
        val panel = ChartPanel(name = name)
        _panels.value = _panels.value + panel
        _selectedPanelId.value = panel.id
        return panel
    }

    /**
     * 重命名面板
     */
    fun renamePanel(panelId: String, newName: String) {
        _panels.value = _panels.value.map { panel ->
            if (panel.id == panelId) panel.copy(name = newName) else panel
        }
    }

    /**
     * 删除面板
     */
    fun deletePanel(panelId: String) {
        val panel = _panels.value.find { it.id == panelId } ?: return
        // 清理面板内图表的状态缓存
        panel.charts.forEach { chart ->
            chartStates.remove(chart.id)
        }
        _panels.value = _panels.value.filter { it.id != panelId }
        if (_selectedPanelId.value == panelId) {
            _selectedPanelId.value = _panels.value.firstOrNull()?.id
        }
    }

    /**
     * 选择面板
     */
    fun selectPanel(panelId: String) {
        if (_panels.value.any { it.id == panelId }) {
            _selectedPanelId.value = panelId
        }
    }

    /**
     * 获取选中的面板
     */
    fun getSelectedPanel(): ChartPanel? {
        return _selectedPanelId.value?.let { getPanel(it) }
    }

    /**
     * 获取面板
     */
    fun getPanel(panelId: String): ChartPanel? {
        return _panels.value.find { it.id == panelId }
    }

    // ==================== 图表操作 ====================

    /**
     * 添加图表到面板
     */
    fun addChart(panelId: String, config: ChartConfig): ChartConfig? {
        val panel = _panels.value.find { it.id == panelId } ?: return null
        val newConfig = config.copy(position = panel.charts.size)
        val updatedPanel = panel.copy(charts = panel.charts + newConfig)
        _panels.value = _panels.value.map { if (it.id == panelId) updatedPanel else it }
        return newConfig
    }

    /**
     * 更新图表
     */
    fun updateChart(panelId: String, chartId: String, update: (ChartConfig) -> ChartConfig): Boolean {
        val panelIndex = _panels.value.indexOfFirst { it.id == panelId }
        if (panelIndex < 0) return false

        val panel = _panels.value[panelIndex]
        val chartIndex = panel.charts.indexOfFirst { it.id == chartId }
        if (chartIndex < 0) return false

        val updatedCharts = panel.charts.toMutableList()
        updatedCharts[chartIndex] = update(updatedCharts[chartIndex])
        _panels.value = _panels.value.toMutableList().also { it[panelIndex] = panel.copy(charts = updatedCharts) }
        return true
    }

    /**
     * 删除图表
     */
    fun deleteChart(panelId: String, chartId: String) {
        val panelIndex = _panels.value.indexOfFirst { it.id == panelId }
        if (panelIndex < 0) return

        val panel = _panels.value[panelIndex]
        val updatedCharts = panel.charts.filter { it.id != chartId }
        _panels.value = _panels.value.toMutableList().also { it[panelIndex] = panel.copy(charts = updatedCharts) }
        chartStates.remove(chartId)
    }

    /**
     * 重新排序图表
     */
    fun reorderCharts(panelId: String, fromIndex: Int, toIndex: Int) {
        val panelIndex = _panels.value.indexOfFirst { it.id == panelId }
        if (panelIndex < 0) return

        val panel = _panels.value[panelIndex]
        val charts = panel.charts.toMutableList()
        if (fromIndex !in charts.indices || toIndex !in charts.indices) return

        val chart = charts.removeAt(fromIndex)
        charts.add(toIndex, chart)
        val reorderedCharts = charts.mapIndexed { index, config -> config.copy(position = index) }
        _panels.value = _panels.value.toMutableList().also { it[panelIndex] = panel.copy(charts = reorderedCharts) }
    }

    // ==================== 图表状态管理 ====================

    /**
     * 获取图表运行时状态
     */
    fun getChartState(chartId: String): ChartState? {
        return chartStates[chartId]
    }

    /**
     * 更新图表运行时状态
     */
    fun updateChartState(chartId: String, state: ChartState) {
        chartStates[chartId] = state
    }

    /**
     * 清除图表状态缓存
     */
    fun clearChartState(chartId: String) {
        chartStates.remove(chartId)
    }

    // ==================== 持久化 ====================

    /**
     * 导出到 AppConfig
     */
    fun toAppConfig(): Pair<List<SerializableChartPanel>, String?> {
        return Pair(
            _panels.value.map { SerializableChartPanel.fromChartPanel(it) },
            _selectedPanelId.value
        )
    }

    /**
     * 从 AppConfig 恢复
     */
    fun restoreFromAppConfig(config: AppConfig) {
        cleanup()
        if (config.chartPanels.isNotEmpty()) {
            _panels.value = config.chartPanels.map { SerializableChartPanel.toChartPanel(it) }
            _selectedPanelId.value = config.selectedChartPanelId ?: _panels.value.firstOrNull()?.id
        }
    }

    /**
     * 清理所有数据
     */
    fun cleanup() {
        chartStates.clear()
        _panels.value = emptyList()
        _selectedPanelId.value = null
    }
}

/**
 * 创建 ChartPanelManager 的 Composable 记忆函数
 */
@Composable
fun rememberChartPanelManager() = remember { ChartPanelManager() }