package space.xiaoxiao.databasemanager.charts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import space.xiaoxiao.databasemanager.core.ConnectionStatus
import space.xiaoxiao.databasemanager.core.DbExecutionChannel
import space.xiaoxiao.databasemanager.core.ExecutionPurpose
import space.xiaoxiao.databasemanager.components.AppButton
import space.xiaoxiao.databasemanager.components.AppConfirmDialog
import space.xiaoxiao.databasemanager.components.AppCustomDialog
import space.xiaoxiao.databasemanager.components.AppEmptyState
import space.xiaoxiao.databasemanager.components.AppErrorState
import space.xiaoxiao.databasemanager.components.AppLoadingIndicator
import space.xiaoxiao.databasemanager.components.AppPillTabRow
import space.xiaoxiao.databasemanager.components.AppTextButton
import space.xiaoxiao.databasemanager.components.AppTextField
import space.xiaoxiao.databasemanager.components.ButtonVariant
import space.xiaoxiao.databasemanager.components.SpeedDialAction
import space.xiaoxiao.databasemanager.components.SpeedDialFAB
import space.xiaoxiao.databasemanager.core.createDatabaseClient
import space.xiaoxiao.databasemanager.features.DatabaseConfigInfo
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.theme.AppSpacing

/**
 * 图表面板主页面
 * 支持多 Tab 面板管理，每个面板包含多个图表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    language: Language = Language.CHINESE,
    databases: List<DatabaseConfigInfo>,
    panelManager: ChartPanelManager,
    onNavigateToEditor: (String, ChartConfig?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val panels by panelManager.panels.collectAsState()
    val selectedPanelId by panelManager.selectedPanelId.collectAsState()
    val selectedPanel = panels.find { it.id == selectedPanelId }

    // 对话框状态
    var showCreatePanelDialog by remember { mutableStateOf(false) }
    var showRenamePanelDialog by remember { mutableStateOf(false) }
    var showDeletePanelDialog by remember { mutableStateOf(false) }
    var panelToRename by remember { mutableStateOf<ChartPanel?>(null) }
    var panelToDelete by remember { mutableStateOf<ChartPanel?>(null) }
    var newPanelName by remember { mutableStateOf("") }
    var isSpeedDialExpanded by remember { mutableStateOf(false) }

    // 图表数据缓存
    val chartDataMap = remember { mutableStateMapOf<String, ChartData>() }
    val chartLoadingMap = remember { mutableStateMapOf<String, Boolean>() }
    val chartErrorMap = remember { mutableStateMapOf<String, String?>() }

    // 自动刷新当前面板的所有图表
    LaunchedEffect(selectedPanelId) {
        val panel = selectedPanel ?: return@LaunchedEffect
        panel.charts.forEach { chart ->
            if (chartDataMap[chart.id] == null && chartLoadingMap[chart.id] != true) {
                refreshChart(chart, databases, chartDataMap, chartLoadingMap, chartErrorMap)
            }
        }
    }

    // 字符串资源
    val chartPanelStr = stringResource("chart_panel", language)
    val noChartPanelsStr = stringResource("no_chart_panels", language)
    val addChartPanelHintStr = stringResource("add_chart_panel_hint", language)
    val noChartsStr = stringResource("no_charts", language)
    val addChartHintStr = stringResource("add_chart_hint", language)
    val editChartStr = stringResource("edit_chart", language)
    val deleteChartStr = stringResource("delete_chart", language)
    val newChartPanelContentDesc = stringResource("new_chart_panel", language)

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // Tab 栏
            if (panels.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppPillTabRow(
                        tabs = panels.map { it.name },
                        selectedIndex = panels.indexOfFirst { it.id == selectedPanelId }.coerceAtLeast(0),
                        onSelectIndex = { index -> panelManager.selectPanel(panels[index].id) },
                        modifier = Modifier.weight(1f),
                        isScrollable = true,
                        addTabIcon = Icons.Filled.Add,
                        addTabContentDescription = newChartPanelContentDesc,
                        onAddTab = { showCreatePanelDialog = true }
                    )

                }
            }

            // 内容区域
            if (panels.isEmpty()) {
                // 空状态 - 无面板
                AppEmptyState(
                    icon = Icons.Filled.Dashboard,
                    title = noChartPanelsStr,
                    message = addChartPanelHintStr,
                    modifier = Modifier.fillMaxSize(),
                    actionLabel = stringResource("new_chart_panel", language),
                    onAction = { showCreatePanelDialog = true }
                )
            } else if (selectedPanel?.charts?.isEmpty() != false) {
                // 空状态 - 无图表
                AppEmptyState(
                    icon = Icons.Filled.BarChart,
                    title = noChartsStr,
                    message = addChartHintStr,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 图表网格 - 使用固定 2 列
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppSpacing.spaceMd),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
                ) {
                    items(
                        items = selectedPanel!!.charts,
                        key = { it.id },
                        span = { chart -> GridItemSpan(chart.width.span) }
                    ) { chart ->
                        ChartCard(
                            chart = chart,
                            chartData = chartDataMap[chart.id],
                            isLoading = chartLoadingMap[chart.id] == true,
                            error = chartErrorMap[chart.id],
                            language = language,
                            onEdit = { onNavigateToEditor(selectedPanel.id, chart) },
                            onDelete = { panelManager.deleteChart(selectedPanel.id, chart.id) },
                            onRefresh = {
                                refreshChart(chart, databases, chartDataMap, chartLoadingMap, chartErrorMap)
                            },
                            onDismissError = { chartErrorMap[chart.id] = null }
                        )
                    }
                }
            }
        }

        // SpeedDialFAB - 悬浮快捷操作
        if (selectedPanel != null) {
            SpeedDialFAB(
                mainIcon = Icons.Filled.Add,
                actions = listOf(
                    SpeedDialAction(Icons.Filled.AddChart, "添加图表") {
                        onNavigateToEditor(selectedPanel.id, null)
                    },
                    SpeedDialAction(Icons.Filled.Edit, "重命名面板") {
                        panelToRename = selectedPanel
                        newPanelName = selectedPanel.name
                        showRenamePanelDialog = true
                    },
                    SpeedDialAction(Icons.Filled.Delete, "删除面板") {
                        panelToDelete = selectedPanel
                        showDeletePanelDialog = true
                    }
                ),
                isExpanded = isSpeedDialExpanded,
                onToggle = { isSpeedDialExpanded = !isSpeedDialExpanded }
            )
        }
    }
}

    // 创建面板对话框
    if (showCreatePanelDialog) {
        AppCustomDialog(
            title = stringResource("new_chart_panel", language),
            confirmText = stringResource("create", language),
            cancelText = stringResource("cancel", language),
            onConfirm = {
                if (newPanelName.isNotBlank()) {
                    panelManager.createPanel(newPanelName)
                    newPanelName = ""
                    showCreatePanelDialog = false
                }
            },
            onDismiss = { showCreatePanelDialog = false },
            content = {
                AppTextField(
                                    value = newPanelName,
                                    onValueChange = { newPanelName = it },
                                    label = stringResource("panel_name", language),
                                    singleLine = true
                                )
            }
        )
    }

    // 重命名面板对话框
    if (showRenamePanelDialog && panelToRename != null) {
        AppCustomDialog(
            title = stringResource("rename_panel", language),
            confirmText = stringResource("save", language),
            cancelText = stringResource("cancel", language),
            onConfirm = {
                if (newPanelName.isNotBlank()) {
                    panelManager.renamePanel(panelToRename!!.id, newPanelName)
                    panelToRename = null
                    newPanelName = ""
                    showRenamePanelDialog = false
                }
            },
            onDismiss = {
                panelToRename = null
                showRenamePanelDialog = false
            },
            content = {
                AppTextField(
                                    value = newPanelName,
                                    onValueChange = { newPanelName = it },
                                    label = stringResource("panel_name", language),
                                    singleLine = true
                                )
            }
        )
    }

    // 删除面板确认对话框
    if (showDeletePanelDialog && panelToDelete != null) {
        AppConfirmDialog(
            title = stringResource("delete_panel", language),
            message = stringResource("confirm_delete_panel", language) + " \"${panelToDelete!!.name}\"?",
            confirmText = stringResource("delete", language),
            cancelText = stringResource("cancel", language),
            onConfirm = {
                panelManager.deletePanel(panelToDelete!!.id)
                panelToDelete = null
                showDeletePanelDialog = false
            },
            onDismiss = {
                panelToDelete = null
                showDeletePanelDialog = false
            },
            isDangerous = true
        )
    }
}

/**
 * 刷新单个图表数据
 */
