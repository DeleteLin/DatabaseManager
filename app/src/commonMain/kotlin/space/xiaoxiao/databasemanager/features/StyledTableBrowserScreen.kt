package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.AppPillTabRow
import space.xiaoxiao.databasemanager.components.CardVariant
import space.xiaoxiao.databasemanager.components.DatabaseTypeIcon
import space.xiaoxiao.databasemanager.theme.AppSpacing
import kotlinx.coroutines.launch
import space.xiaoxiao.databasemanager.core.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledTableBrowserScreen(
    language: Language = Language.CHINESE,
    databases: List<DatabaseConfigInfo>,
    selectedDatabaseId: String?,
    onDatabaseSelected: (String) -> Unit
) {
    val selectedDatabase = databases.find { it.id == selectedDatabaseId }
    val viewModel = remember { DatabaseViewModel() }
    // 弹窗内部“预览/刷新服务器库列表”的连接，不直接影响主页面的 tables 展示
    val dialogViewModel = remember { DatabaseViewModel() }
    val scope = rememberCoroutineScope()

    var selectedTableName by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var searchText by remember { mutableStateOf("") }

    // 窄屏模式下的导航状态：true=显示详情，false=显示列表
    var showDetailOnNarrowScreen by remember { mutableStateOf(false) }

    val tables by derivedStateOf { viewModel.tables }
    val tableSchema by derivedStateOf { viewModel.tableSchema }
    val databaseType by derivedStateOf { viewModel.databaseType }
    val isRedis by derivedStateOf { databaseType == DatabaseType.REDIS }
    val indexes by derivedStateOf { viewModel.indexes }
    val tableStats by derivedStateOf { viewModel.tableStats }
    val connectionState by derivedStateOf { viewModel.connectionState }
    val isManagingTable by derivedStateOf { viewModel.isManagingTable }

    val dialogConnectionState by derivedStateOf { dialogViewModel.connectionState }
    val dialogServerDatabases by derivedStateOf { dialogViewModel.serverDatabases }

    // 对话框状态
    var showCreateTableDialog by remember { mutableStateOf(false) }
    var showFieldEditorScreen by remember { mutableStateOf(false) }
    var showCreateIndexDialog by remember { mutableStateOf(false) }
    var confirmDialogState by remember { mutableStateOf<ConfirmDialogState?>(null) }
    var showDatabaseSwitchDialog by remember { mutableStateOf(false) }
    var tempSelectedDatabaseId by remember { mutableStateOf<String?>(selectedDatabaseId) }
    var tempSelectedServerDatabaseName by remember { mutableStateOf<String?>(null) }

    // 编辑字段状态
    var editingColumn by remember { mutableStateOf<ColumnDefinition?>(null) }
    val isEditMode by derivedStateOf { editingColumn != null }

    val filteredTables = tables.filter { it.name.contains(searchText, ignoreCase = true) }

    // 预计算字符串资源
    val deleteFieldTitle = stringResource("delete_field", language)
    val dropIndexTitle = stringResource("drop_index", language)
    val confirmDropIndexTemplate = stringResource("confirm_drop_index", language)
    val confirmDropColumnTemplate = stringResource("confirm_drop_column", language)

    // 确保 ViewModel 在离开屏幕时释放资源
    DisposableEffect(viewModel) {
        onDispose {
            scope.launch { viewModel.close() }
        }
    }

    DisposableEffect(dialogViewModel) {
        onDispose {
            scope.launch { dialogViewModel.close() }
        }
    }

    LaunchedEffect(selectedDatabaseId) {
        if (selectedDatabase != null) {
            // 当主页面已连接到同一个实例时，不重复 disconnect/connect，避免闪空窗期。
            if (selectedDatabaseId != null && viewModel.currentConfigId == selectedDatabaseId) {
                viewModel.loadTables()
                viewModel.loadServerDatabases()
            } else {
                // selectedDatabaseId 发生变化时必须重连并重新加载表
                // 否则可能拿到旧连接的元数据，表现为“切换后表为空”。
                viewModel.disconnect()
                viewModel.connect(selectedDatabase)
                viewModel.loadTables()
                viewModel.loadServerDatabases()
            }
        }
    }

    // 弹窗打开/选择“实例(第一级数据库)”时：只刷新弹窗内的服务器库列表
    LaunchedEffect(showDatabaseSwitchDialog, tempSelectedDatabaseId) {
        if (!showDatabaseSwitchDialog) return@LaunchedEffect
        val targetId = tempSelectedDatabaseId ?: return@LaunchedEffect
        val targetConfig = databases.find { it.id == targetId } ?: return@LaunchedEffect

        // 先清空再连接/拉取，避免切到 Redis 等类型时仍短暂显示上一实例的服务器库
        dialogViewModel.clearServerDatabasesList()

        if (dialogViewModel.currentConfigId != targetId) {
            dialogViewModel.disconnect()
            dialogViewModel.connect(targetConfig)
        }
        dialogViewModel.loadServerDatabases()
        tempSelectedServerDatabaseName = dialogViewModel.currentDatabaseName
    }

    LaunchedEffect(selectedTableName, selectedTab) {
        selectedTableName?.let { tableName ->
            if (connectionState == ConnectionUiState.CONNECTED && !isRedis) {
                when (selectedTab) {
                    0 -> viewModel.loadTableSchema(tableName)
                    1 -> viewModel.loadIndexes(tableName)
                    2 -> viewModel.loadTableStats(tableName)
                    3 -> viewModel.loadTableSchema(tableName) // 外键信息在表结构中
                }
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部工具栏 - 重新设计的响应式布局
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.spaceMd),
            ) {
                // 顶部一行：数据库切换 + 刷新 + 添加
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            tempSelectedDatabaseId = selectedDatabaseId
                            tempSelectedServerDatabaseName = viewModel.currentDatabaseName
                            showDatabaseSwitchDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = buildString {
                                    append(selectedDatabase?.name ?: stringResource("not_selected", language))
                                    val serverDb = viewModel.currentDatabaseName
                                    if (!serverDb.isNullOrBlank()) {
                                        append(" / ")
                                        append(serverDb)
                                    }
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }

                    // 刷新按钮
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.loadTables()
                                selectedTableName?.let { viewModel.loadTableSchema(it) }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = stringResource("refresh", language),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 新建表按钮（非 Redis 模式下显示）
                    if (connectionState == ConnectionUiState.CONNECTED && !isRedis) {
                        IconButton(
                            onClick = { showCreateTableDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = stringResource("create_table", language),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                    }
                }
            }
        }
        if (selectedDatabase == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource("no_database_selected", language),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (connectionState == ConnectionUiState.CONNECTING) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (connectionState == ConnectionUiState.FAILED) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource("connection_failed", language),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            // 使用 BoxWithConstraints 检测可用宽度
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth > 500.dp

                // 根据屏幕宽度选择布局
                if (isWideScreen) {
                    // 宽屏：左右分栏布局
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (isRedis) {
                            // Redis 数据库不支持表操作提示
                            AppCard(
                                modifier = Modifier.widthIn(min = 180.dp, max = 220.dp).fillMaxHeight().padding(6.dp),
                                variant = CardVariant.Default
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Redis 数据库",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "不支持表操作",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "请使用查询页面\n执行 Redis 命令",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            TableListPanel(
                            modifier = Modifier.widthIn(min = 180.dp, max = 220.dp).fillMaxHeight().padding(6.dp),
                            tables = tables,
                            filteredTables = filteredTables,
                            searchText = searchText,
                            onSearchTextChange = { searchText = it },
                            selectedTableName = selectedTableName,
                            onTableSelected = { tableName ->
                                selectedTableName = tableName
                            },
                            language = language
                        )
                        }

                        TableDetailPanel(
                            modifier = Modifier.weight(1f).fillMaxHeight().padding(6.dp),
                            selectedTableName = selectedTableName,
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            tableSchema = tableSchema,
                            indexes = indexes,
                            tableStats = tableStats,
                            language = language,
                            showBackButton = false,
                            onBack = {},
                            onAddField = { showFieldEditorScreen = true },
                            onEditField = { column ->
                                editingColumn = column
                                showFieldEditorScreen = true
                            },
                            onDropField = { columnName ->
                                confirmDialogState = ConfirmDialogState(
                                    title = deleteFieldTitle,
                                    message = confirmDropColumnTemplate.replace("{column}", columnName),
                                    onConfirm = {
                                        scope.launch {
                                            selectedTableName?.let { viewModel.dropColumn(it, columnName) }
                                        }
                                    }
                                )
                            },
                            onCreateIndex = { showCreateIndexDialog = true },
                            onDropIndex = { indexName ->
                                confirmDialogState = ConfirmDialogState(
                                    title = dropIndexTitle,
                                    message = confirmDropIndexTemplate.replace("{index}", indexName),
                                    onConfirm = {
                                        scope.launch {
                                            selectedTableName?.let { viewModel.dropIndex(it, indexName) }
                                        }
                                    }
                                )
                            }
                        )
                    }
                } else {
                    // 窄屏：全屏切换模式
                    if (showDetailOnNarrowScreen && selectedTableName != null) {
                        // 显示表详情
                        TableDetailPanel(
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            selectedTableName = selectedTableName,
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            tableSchema = tableSchema,
                            indexes = indexes,
                            tableStats = tableStats,
                            language = language,
                            showBackButton = true,
                            onBack = { showDetailOnNarrowScreen = false },
                            onAddField = { showFieldEditorScreen = true },
                            onEditField = { column ->
                                editingColumn = column
                                showFieldEditorScreen = true
                            },
                            onDropField = { columnName ->
                                confirmDialogState = ConfirmDialogState(
                                    title = deleteFieldTitle,
                                    message = confirmDropColumnTemplate.replace("{column}", columnName),
                                    onConfirm = {
                                        scope.launch {
                                            selectedTableName?.let { viewModel.dropColumn(it, columnName) }
                                        }
                                    }
                                )
                            },
                            onCreateIndex = { showCreateIndexDialog = true },
                            onDropIndex = { indexName ->
                                confirmDialogState = ConfirmDialogState(
                                    title = dropIndexTitle,
                                    message = confirmDropIndexTemplate.replace("{index}", indexName),
                                    onConfirm = {
                                        scope.launch {
                                            selectedTableName?.let { viewModel.dropIndex(it, indexName) }
                                        }
                                    }
                                )
                            }
                        )
                    } else {
                        // 显示表列表
                        if (isRedis) {
                            // Redis 数据库不支持表操作提示
                            AppCard(
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                variant = CardVariant.Default
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Redis 数据库",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "不支持表操作",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "请使用查询页面执行 Redis 命令",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            TableListPanel(
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                tables = tables,
                                filteredTables = filteredTables,
                                searchText = searchText,
                                onSearchTextChange = { searchText = it },
                                selectedTableName = selectedTableName,
                                onTableSelected = { tableName ->
                                    selectedTableName = tableName
                                    showDetailOnNarrowScreen = true
                                },
                                language = language
                            )
                        }
                    }
                }
            }
        }
    }

    // 新建表对话框
    if (showCreateTableDialog) {
        TableCreateDialog(
            language = language,
            onDismiss = { showCreateTableDialog = false },
            onCreate = { definition ->
                scope.launch {
                    if (viewModel.createTable(definition)) {
                        showCreateTableDialog = false
                    }
                }
            }
        )
    }

    // 字段编辑二级页面
    if (showFieldEditorScreen && selectedTableName != null) {
        val currentTableName = selectedTableName!!
        val existingColumns = tableSchema?.columns?.map { it.name } ?: emptyList()
        FieldEditorScreen(
            language = language,
            tableName = currentTableName,
            isEditMode = isEditMode,
            initialColumn = editingColumn,
            existingColumnNames = if (isEditMode) {
                existingColumns.filter { it != editingColumn?.name }
            } else {
                existingColumns
            },
            onNavigateBack = {
                editingColumn = null
                showFieldEditorScreen = false
            },
            onSave = { columnDef ->
                val oldColumn = editingColumn
                val success = if (isEditMode && oldColumn != null) {
                    viewModel.modifyColumn(
                        currentTableName,
                        ColumnModification(
                            oldName = oldColumn.name,
                            newName = columnDef.name,
                            typeName = columnDef.typeName,
                            isNullable = columnDef.isNullable,
                            defaultValue = columnDef.defaultValue,
                            comment = columnDef.comment,
                            charset = columnDef.charset
                        )
                    )
                } else {
                    viewModel.addColumn(currentTableName, columnDef)
                }
                if (success) {
                    editingColumn = null
                    showFieldEditorScreen = false
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(viewModel.lastErrorMessage ?: "操作失败"))
                }
            }
        )
        return // 全屏显示，不渲染下面的内容
    }

    // 创建索引对话框
    if (showCreateIndexDialog && selectedTableName != null) {
        val currentTableName = selectedTableName!!
        IndexCreateDialog(
            language = language,
            columns = tableSchema?.columns?.map { it.name } ?: emptyList(),
            onDismiss = { showCreateIndexDialog = false },
            onCreate = { indexName, columns, isUnique ->
                scope.launch {
                    if (viewModel.createIndex(currentTableName, indexName, columns, isUnique)) {
                        showCreateIndexDialog = false
                    }
                }
            }
        )
    }

    // 确认对话框
    confirmDialogState?.let { state ->
        AlertDialog(
            onDismissRequest = { confirmDialogState = null },
            title = { Text(state.title) },
            text = { Text(state.message) },
            confirmButton = {
                Button(
                    onClick = {
                        state.onConfirm()
                        confirmDialogState = null
                    }
                ) {
                    Text(stringResource("ok", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDialogState = null }) {
                    Text(stringResource("cancel", language))
                }
            }
        )
    }

    // 数据库/服务器库切换对话框（样式参考查询页卡片列表弹窗）
    if (showDatabaseSwitchDialog) {
        AlertDialog(
            onDismissRequest = { showDatabaseSwitchDialog = false },
            title = { Text(text = stringResource("select_database", language)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 数据库列表
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(databases) { db ->
                            val isSelected = tempSelectedDatabaseId == db.id
                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    // 同步清空，避免 LaunchedEffect 晚一帧时仍看到旧列表
                                    dialogViewModel.clearServerDatabasesList()
                                    tempSelectedDatabaseId = db.id
                                    tempSelectedServerDatabaseName =
                                        if (db.id == selectedDatabaseId) viewModel.currentDatabaseName else null
                                    // 第一步：只刷新弹窗内服务器库列表，不切换主页面已选实例（等待你在第二步点具体服务器库）
                                },
                                variant = if (isSelected) CardVariant.Elevated else CardVariant.Default
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppSpacing.spaceLg),
                                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    DatabaseTypeIcon(
                                        databaseType = db.type,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = db.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${db.host}:${db.port}/${db.database}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 服务器数据库列表：第二步真正选择时再切主页面实例并刷新 tables
                    if (tempSelectedDatabaseId != null) {
                        val tempConfig = databases.find { it.id == tempSelectedDatabaseId }
                        val isTempRedis = tempConfig?.type == DatabaseType.REDIS
                        Text(
                            text = stringResource("server_database_label", language),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isTempRedis) {
                            Text(
                                text = stringResource("redis_no_database_management", language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(dialogServerDatabases) { dbName ->
                                val isSelected = tempSelectedServerDatabaseName == dbName
                                AppCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        tempSelectedServerDatabaseName = dbName
                                        selectedTableName = null
                                        selectedTab = 0
                                        // 确认切换服务器库后，清空筛选条件，确保表列表按新库全量刷新展示
                                        searchText = ""
                                        showDetailOnNarrowScreen = false
                                        showDatabaseSwitchDialog = false

                                        scope.launch {
                                            val targetInstanceId = tempSelectedDatabaseId ?: return@launch
                                            val targetConfig = databases.find { it.id == targetInstanceId } ?: return@launch

                                            // 主页面按选中的“实例”重新建立连接
                                            if (viewModel.currentConfigId != targetInstanceId) {
                                                viewModel.disconnect()
                                                viewModel.connect(targetConfig)
                                            }

                                            // 切到选中的“服务器库”
                                            if (viewModel.connectionState == ConnectionUiState.CONNECTED &&
                                                viewModel.databaseType != DatabaseType.REDIS
                                            ) {
                                                viewModel.switchServerDatabase(dbName)
                                            }

                                            // 同步外部选中状态，用于主页面显示正确的实例名
                                            onDatabaseSelected(targetInstanceId)
                                        }
                                    },
                                    variant = if (isSelected) CardVariant.Elevated else CardVariant.Default
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(AppSpacing.spaceLg),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = dbName,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showDatabaseSwitchDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = stringResource("cancel", language))
                }
            }
        )
    }

    // 加载中遮罩

    if (isManagingTable) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

/**
 * 确认对话框状态
 */
private data class ConfirmDialogState(
    val title: String,
    val message: String,
    val onConfirm: () -> Unit
)

// ==================== 面板组件 ====================

/**
 * 表列表面板
 */
@Composable
private fun TableListPanel(
    modifier: Modifier = Modifier,
    tables: List<TableInfo>,
    filteredTables: List<TableInfo>,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedTableName: String?,
    onTableSelected: (String) -> Unit,
    language: Language
) {
    AppCard(modifier = modifier, variant = CardVariant.Surface) {
        Column {
            // 搜索框
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = { Text(stringResource("search", language) + "...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                singleLine = true
            )

            Text(
                text = "${stringResource("select_table", language)} (${tables.size})",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )

            if (tables.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource("no_tables", language),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                LazyColumn {
                    items(filteredTables) { table ->
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    table.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            selected = selectedTableName == table.name,
                            onClick = { onTableSelected(table.name) },
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 表详情面板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableDetailPanel(
    modifier: Modifier = Modifier,
    selectedTableName: String?,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tableSchema: TableSchema?,
    indexes: List<IndexInfo>,
    tableStats: TableStats?,
    language: Language,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onAddField: () -> Unit,
    onEditField: (ColumnDefinition) -> Unit,
    onDropField: (String) -> Unit,
    onCreateIndex: () -> Unit,
    onDropIndex: (String) -> Unit
) {
    AppCard(modifier = modifier, variant = CardVariant.Surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 窄屏模式下显示返回按钮和表名
            if (showBackButton && selectedTableName != null) {
                TopAppBar(
                    title = { Text(selectedTableName) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = stringResource("back", language))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            val currentTableName = selectedTableName
            if (currentTableName == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource("select_table_to_show_data", language),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Tab 栏
                val tabs = listOf(
                    stringResource("fields", language),
                    stringResource("indexes", language),
                    stringResource("statistics", language),
                    stringResource("foreign_keys", language)
                )
                AppPillTabRow(
                    tabs = tabs,
                    selectedIndex = selectedTab,
                    onSelectIndex = { onTabSelected(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Tab 内容
                when (selectedTab) {
                    0 -> FieldsTab(
                        tableName = currentTableName,
                        schema = tableSchema,
                        language = language,
                        onAddField = onAddField,
                        onEditField = onEditField,
                        onDropField = onDropField
                    )
                    1 -> IndexesTab(
                        tableName = currentTableName,
                        indexes = indexes,
                        language = language,
                        onCreateIndex = onCreateIndex,
                        onDropIndex = onDropIndex
                    )
                    2 -> StatsTab(
                        tableName = currentTableName,
                        stats = tableStats,
                        language = language
                    )
                    3 -> ForeignKeysTab(
                        schema = tableSchema,
                        language = language
                    )
                }
            }
        }
    }
}

// ==================== Tab 组件 ====================

@Composable
private fun FieldsTab(
    tableName: String,
    schema: TableSchema?,
    language: Language,
    onAddField: () -> Unit,
    onEditField: (ColumnDefinition) -> Unit,
    onDropField: (String) -> Unit
) {
    BoxWithConstraints {
        val isWideScreen = maxWidth > 400.dp
        Column(modifier = Modifier.fillMaxSize().padding(if (isWideScreen) 8.dp else 6.dp)) {
            // 工具栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource("fields", language) + ": ${schema?.columns?.size ?: 0}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (isWideScreen) {
                    Button(onClick = onAddField) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource("add_field", language))
                    }
                } else {
                    IconButton(onClick = onAddField, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource("add_field", language), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            schema?.let { tableSchema ->
                if (tableSchema.columns.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource("no_data", language))
                    }
                } else {
                    LazyColumn {
                        items(tableSchema.columns) { column ->
                            FieldItem(
                                column = column,
                                isPrimaryKey = column.isPrimaryKey || column.name in tableSchema.primaryKeys,
                                language = language,
                                onEdit = { onEditField(column) },
                                onDelete = { onDropField(column.name) }
                            )
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun FieldItem(
    column: ColumnDefinition,
    isPrimaryKey: Boolean,
    language: Language,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimaryKey)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 字段信息
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = column.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isPrimaryKey) {
                        Spacer(modifier = Modifier.width(6.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource("pk_badge", language)) },
                            modifier = Modifier.height(22.dp)
                        )
                    }
                    if (column.isAutoIncrement) {
                        Spacer(modifier = Modifier.width(3.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource("ai_badge", language)) },
                            modifier = Modifier.height(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = column.typeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!column.isNullable) {
                        Text(
                            text = stringResource("not_null", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    column.defaultValue?.let { default ->
                        Text(
                            text = "${stringResource("field_default", language)}: $default",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                column.comment?.let { comment ->
                    if (comment.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${stringResource("field_comment", language)}: $comment",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 操作按钮
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource("edit", language), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource("delete", language), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun IndexesTab(
    tableName: String,
    indexes: List<IndexInfo>,
    language: Language,
    onCreateIndex: () -> Unit,
    onDropIndex: (String) -> Unit
) {
    BoxWithConstraints {
        val isWideScreen = maxWidth > 400.dp
        Column(modifier = Modifier.fillMaxSize().padding(if (isWideScreen) 8.dp else 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource("indexes", language) + ": ${indexes.size}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (isWideScreen) {
                    Button(onClick = onCreateIndex) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource("create_index", language))
                    }
                } else {
                    IconButton(onClick = onCreateIndex, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource("create_index", language), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (indexes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource("no_data", language))
                }
            } else {
                LazyColumn {
                    items(indexes) { index ->
                        IndexItem(
                            index = index,
                            language = language,
                            onDelete = { onDropIndex(index.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IndexItem(
    index: IndexInfo,
    language: Language,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (index.isPrimary)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = index.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (index.isPrimary) {
                        Spacer(modifier = Modifier.width(6.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource("primary", language)) },
                            modifier = Modifier.height(22.dp)
                        )
                    }
                    if (index.isUnique && !index.isPrimary) {
                        Spacer(modifier = Modifier.width(3.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(stringResource("unique", language)) },
                            modifier = Modifier.height(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${stringResource("index_columns", language)}: ${index.columns.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stringResource("index_type", language)}: ${index.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!index.isPrimary) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource("delete", language), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun StatsTab(
    tableName: String,
    stats: TableStats?,
    language: Language
) {
    BoxWithConstraints {
        val isWideScreen = maxWidth > 400.dp
        val contentPadding = if (isWideScreen) 16.dp else 12.dp

        Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            Text(
                text = stringResource("statistics", language),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            stats?.let { tableStats ->
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = CardVariant.Default
                ) {
                    Column(modifier = Modifier.padding(if (isWideScreen) 16.dp else 12.dp)) {
                        StatRow(stringResource("table_name", language), tableStats.tableName)
                        StatRow(stringResource("row_count", language), formatNumber(tableStats.rowCount))
                        StatRow(stringResource("data_size", language), formatBytes(tableStats.dataSize))
                        StatRow(stringResource("index_size", language), formatBytes(tableStats.indexSize))
                        tableStats.autoIncrementValue?.let { autoInc ->
                            StatRow(stringResource("field_auto_increment", language), formatNumber(autoInc))
                        }
                        tableStats.createTime?.let { time ->
                            StatRow(stringResource("create_time", language), time)
                        }
                        tableStats.updateTime?.let { time ->
                            StatRow(stringResource("update_time", language), time)
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    HorizontalDivider()
}

@Composable
private fun ForeignKeysTab(
    schema: TableSchema?,
    language: Language
) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text(
            text = stringResource("foreign_keys", language),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        val foreignKeys = schema?.foreignKeys ?: emptyList()

        if (foreignKeys.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource("no_data", language))
            }
        } else {
            LazyColumn {
                items(foreignKeys) { fk ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        variant = CardVariant.Default
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${stringResource("fk_column", language)}: ${fk.columnName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "→ ${fk.referencedTable}.${fk.referencedColumn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 辅助函数 ====================

private fun formatNumber(value: Long): String {
    return String.format("%,d", value)
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }
}
