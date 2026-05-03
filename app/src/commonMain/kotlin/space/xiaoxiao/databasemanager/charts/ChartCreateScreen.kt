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
import androidx.compose.material.icons.automirrored.filled.ShowChart
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
import space.xiaoxiao.databasemanager.components.AppButton
import space.xiaoxiao.databasemanager.components.AppTextButton
import space.xiaoxiao.databasemanager.components.AppTextField
import space.xiaoxiao.databasemanager.components.AppTopBar
import space.xiaoxiao.databasemanager.components.SmallLoadingIndicator
import space.xiaoxiao.databasemanager.theme.AppSpacing

/**
 * 全屏图表创建页面
 * 用于新建图表配置，从 ChartEditorScreen 的创建模式提取
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartCreateScreen(
    language: Language = Language.CHINESE,
    databases: List<DatabaseConfigInfo>,
    panelId: String,
    onNavigateBack: () -> Unit,
    onChartSaved: (ChartConfig) -> Unit
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var selectedDbId by remember { mutableStateOf("") }
    var sqlQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedChartType by remember { mutableStateOf(ChartType.BAR) }
    var labelColumnIndex by remember { mutableStateOf(0) }
    var valueColumnIndex by remember { mutableStateOf(1) }
    var selectedWidth by remember { mutableStateOf(ChartWidth.FULL) }
    var chartColor by remember { mutableStateOf(Color(0xFF2196F3)) }

    var isExecuting by remember { mutableStateOf(false) }
    var queryResult by remember { mutableStateOf<QueryResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewData by remember { mutableStateOf<ChartData?>(null) }

    var dbExpanded by remember { mutableStateOf(false) }

    val selectedDb = databases.find { it.id == selectedDbId }

    val chartTitleStr = stringResource("chart_title", language)
    val selectDatabaseStr = stringResource("select_database", language)
    val sqlStatementStr = stringResource("sql_statement", language)
    val sqlPlaceholderStr = stringResource("sql_placeholder", language)
    val chartTypeStr = stringResource("chart_type", language)
    val chartWidthStr = stringResource("chart_width", language)
    val chartPreviewStr = stringResource("chart_preview", language)
    val previewStr = stringResource("preview", language)
    val saveStr = stringResource("save", language)
    val chartSelectDatabaseStr = stringResource("chart_select_database", language)
    val chartNoDataStr = stringResource("chart_no_data", language)
    val chartNoResultStr = stringResource("chart_no_result", language)
    val chartLabelColumnStr = stringResource("chart_label_column", language)
    val chartValueColumnStr = stringResource("chart_value_column", language)

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

    fun handleSave() {
        if (title.isBlank() || selectedDbId.isBlank() || sqlQuery.text.isBlank()) {
            return
        }

        val config = ChartConfig(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            chartType = selectedChartType,
            databaseId = selectedDbId,
            sqlQuery = sqlQuery.text,
            labelColumnIndex = labelColumnIndex,
            valueColumnIndex = valueColumnIndex,
            color = chartColor,
            width = selectedWidth,
            position = 0
        )

        onChartSaved(config)
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource("add_chart", language),
                onNavigationClick = onNavigateBack,
                actions = {
                    AppTextButton(
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
            AppTextField(
                value = title,
                onValueChange = { title = it },
                label = chartTitleStr,
                singleLine = true
            )

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
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
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
                        ChartType.LINE -> Icons.AutoMirrored.Filled.ShowChart
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

            Text(
                text = stringResource("chart_color", language),
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceXs)
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
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
                    ) {
                        Text(chartLabelColumnStr, style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceXs)
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
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceXs)
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

            AppButton(
                onClick = { executePreview() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExecuting && selectedDb != null && sqlQuery.text.isNotBlank()
            ) {
                if (isExecuting) {
                    SmallLoadingIndicator()
                    Spacer(modifier = Modifier.width(AppSpacing.spaceSm))
                } else {
                    Icon(Icons.Filled.Preview, contentDescription = null)
                    Spacer(modifier = Modifier.width(AppSpacing.spaceSm))
                }
                Text(previewStr)
            }

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

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Text(
                    text = stringResource("chart_hint_text", language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(AppSpacing.spaceSm)
                )
            }
        }
    }
}