private fun refreshChart(
    chart: ChartConfig,
    databases: List<DatabaseConfigInfo>,
    chartDataMap: MutableMap<String, ChartData>,
    chartLoadingMap: MutableMap<String, Boolean>,
    chartErrorMap: MutableMap<String, String?>
) {
    val db = databases.find { it.id == chart.databaseId }
    if (db == null) {
        chartErrorMap[chart.id] = "数据库不存在"
        return
    }

    chartLoadingMap[chart.id] = true
    chartErrorMap[chart.id] = null

    kotlinx.coroutines.GlobalScope.launch {
        try {
            val config = db.toDatabaseConfig()
            val client = createDatabaseClient(config)
            val status = client.context.connect()

            if (status !is ConnectionStatus.Connected) {
                chartErrorMap[chart.id] = "连接失败: $status"
                chartLoadingMap[chart.id] = false
                return@launch
            }

            val result = client.executor.executeQuery(
                client.context,
                DbExecutionChannel(
                    sql = chart.sqlQuery,
                    purpose = ExecutionPurpose.UTIL
                )
            )

            result.onSuccess { qr ->
                if (qr.rows.isEmpty()) {
                    chartErrorMap[chart.id] = "查询结果为空"
                } else {
                    val maxColumns = qr.columns.size
                    val labelIdx = chart.labelColumnIndex.coerceIn(0, maxColumns - 1)
                    val valueIdx = chart.valueColumnIndex.coerceIn(0, maxColumns - 1)

                    val chartResult = qr.toChartData(
                        labelColumn = labelIdx,
                        valueColumn = valueIdx,
                        title = chart.title
                    )

                    chartResult.onSuccess { data ->
                        chartDataMap[chart.id] = data.copy(color = chart.color)
                    }.onFailure { e ->
                        chartErrorMap[chart.id] = e.message
                    }
                }
            }.onFailure { e ->
                chartErrorMap[chart.id] = e.message ?: "查询失败"
            }

            client.context.disconnect()
        } catch (e: Exception) {
            chartErrorMap[chart.id] = e.message ?: "执行失败"
        } finally {
            chartLoadingMap[chart.id] = false
        }
    }
}

