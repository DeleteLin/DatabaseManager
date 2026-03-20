package space.xiaoxiao.databasemanager.charts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import space.xiaoxiao.databasemanager.core.ConnectionStatus
import space.xiaoxiao.databasemanager.core.DbExecutionChannel
import space.xiaoxiao.databasemanager.core.ExecutionPurpose
import space.xiaoxiao.databasemanager.core.QueryResult
import space.xiaoxiao.databasemanager.core.createDatabaseClient
import space.xiaoxiao.databasemanager.features.DatabaseConfigInfo
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.theme.AppSpacing

/**
 * 图表编辑器页面
 * 用于创建和编辑图表配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartEditorScreen(
    language: Language = Language.CHINESE,
    databases: List<DatabaseConfigInfo>,
    panelId: String,
    chartToEdit: ChartConfig? = null,
    onNavigateBack: () -> Unit,
    onSave: (String, ChartConfig) -> Unit
) {
    val scope = rememberCoroutineScope()

    // 表单状态
    var title by remember { mutableStateOf(chartToEdit?.title ?: "") }
    var selectedDbId by remember { mutableStateOf(chartToEdit?.databaseId ?: "") }
    var sqlQuery by remember { mutableStateOf(TextFieldValue(chartToEdit?.sqlQuery ?: "")) }
    var selectedChartType by remember { mutableStateOf(chartToEdit?.chartType ?: ChartType.BAR) }
    var labelColumnIndex by remember { mutableStateOf(chartToEdit?.labelColumnIndex ?: 0) }
    var valueColumnIndex by remember { mutableStateOf(chartToEdit?.valueColumnIndex ?: 1) }
    var selectedWidth by remember { mutableStateOf(chartToEdit?.width ?: ChartWidth.FULL) }
    var chartColor by remember { mutableStateOf(chartToEdit?.color ?: Color(0xFF2196F3)) }

    // 查询状态
    var isExecuting by remember { mutableStateOf(false) }
    var queryResult by remember { mutableStateOf<QueryResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewData by remember { mutableStateOf<ChartData?>(null) }

    // 下拉菜单状态
    var dbExpanded by remember { mutableStateOf(false) }

    val selectedDb = databases.find { it.id == selectedDbId }

    // 字符串资源
    val chartTitleStr = stringResource("chart_title", language)
    val selectDatabaseStr = stringResource("select_database", language)
    val sqlStatementStr = stringResource("sql_statement", language)
    val sqlPlaceholderStr = stringResource("sql_placeholder", language)
    val chartTypeStr = stringResource("chart_type", language)
    val barChartStr = stringResource("bar_chart", language)
    val pieChartStr = stringResource("pie_chart", language)
    val lineChartStr = stringResource("line_chart", language)
    val chartWidthStr = stringResource("chart_width", language)
    val chartPreviewStr = stringResource("chart_preview", language)
    val previewStr = stringResource("preview", language)
    val saveStr = stringResource("save", language)
    val chartSelectDatabaseStr = stringResource("chart_select_database", language)
    val chartNoDataStr = stringResource("chart_no_data", language)
    val chartNoResultStr = stringResource("chart_no_result", language)
    val chartLabelColumnStr = stringResource("chart_label_column", language)
    val chartValueColumnStr = stringResource("chart_value_column", language)

    // 预设颜色
    val presetColors = listOf(
        Color(0xFF2196F3),
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF00BCD4),
        Color(0xFFFFEB3B),
        Color(0xFF795548)
    )

    // 执行查询预览
    fun executePreview() {
        if (selectedDb == null) {
            errorMessage = chartSelectDatabaseStr
            return
        }
        if (sqlQuery.text.isBlank()) {
            errorMessage = chartNoDataStr
            return
        }

        isExecuting = true
        errorMessage = null
        previewData = null

        scope.launch {
            try {
                val config = selectedDb.toDatabaseConfig()
                val client = createDatabaseClient(config)
                val status = client.context.connect()

                if (status !is ConnectionStatus.Connected) {
                    errorMessage = "连接失败: $status"
                    isExecuting = false
                    return@launch
                }

                val result = client.executor.executeQuery(
                    client.context,
                    DbExecutionChannel(
                        sql = sqlQuery.text,
                        purpose = ExecutionPurpose.UTIL
                    )
                )

                result.onSuccess { qr ->
                    queryResult = qr
                    if (qr.rows.isEmpty()) {
                        errorMessage = chartNoResultStr
                    } else {
                        val maxColumns = qr.columns.size
                        val labelIdx = labelColumnIndex.coerceIn(0, maxColumns - 1)
                        val valueIdx = valueColumnIndex.coerceIn(0, maxColumns - 1)

                        val chartResult = qr.toChartData(
                            labelColumn = labelIdx,
                            valueColumn = valueIdx,
                            title = title
                        )

                        chartResult.onSuccess { data ->
                            previewData = data.copy(color = chartColor)
                        }.onFailure { e ->
                            errorMessage = e.message
                        }
                    }
                }.onFailure { e ->
                    errorMessage = e.message ?: "查询失败"
                }

                client.context.disconnect()
            } catch (e: Exception) {
                errorMessage = e.message ?: "执行失败"
            } finally {
                isExecuting = false
            }
        }
    }

    // 保存图表
    fun handleSave() {
        if (title.isBlank() || selectedDbId.isBlank() || sqlQuery.text.isBlank()) {
            return
        }

        val config = ChartConfig(
            id = chartToEdit?.id ?: java.util.UUID.randomUUID().toString(),
            title = title,
            chartType = selectedChartType,
            databaseId = selectedDbId,
            sqlQuery = sqlQuery.text,
            labelColumnIndex = labelColumnIndex,
            valueColumnIndex = valueColumnIndex,
            color = chartColor,
            width = selectedWidth,
            position = chartToEdit?.position ?: 0
        )

        onSave(panelId, config)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (chartToEdit != null) stringResource("edit_chart", language)
                        else stringResource("add_chart", language)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource("back", language))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { handleSave() },
                        enabled = title.isNotBlank() && selectedDbId.isNotBlank() && sqlQuery.text.isNotBlank()
                    ) {
                        Text(saveStr)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.spaceMd),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
        ) {
            // 图表标题
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(chartTitleStr) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 数据库选择
            ExposedDropdownMenuBox(
                expanded = dbExpanded,
                onExpandedChange = { dbExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedDb?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(selectDatabaseStr) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dbExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = dbExpanded,
                    onDismissRequest = { dbExpanded = false }
                ) {
                    databases.forEach { db ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(db.name)
                                    Text(
                                        text = "(${db.type.name})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                selectedDbId = db.id
                                dbExpanded = false
                            }
                        )
                    }
                }
            }

            // SQL 输入
            OutlinedTextField(
                value = sqlQuery,
                onValueChange = { sqlQuery = it },
                label = { Text(sqlStatementStr) },
                placeholder = { Text(sqlPlaceholderStr) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            // 图表类型
            Text(
                text = chartTypeStr,
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                ChartType.entries.forEach { type ->
                    val isSelected = selectedChartType == type
                    val icon = when (type) {
                        ChartType.BAR -> Icons.Filled.BarChart
                        ChartType.PIE -> Icons.Filled.PieChart
                        ChartType.LINE -> Icons.Filled.ShowChart
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedChartType = type },
                        label = {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 图表颜色
            Text(
                text = stringResource("chart_color", language),
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (chartColor == color) {
                                    Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { chartColor = color }
                    )
                }
            }

            // 图表尺寸选择
            Text(
                text = chartWidthStr,
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                ChartWidth.entries.forEach { width ->
                    val isSelected = selectedWidth == width
                    val label = stringResource(width.displayNameKey, language)

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedWidth = width },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 错误信息
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(AppSpacing.spaceMd)
                    )
                }
            }

            // 列选择（当有结果时）
            val result = queryResult
            if (result != null && result.columns.size > 1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.spaceMd),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(chartLabelColumnStr, style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            result.columns.forEachIndexed { index, column ->
                                FilterChip(
                                    selected = labelColumnIndex == index,
                                    onClick = { labelColumnIndex = index },
                                    label = { Text(column.name.take(10), style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Text(chartValueColumnStr, style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            result.columns.forEachIndexed { index, column ->
                                FilterChip(
                                    selected = valueColumnIndex == index,
                                    onClick = { valueColumnIndex = index },
                                    label = { Text(column.name.take(10), style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 预览按钮
            Button(
                onClick = { executePreview() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExecuting && selectedDb != null && sqlQuery.text.isNotBlank()
            ) {
                if (isExecuting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(Icons.Filled.Preview, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(previewStr)
            }

            // 图表预览
            previewData?.let { data ->
                Text(
                    text = chartPreviewStr,
                    style = MaterialTheme.typography.labelMedium
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.spaceMd),
                        contentAlignment = Alignment.Center
                    ) {
                        when (selectedChartType) {
                            ChartType.BAR -> BarChart(
                                chartData = data,
                                modifier = Modifier.fillMaxWidth()
                            )
                            ChartType.PIE -> PieChart(
                                chartData = data,
                                modifier = Modifier.fillMaxWidth()
                            )
                            ChartType.LINE -> LineChart(
                                chartData = data,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // 提示信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Text(
                    text = "提示：SQL 查询应返回两列或多列，第一列作为标签，第二列作为数值。例如：SELECT category, COUNT(*) FROM products GROUP BY category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(AppSpacing.spaceSm)
                )
            }
        }
    }
}