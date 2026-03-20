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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import space.xiaoxiao.databasemanager.core.ConnectionStatus
import space.xiaoxiao.databasemanager.core.DbExecutionChannel
import space.xiaoxiao.databasemanager.core.ExecutionPurpose
import space.xiaoxiao.databasemanager.core.QueryResult
import space.xiaoxiao.databasemanager.components.AppPillTabRow
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
    val addChartStr = stringResource("add_chart", language)
    val editChartStr = stringResource("edit_chart", language)
    val deleteChartStr = stringResource("delete_chart", language)
    val newChartPanelContentDesc = stringResource("new_chart_panel", language)

    Scaffold(
        floatingActionButton = {
            if (selectedPanel != null) {
                ExtendedFloatingActionButton(
                    onClick = { onNavigateToEditor(selectedPanel.id, null) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(addChartStr) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    // 选中面板的操作按钮
                    if (selectedPanel != null) {
                        Row(
                            modifier = Modifier.padding(end = AppSpacing.spaceSm),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    panelToRename = selectedPanel
                                    newPanelName = selectedPanel.name
                                    showRenamePanelDialog = true
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = stringResource("rename_panel", language),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    panelToDelete = selectedPanel
                                    showDeletePanelDialog = true
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = stringResource("delete_panel", language),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // 内容区域
            if (panels.isEmpty()) {
                // 空状态 - 无面板
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
                    ) {
                        Icon(
                            Icons.Filled.Dashboard,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = noChartPanelsStr,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = addChartPanelHintStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Button(onClick = { showCreatePanelDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource("new_chart_panel", language))
                        }
                    }
                }
            } else if (selectedPanel?.charts?.isEmpty() != false) {
                // 空状态 - 无图表
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
                    ) {
                        Icon(
                            Icons.Filled.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = noChartsStr,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = addChartHintStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
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
                            onEdit = { onNavigateToEditor(selectedPanel.id, chart) },
                            onDelete = { panelManager.deleteChart(selectedPanel.id, chart.id) },
                            onRefresh = {
                                refreshChart(chart, databases, chartDataMap, chartLoadingMap, chartErrorMap)
                            }
                        )
                    }
                }
            }
        }
    }

    // 创建面板对话框
    if (showCreatePanelDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePanelDialog = false },
            title = { Text(stringResource("new_chart_panel", language)) },
            text = {
                OutlinedTextField(
                    value = newPanelName,
                    onValueChange = { newPanelName = it },
                    label = { Text(stringResource("panel_name", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPanelName.isNotBlank()) {
                            panelManager.createPanel(newPanelName)
                            newPanelName = ""
                            showCreatePanelDialog = false
                        }
                    },
                    enabled = newPanelName.isNotBlank()
                ) {
                    Text(stringResource("create", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePanelDialog = false }) {
                    Text(stringResource("cancel", language))
                }
            }
        )
    }

    // 重命名面板对话框
    if (showRenamePanelDialog && panelToRename != null) {
        AlertDialog(
            onDismissRequest = { showRenamePanelDialog = false },
            title = { Text(stringResource("rename_panel", language)) },
            text = {
                OutlinedTextField(
                    value = newPanelName,
                    onValueChange = { newPanelName = it },
                    label = { Text(stringResource("panel_name", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPanelName.isNotBlank()) {
                            panelManager.renamePanel(panelToRename!!.id, newPanelName)
                            panelToRename = null
                            newPanelName = ""
                            showRenamePanelDialog = false
                        }
                    },
                    enabled = newPanelName.isNotBlank()
                ) {
                    Text(stringResource("save", language))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    panelToRename = null
                    showRenamePanelDialog = false
                }) {
                    Text(stringResource("cancel", language))
                }
            }
        )
    }

    // 删除面板确认对话框
    if (showDeletePanelDialog && panelToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeletePanelDialog = false },
            title = { Text(stringResource("delete_panel", language)) },
            text = {
                Text(stringResource("confirm_delete_panel", language) + " \"${panelToDelete!!.name}\"?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        panelManager.deletePanel(panelToDelete!!.id)
                        panelToDelete = null
                        showDeletePanelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource("delete", language))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    panelToDelete = null
                    showDeletePanelDialog = false
                }) {
                    Text(stringResource("cancel", language))
                }
            }
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
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRefresh: () -> Unit
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
                    isLoading -> CircularProgressIndicator()
                    error != null -> Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
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
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                IconButton(onClick = onRefresh, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Refresh, contentDescription = "刷新", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "编辑", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}