/**
 * 图表卡片组件
 */
@Composable
private fun ChartCard(
    chart: ChartConfig,
    chartData: ChartData?,
    isLoading: Boolean,
    error: String?,
    language: Language = Language.CHINESE,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRefresh: () -> Unit,
    onDismissError: () -> Unit
) {
    // 根据图表宽度设置高度
    val cardHeight = when (chart.width) {
        ChartWidth.MEDIUM -> 240.dp
        ChartWidth.FULL -> 300.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 图表内容
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSpacing.spaceSm),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> AppLoadingIndicator()
                    error != null -> AppErrorState(
                        message = error,
                        onDismiss = onDismissError,
                        onRetry = onRefresh,
                        showRetry = false,
                        language = language
                    )
                    chartData != null -> {
                        when (chart.chartType) {
                            ChartType.BAR -> BarChart(chartData = chartData, modifier = Modifier.fillMaxSize())
                            ChartType.PIE -> PieChart(chartData = chartData, modifier = Modifier.fillMaxSize())
                            ChartType.LINE -> LineChart(chartData = chartData, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }

            // 操作按钮放在右上角
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(AppSpacing.spaceXs),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceNone)
            ) {
                IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Refresh, contentDescription = stringResource("refresh", language), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = stringResource("edit", language), modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource("delete", language), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}