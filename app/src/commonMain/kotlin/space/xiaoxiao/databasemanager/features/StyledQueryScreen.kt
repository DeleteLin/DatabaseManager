package space.xiaoxiao.databasemanager.features

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.components.StyledEmptyState
import space.xiaoxiao.databasemanager.components.StyledErrorState
import space.xiaoxiao.databasemanager.components.AppIcons
import space.xiaoxiao.databasemanager.components.CodeEditor
import space.xiaoxiao.databasemanager.components.EditorLanguage
import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.CardVariant
import space.xiaoxiao.databasemanager.components.DatabaseTypeIcon
import space.xiaoxiao.databasemanager.theme.AppSpacing
import space.xiaoxiao.databasemanager.storage.AiConfigStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import space.xiaoxiao.databasemanager.core.*
import space.xiaoxiao.databasemanager.utils.KeyboardUtils
import space.xiaoxiao.databasemanager.utils.FileUtils
import space.xiaoxiao.databasemanager.utils.Platform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledQueryScreen(
    language: Language = Language.CHINESE,
    databases: List<DatabaseConfigInfo> = emptyList(),
    queryHistoryStorage: QueryHistoryStorage? = null,
    querySessionStorage: QuerySessionStorage? = null,
    appConfigStorage: space.xiaoxiao.databasemanager.config.AppConfigStorage? = null,
    aiConfigStorage: AiConfigStorage? = null,
    onNavigateToAiConfig: (() -> Unit)? = null
) {
    val tabManager = rememberQueryTabManager(queryHistoryStorage, querySessionStorage)
    val scope = rememberCoroutineScope()
    val tabs by tabManager.tabs.collectAsState(emptyList())
    val selectedTabId by tabManager.selectedTabId.collectAsState(null)

    var showCreateTabDialog by remember { mutableStateOf(false) }
    var showDbSelector by remember { mutableStateOf(false) }

    // 启动时从 AppConfig 恢复会话（优先）
    LaunchedEffect(Unit) {
        if (tabs.isEmpty()) {
            // 优先从 AppConfig 恢复（新方式）
            val appConfig = appConfigStorage?.loadConfig()
            if (appConfig?.openQueryTabs?.isNotEmpty() == true) {
                tabManager.restoreFromAppConfig(appConfig)
            } else {
                // 否则使用旧方式从 sessionStorage 恢复
                tabManager.restoreSessions()
            }
        }
    }

    // 当 tabs 或 selectedTabId 变化时，保存状态到 AppConfig
    LaunchedEffect(tabs, selectedTabId) {
        if (tabs.isNotEmpty()) {
            appConfigStorage?.updateQueryTabs(tabManager.toAppConfigTabs(), selectedTabId)
        } else {
            // 如果没有 tab 了，清空存储
            appConfigStorage?.clearQueryTabs()
        }
    }

    // 确保离开屏幕时清理所有 ViewModel 资源
    DisposableEffect(tabManager) {
        onDispose {
            tabManager.cleanup()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        QueryTabBar(
            tabs = tabs,
            selectedTabId = selectedTabId,
            onSelectTab = { tabManager.selectTab(it) },
            onCloseTab = { tabManager.closeTab(it) },
            onAddTab = { showCreateTabDialog = true },
            language = language,
            modifier = Modifier.fillMaxWidth()
        )

        selectedTabId?.let { tabId ->
            val tab = tabManager.getTab(tabId)
            val viewModel = tabManager.getTabViewModel(tabId)
            if (tab != null && viewModel != null) {
                // 从 databases 列表获取最新的配置（如果 databaseId 存在）
                val latestConfig = tab.databaseId?.let { dbId ->
                    databases.find { it.id == dbId }
                } ?: tab.databaseConfig

                QueryTabContent(
                    tab = tab,
                    viewModel = viewModel,
                    databases = databases,
                    latestDatabaseConfig = latestConfig,
                    onUpdateTab = { update -> tabManager.updateTab(tabId, update) },
                    onShowDbSelector = { showDbSelector = true },
                    language = language,
                    aiConfigStorage = aiConfigStorage,
                    onNavigateToAiConfig = onNavigateToAiConfig
                )
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddBox,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = stringResource("no_tab_hint", language),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Tab 创建对话框
    CreateTabDialog(
        showDialog = showCreateTabDialog,
        onDismissRequest = { showCreateTabDialog = false },
        onCreate = { config, sessionName ->
            tabManager.createTab(
                databaseId = config.id,
                databaseConfig = config,
                sessionName = sessionName
            )
            showCreateTabDialog = false
        },
        databases = databases,
        language = language
    )

    // 数据库选择对话框
    if (showDbSelector) {
        val currentTabId = selectedTabId
        if (currentTabId != null) {
            DatabaseSelectorForTabDialog(
                showDialog = showDbSelector,
                onDismissRequest = { showDbSelector = false },
                onSelect = { config ->
                    tabManager.updateTab(currentTabId) {
                        it.copy(databaseId = config.id, databaseConfig = config)
                    }
                    tabManager.getTabViewModel(currentTabId)?.let { vm ->
                        scope.launch { vm.connect(config) }
                    }
                    showDbSelector = false
                },
                databases = databases,
                language = language
            )
        }
    }
}

@Composable
fun QueryTabBar(
    tabs: List<QueryTab>,
    selectedTabId: String?,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onAddTab: () -> Unit,
    language: Language,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        BoxWithConstraints {
            val isWideScreen = maxWidth > 400.dp
            Row(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 6.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    QueryTabItem(
                        tab = tab,
                        isSelected = tab.id == selectedTabId,
                        onSelect = { onSelectTab(tab.id) },
                        onClose = { onCloseTab(tab.id) },
                        language = language,
                        isWideScreen = isWideScreen
                    )
                }

                // 添加 Tab 按钮：对齐到胶囊 Tab 的高度/形状
                val tabShape = RoundedCornerShape(6.dp)
                Surface(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(tabShape)
                        .clickable(onClick = onAddTab),
                    color = MaterialTheme.colorScheme.surface,
                    shape = tabShape
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource("new_tab", language),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueryTabItem(
    tab: QueryTab,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit,
    language: Language,
    isWideScreen: Boolean = true
) {
    Surface(
        modifier = Modifier.height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onSelect),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (isWideScreen) 10.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 连接状态图标
            Icon(
                imageVector = when (tab.connectionState) {
                    ConnectionUiState.CONNECTED -> Icons.Filled.CheckCircle
                    ConnectionUiState.CONNECTING -> Icons.Filled.Loop
                    ConnectionUiState.FAILED -> Icons.Filled.Error
                    else -> Icons.Filled.RadioButtonUnchecked
                },
                contentDescription = null,
                tint = when (tab.connectionState) {
                    ConnectionUiState.CONNECTED -> MaterialTheme.colorScheme.primary
                    ConnectionUiState.FAILED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(14.dp)
            )
            // 事务状态指示器
            if (tab.isInTransaction) {
                Box(
                    modifier = Modifier.size(6.dp)
                        .background(MaterialTheme.colorScheme.error, RoundedCornerShape(3.dp))
                )
            }
            Text(
                text = tab.getDisplayTitle(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = if (isWideScreen) 140.dp else 100.dp)
            )
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource("close_tab", language),
                modifier = Modifier.size(14.dp).clickable(onClick = onClose),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryTabContent(
    tab: QueryTab,
    viewModel: DatabaseViewModel,
    databases: List<DatabaseConfigInfo>,
    latestDatabaseConfig: DatabaseConfigInfo?,
    onUpdateTab: ((QueryTab) -> QueryTab) -> Unit,
    onShowDbSelector: () -> Unit,
    language: Language,
    aiConfigStorage: AiConfigStorage? = null,
    onNavigateToAiConfig: (() -> Unit)? = null
) {
    // 使用 TextFieldValue 支持获取选中文本
    // 使用 tab.id 作为 key，确保切换 Tab 时正确重置
    var textFieldValue by remember(tab.id) { mutableStateOf(TextFieldValue(tab.sql)) }
    val scope = rememberCoroutineScope()

    val connectionState = viewModel.connectionState
    val isExecuting = viewModel.isExecuting
    val lastQueryResult = viewModel.lastQueryResult
    val lastUpdateResult = viewModel.lastUpdateResult
    val lastErrorMessage = viewModel.lastErrorMessage
    val databaseType = viewModel.databaseType

    val isRedis = databaseType == DatabaseType.REDIS

    // 同步 Tab 的 SQL 内容到 TextFieldValue - 仅在内容真正变化时更新
    LaunchedEffect(tab.id, tab.sql) {
        if (tab.sql != textFieldValue.text) {
            textFieldValue = TextFieldValue(tab.sql, selection = textFieldValue.selection)
        }
    }

    // 连接数据库 - 使用最新配置
    LaunchedEffect(latestDatabaseConfig) {
        if (latestDatabaseConfig != null) {
            // 只有在未连接时才连接，避免重复连接
            if (viewModel.connectionState != ConnectionUiState.CONNECTED) {
                viewModel.connect(latestDatabaseConfig)
                // 更新 tab 中的配置引用
                onUpdateTab { it.copy(databaseConfig = latestDatabaseConfig) }
            } else {
                // 已连接但配置发生了变化（例如用户切换了数据库）
                // 检查是否需要更新配置引用
                val currentConfigId = tab.databaseId
                val newConfigId = latestDatabaseConfig.id
                if (currentConfigId != newConfigId) {
                    // 配置 ID 不同，需要重新连接
                    viewModel.disconnect()
                    viewModel.connect(latestDatabaseConfig)
                    onUpdateTab { it.copy(databaseConfig = latestDatabaseConfig, databaseId = newConfigId) }
                }
            }
        }
    }

    // 同步 ViewModel 状态到 Tab（连接状态）
    LaunchedEffect(viewModel.connectionState) {
        onUpdateTab { it.copy(connectionState = viewModel.connectionState) }
    }

    // 同步 ViewModel 事务状态到 Tab
    LaunchedEffect(viewModel.isInTransaction) {
        onUpdateTab { it.copy(isInTransaction = viewModel.isInTransaction) }
    }

    // 使用 Box 实现层叠布局：编辑区在下，结果面板在上
    Box(modifier = Modifier.fillMaxSize()) {
        // 编辑区（底层）
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(bottom = if (tab.isResultExpanded) 0.dp else 48.dp)
        ) {
            // 命令编辑卡片
            BoxWithConstraints(
                modifier = Modifier.weight(1f)
            ) {
                val isWideScreen = maxWidth > 360.dp
                val cardPadding = if (isWideScreen) AppSpacing.spaceLg else AppSpacing.spaceMd

                // 注意：卡片外部不使用 padding，让编辑器占满整个区域以最大化代码编写空间
                // 内部内容通过 Column 的 padding 来控制边距
                AppCard(
                    modifier = Modifier.fillMaxSize(),
                    variant = CardVariant.Default
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(cardPadding)) {
                        // 执行按钮行（移到输入框上方，避免软键盘遮挡）
                        var rememberedSelectedText by remember { mutableStateOf("") }

                        LaunchedEffect(textFieldValue.selection) {
                            if (textFieldValue.selection.length > 0) {
                                val start = minOf(textFieldValue.selection.start, textFieldValue.selection.end)
                                val end = maxOf(textFieldValue.selection.start, textFieldValue.selection.end)
                                rememberedSelectedText = textFieldValue.text.substring(start, end)
                            }
                        }

                        val hasSelection = rememberedSelectedText.isNotBlank()

                        // 操作栏 - 两个执行按钮 + 三点菜单
                        var showMenu by remember { mutableStateOf(false) }
                        var showTemplateSelector by remember { mutableStateOf(false) }
                        var showAiGenerator by remember { mutableStateOf(false) }

                        // 获取当前数据库类型和表结构
                        val currentDatabaseType = latestDatabaseConfig?.type ?: DatabaseType.MYSQL
                        val currentTableSchema = viewModel.tableSchema?.let { schema ->
                            schema.columns.joinToString("\n") { col ->
                                "${col.name}: ${col.typeName}${if (col.isNullable) " NULL" else " NOT NULL"}"
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 执行选中按钮
                            OutlinedButton(
                                onClick = {
                                    val textToExecute = rememberedSelectedText
                                    rememberedSelectedText = ""
                                    scope.launch {
                                        val success = viewModel.executeCommand(textToExecute)
                                        if (success && tab.autoExpandResult) {
                                            onUpdateTab { it.copy(isResultExpanded = true) }
                                        }
                                    }
                                },
                                enabled = hasSelection && connectionState == ConnectionUiState.CONNECTED,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                            ) {
                                Icon(Icons.Filled.DragHandle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource("execute_selected", language), style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // 执行全部按钮（主操作）
                            Button(
                                onClick = {
                                    KeyboardUtils.hideKeyboard()
                                    scope.launch {
                                        val success = viewModel.executeCommand(textFieldValue.text)
                                        if (success && tab.autoExpandResult) {
                                            onUpdateTab { it.copy(isResultExpanded = true) }
                                        }
                                    }
                                },
                                enabled = textFieldValue.text.isNotBlank() && connectionState == ConnectionUiState.CONNECTED,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.buttonColors(),
                                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                            ) {
                                if (isExecuting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource("execute_all", language), style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // 三点菜单按钮 - 使用 Box 包裹以正确定位菜单
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = null)
                                }
                                // 菜单
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource("sql_templates", language)) },
                                        onClick = {
                                            showMenu = false
                                            showTemplateSelector = true
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Filled.List, contentDescription = null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource("ai_generate", language)) },
                                        onClick = {
                                            showMenu = false
                                            showAiGenerator = true
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Filled.SmartToy, contentDescription = null)
                                        }
                                    )
                                    Divider()
                                    DropdownMenuItem(
                                        text = { Text(stringResource("import_file", language)) },
                                        onClick = {
                                            showMenu = false
                                            scope.launch {
                                                val content = FileUtils.pickFile(emptyList())
                                                if (content != null) {
                                                    textFieldValue = TextFieldValue(content)
                                                    onUpdateTab { tab -> tab.copy(sql = content) }
                                                }
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Filled.FolderOpen, contentDescription = null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource("export_file", language)) },
                                        onClick = {
                                            showMenu = false
                                            scope.launch {
                                                FileUtils.saveFile(
                                                    content = textFieldValue.text,
                                                    defaultName = "query.sql",
                                                    extension = "sql"
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Save, contentDescription = null)
                                        },
                                        enabled = textFieldValue.text.isNotBlank()
                                    )
                                }
                            }
                        }

                        // 模板选择对话框
                        TemplateSelectorDialog(
                            showDialog = showTemplateSelector,
                            onDismissRequest = { showTemplateSelector = false },
                            onSelectTemplate = { template ->
                                // 将模板内容追加到当前 SQL 后面
                                val newSql = if (textFieldValue.text.isNotBlank()) {
                                    textFieldValue.text + "\n\n" + template.sql
                                } else {
                                    template.sql
                                }
                                textFieldValue = TextFieldValue(newSql)
                                onUpdateTab { tab -> tab.copy(sql = newSql) }
                                showTemplateSelector = false
                            },
                            databaseType = currentDatabaseType,
                            language = language
                        )

                        // AI 生成语句对话框
                        AiSqlGeneratorDialog(
                            showDialog = showAiGenerator,
                            onDismissRequest = { showAiGenerator = false },
                            onInsertSql = { sql ->
                                // 将生成的 SQL 追加到当前 SQL 后面
                                val newSql = if (textFieldValue.text.isNotBlank()) {
                                    textFieldValue.text + "\n\n" + sql
                                } else {
                                    sql
                                }
                                textFieldValue = TextFieldValue(newSql)
                                onUpdateTab { tab -> tab.copy(sql = newSql) }
                                showAiGenerator = false
                            },
                            databaseType = currentDatabaseType,
                            tableSchema = currentTableSchema,
                            language = language,
                            aiConfigStorage = aiConfigStorage,
                            onNavigateToAiConfig = onNavigateToAiConfig
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // SQL 输入框 - 带语法高亮
                        CodeEditor(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                textFieldValue = newValue
                                onUpdateTab { tab -> tab.copy(sql = newValue.text) }
                            },
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            placeholder = if (isRedis) stringResource("redis_command_hint", language) else stringResource("sql_placeholder", language),
                            language = EditorLanguage.SQL,
                            enabled = connectionState == ConnectionUiState.CONNECTED,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            showLineNumbers = true
                        )
                    }
                }
            }

            // 错误提示
            AnimatedVisibility(visible = lastErrorMessage != null, enter = fadeIn()) {
                val errorMsg = lastErrorMessage ?: ""
                val errorTemplate = stringResource("error_with_message", language)
                BoxWithConstraints {
                    StyledErrorState(
                        message = errorTemplate.replace("{message}", errorMsg),
                        icon = AppIcons.error,
                        onDismiss = { viewModel.lastErrorMessage = null },
                        onRetry = {
                            scope.launch {
                                if (connectionState != ConnectionUiState.CONNECTED) {
                                    // 连接失败或已断开时，优先重连当前数据库
                                    latestDatabaseConfig?.let { config ->
                                        viewModel.connect(config)
                                    }
                                } else if (textFieldValue.text.isNotBlank()) {
                                    // 已连接时，重试执行当前 SQL
                                    viewModel.executeCommand(textFieldValue.text)
                                }
                            }
                        },
                        language = language,
                        modifier = Modifier.padding(horizontal = if (maxWidth > 360.dp) 16.dp else 12.dp)
                    )
                }
            }
        }

        // 底部结果面板（始终可见）
        BottomResultPanel(
            result = lastQueryResult,
            updateResult = lastUpdateResult,
            isExpanded = tab.isResultExpanded,
            autoExpand = tab.autoExpandResult,
            onToggleExpand = { onUpdateTab { t -> t.copy(isResultExpanded = !t.isResultExpanded) } },
            onAutoExpandChange = { onUpdateTab { t -> t.copy(autoExpandResult = it) } },
            language = language,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun BottomResultPanel(
    result: QueryResult?,
    updateResult: UpdateResult?,
    isExpanded: Boolean,
    autoExpand: Boolean,
    onToggleExpand: () -> Unit,
    onAutoExpandChange: (Boolean) -> Unit,
    language: Language,
    modifier: Modifier = Modifier
) {
    val hasResult = result != null || updateResult != null
    val scope = rememberCoroutineScope()
    var showExportMenu by remember { mutableStateOf(false) }
    val resultToolbarScroll = rememberScrollState()

    BoxWithConstraints(modifier = modifier.zIndex(10f)) {
        // 展开时高度为整个区域减去顶部区域，但最少保留 200.dp 给编辑区
        val expandedHeight = (maxHeight - 60.dp).coerceAtMost(maxHeight - 200.dp)
        // 折叠时高度固定为工具栏高度
        val collapsedHeight = 48.dp

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isExpanded) expandedHeight else collapsedHeight),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 工具栏（始终可见）
                Surface(
                    modifier = Modifier.fillMaxWidth().height(collapsedHeight),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧：标题与摘要可横向滚动，避免窄屏与右侧操作区抢宽导致溢出
                        Row(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .horizontalScroll(resultToolbarScroll),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource("query_result", language),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            // 折叠时且有结果时显示摘要
                            if (!isExpanded && hasResult) {
                                if (result != null) {
                                    Text(
                                        text = stringResource("total_rows", language).replace("{rowCount}", result.rowCount.toString()),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = stringResource("execution_time", language).replace("{ms}", result.executionTimeMs.toString()),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                } else if (updateResult != null) {
                                    Text(
                                        text = stringResource("affected_rows", language).replace("{rows}", updateResult.affectedRows.toString()),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            // 折叠时无结果显示提示
                            if (!isExpanded && !hasResult) {
                                Text(
                                    text = stringResource("query_result_placeholder", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        // 右侧：导出（图标）、Auto、展开/折叠 — 固定占位，不随左侧挤压溢出
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            // 导出（仅图标，菜单内选 CSV/Excel）
                            if (result != null) {
                                Box {
                                    IconButton(
                                        onClick = { showExportMenu = true },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Download,
                                            contentDescription = stringResource("export_file", language),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showExportMenu,
                                        onDismissRequest = { showExportMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource("export_as_csv", language)) },
                                            onClick = {
                                                showExportMenu = false
                                                scope.launch {
                                                    val csvContent = buildCsvString(result)
                                                    FileUtils.saveFile(csvContent, "query_result", "csv")
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.size(18.dp))
                                            }
                                        )
                                        // Excel 导出
                                        DropdownMenuItem(
                                            text = { Text(stringResource("export_as_excel", language)) },
                                            onClick = {
                                                showExportMenu = false
                                                scope.launch {
                                                    val bytes = buildExcelBytes(result)
                                                    if (bytes != null) {
                                                        FileUtils.saveBinaryFile(bytes, "query_result", "xlsx")
                                                    }
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Filled.GridView, contentDescription = null, modifier = Modifier.size(18.dp))
                                            }
                                        )
                                    }
                                }
                            }
                            // Auto：紧凑胶囊，避免与导出文字并排占宽
                            Surface(
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(15.dp),
                                color = if (autoExpand) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                onClick = { onAutoExpandChange(!autoExpand) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Auto",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (autoExpand) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            // 展开/折叠
                            IconButton(
                                onClick = onToggleExpand,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                                    contentDescription = if (isExpanded) stringResource("collapse_result", language) else stringResource("expand_result", language),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // 展开时显示内容
                if (isExpanded) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        if (hasResult) {
                            if (result != null) {
                                QueryResultTable(
                                    result = result,
                                    modifier = Modifier.fillMaxSize(),
                                    language = language,
                                    onExportCsv = null
                                )
                            } else if (updateResult != null) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource("execution_success", language),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource("affected_rows", language).replace("{rows}", updateResult.affectedRows.toString()),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource("execution_time", language).replace("{ms}", updateResult.executionTimeMs.toString()),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            // 展开但无结果时显示空状态
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource("query_result_placeholder", language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatabaseSelectorForTabDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onSelect: (DatabaseConfigInfo) -> Unit,
    databases: List<DatabaseConfigInfo>,
    language: Language
) {
    if (!showDialog) return
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource("select_database", language)) },
        text = {
            if (databases.isEmpty()) {
                Text(stringResource("no_database_configured", language))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(databases) { db ->
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSelect(db) },
                            variant = CardVariant.Default
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(AppSpacing.spaceLg),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DatabaseTypeIcon(
                                    databaseType = db.type,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = db.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${db.host}:${db.port}/${db.database}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource("cancel", language))
            }
        }
    )
}