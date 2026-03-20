package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import space.xiaoxiao.databasemanager.components.AppBottomSheet
import space.xiaoxiao.databasemanager.components.AppButton
import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.AppEmptyState
import space.xiaoxiao.databasemanager.components.AppIconButton
import space.xiaoxiao.databasemanager.components.AppTextButton
import space.xiaoxiao.databasemanager.components.AppTextField
import space.xiaoxiao.databasemanager.components.AppTopBar
import space.xiaoxiao.databasemanager.components.CardVariant
import space.xiaoxiao.databasemanager.core.DatabaseConfig
import space.xiaoxiao.databasemanager.core.DatabaseType
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseManageScreen(
    language: Language = Language.CHINESE,
    databaseConfig: DatabaseConfigInfo,
    onNavigateBack: () -> Unit
) {
    val viewModel = remember { DatabaseViewModel() }
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }
    var confirmDeleteDatabase by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCreateSuccess by remember { mutableStateOf(false) }
    var showDeleteSuccess by remember { mutableStateOf(false) }

    val manageDatabasesTitle = stringResource("manage_databases", language)
    val createDatabaseTitle = stringResource("create_database", language)
    val databaseNameEmptyError = stringResource("database_name_empty", language)
    val databaseNameInvalidError = stringResource("database_name_invalid", language)
    val databaseNameExistsError = stringResource("database_name_exists", language)
    val databaseNameHint = stringResource("database_name_hint", language)
    val cancelText = stringResource("cancel", language)
    val currentDatabaseLabel = stringResource("current_database", language)
    val serverDatabasesLabel = stringResource("server_databases", language)
    val noDatabaseAvailableTitle = stringResource("no_database_available", language)
    val notSelectedTitle = stringResource("not_selected", language)
    val switchDatabaseTitle = stringResource("switch_database", language)
    val deleteTitle = stringResource("delete", language)
    val dropDatabaseTitle = stringResource("drop_database", language)
    val confirmDropDatabaseTemplate = stringResource("confirm_drop_database", language)
    val databaseCreatedTitle = stringResource("database_created", language)
    val databaseDroppedTitle = stringResource("database_dropped", language)
    val errorTitle = stringResource("error", language)
    val successTitle = stringResource("success", language)
    val refreshFailedText = stringResource("refresh_failed", language)

    val serverDatabases by derivedStateOf { viewModel.serverDatabases }
    val currentDatabaseName by derivedStateOf { viewModel.currentDatabaseName }
    val connectionState by derivedStateOf { viewModel.connectionState }

    // Redis 不支持动态数据库管理
    val isRedis = databaseConfig.type == DatabaseType.REDIS

    LaunchedEffect(databaseConfig) {
        viewModel.connect(databaseConfig)
        scope.launch {
            viewModel.loadServerDatabases()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = manageDatabasesTitle,
                onNavigationClick = onNavigateBack,
                actions = {
                    BoxWithConstraints {
                        if (maxWidth > 300.dp) {
                            AppIconButton(
                                onClick = {
                                    scope.launch {
                                        val result = viewModel.loadServerDatabases()
                                        if (!result) {
                                            errorMessage = viewModel.lastErrorMessage ?: refreshFailedText
                                        }
                                    }
                                },
                                icon = Icons.Filled.Refresh
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(AppSpacing.spaceLg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceLg)
        ) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                variant = CardVariant.Default
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.spaceLg),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
                ) {
                    Text(
                        text = currentDatabaseLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentDatabaseName ?: notSelectedTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceXs)
                ) {
                    Text(
                        text = serverDatabasesLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = switchDatabaseTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!isRedis) {
                    AppTextButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(createDatabaseTitle)
                    }
                }
            }

            // Redis 提示信息
            if (isRedis) {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = CardVariant.Default
                ) {
                    Row(
                        modifier = Modifier.padding(AppSpacing.spaceLg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.spaceSm))
                        Text(
                            text = stringResource("redis_no_database_management", language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            if (serverDatabases.isEmpty()) {
                if (connectionState == ConnectionUiState.CONNECTING) {
                    AppBottomSheet(
                        onDismissRequest = { }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.spaceLg),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = stringResource("loading", language),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    AppEmptyState(
                        icon = Icons.Filled.Storage,
                        title = noDatabaseAvailableTitle,
                        message = databaseNameHint,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
                ) {
                    items(serverDatabases) { dbName ->
                        val isCurrent = dbName == currentDatabaseName
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            variant = if (isCurrent) CardVariant.Elevated else CardVariant.Default
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(AppSpacing.spaceLg),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dbName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isCurrent) {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(AppSpacing.spaceXs))
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = currentDatabaseLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier
                                                    .padding(
                                                        horizontal = AppSpacing.spaceSm,
                                                        vertical = AppSpacing.spaceXs
                                                    )
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (!isCurrent) {
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    val result = viewModel.switchServerDatabase(dbName)
                                                    if (!result) {
                                                        errorMessage = viewModel.lastErrorMessage ?: "切换数据库失败"
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = switchDatabaseTitle,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { confirmDeleteDatabase = dbName },
                                        enabled = !isCurrent && !isRedis
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = deleteTitle,
                                            tint = if (isRedis) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            icon = {
                Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text(errorTitle) },
            text = { Text(msg) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text(stringResource("ok", language))
                }
            }
        )
    }

    if (showCreateSuccess) {
        AlertDialog(
            onDismissRequest = { showCreateSuccess = false },
            icon = {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = { Text(successTitle) },
            text = { Text(databaseCreatedTitle) },
            confirmButton = {
                Button(onClick = { showCreateSuccess = false }) {
                    Text(stringResource("ok", language))
                }
            }
        )
    }

    if (showDeleteSuccess) {
        AlertDialog(
            onDismissRequest = { showDeleteSuccess = false },
            icon = {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = { Text(successTitle) },
            text = { Text(databaseDroppedTitle) },
            confirmButton = {
                Button(onClick = { showDeleteSuccess = false }) {
                    Text(stringResource("ok", language))
                }
            }
        )
    }

    confirmDeleteDatabase?.let { dbName ->
        AlertDialog(
            onDismissRequest = { confirmDeleteDatabase = null },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            title = { Text(dropDatabaseTitle) },
            text = { Text(confirmDropDatabaseTemplate.replace("{database}", dbName)) },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val result = viewModel.dropDatabase(dbName)
                        if (result) {
                            showDeleteSuccess = true
                        } else {
                            errorMessage = viewModel.lastErrorMessage ?: "删除失败"
                        }
                    }
                    confirmDeleteDatabase = null
                }) {
                    Text(stringResource("delete", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteDatabase = null }) {
                    Text(cancelText)
                }
            }
        )
    }

    if (showCreateDialog) {
        CreateDatabaseDialog(
            createDatabaseTitle = createDatabaseTitle,
            databaseNameEmptyError = databaseNameEmptyError,
            databaseNameInvalidError = databaseNameInvalidError,
            databaseNameExistsError = databaseNameExistsError,
            databaseNameHint = databaseNameHint,
            cancelText = cancelText,
            existingDatabases = serverDatabases,
            databaseType = databaseConfig.type,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, charset ->
                scope.launch {
                    val result = viewModel.createDatabase(name, charset)
                    if (result) {
                        showCreateDialog = false
                        showCreateSuccess = true
                    } else {
                        errorMessage = viewModel.lastErrorMessage ?: "创建失败"
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateDatabaseDialog(
    createDatabaseTitle: String,
    databaseNameEmptyError: String,
    databaseNameInvalidError: String,
    databaseNameExistsError: String,
    databaseNameHint: String,
    cancelText: String,
    existingDatabases: List<String>,
    databaseType: DatabaseType,
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit
) {
    var databaseName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var charset by remember { mutableStateOf<String?>(null) }
    var charsetExpanded by remember { mutableStateOf(false) }

    // 根据数据库类型提供不同的字符集选项
    val charsetOptions = when (databaseType) {
        DatabaseType.MYSQL -> listOf(
            "utf8mb4" to stringResource("charset_utf8mb4", Language.CHINESE),
            "utf8" to stringResource("charset_utf8", Language.CHINESE),
            "latin1" to stringResource("charset_latin1", Language.CHINESE),
            "gbk" to stringResource("charset_gbk", Language.CHINESE)
        )
        DatabaseType.POSTGRESQL -> listOf(
            "UTF8" to stringResource("charset_utf8_pg", Language.CHINESE),
            "LATIN1" to stringResource("charset_latin1_pg", Language.CHINESE),
            "GB18030" to stringResource("charset_gbk_pg", Language.CHINESE)
        )
        DatabaseType.REDIS -> emptyList()
    }

    val showCharset = databaseType == DatabaseType.MYSQL || databaseType == DatabaseType.POSTGRESQL
    val selectCharsetLabel = stringResource("select_charset", Language.CHINESE)
    val charsetLabel = stringResource("charset", Language.CHINESE)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(createDatabaseTitle) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
            ) {
                AppTextField(
                    value = databaseName,
                    onValueChange = {
                        databaseName = it
                        error = null
                    },
                    label = databaseNameHint,
                    placeholder = databaseNameHint,
                    singleLine = true,
                    isError = error != null
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // 字符集选择器（仅 MySQL 和 PostgreSQL）
                if (showCharset) {
                    ExposedDropdownMenuBox(
                        expanded = charsetExpanded,
                        onExpandedChange = { charsetExpanded = !charsetExpanded }
                    ) {
                        AppTextField(
                            value = charset ?: "",
                            onValueChange = {},
                            label = charsetLabel,
                            placeholder = selectCharsetLabel,
                            singleLine = true,
                            enabled = false,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = charsetExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = charsetExpanded,
                            onDismissRequest = { charsetExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(selectCharsetLabel) },
                                onClick = {
                                    charset = null
                                    charsetExpanded = false
                                }
                            )
                            charsetOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        charset = value
                                        charsetExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            AppButton(onClick = {
                if (databaseName.isBlank()) {
                    error = databaseNameEmptyError
                    return@AppButton
                }
                if (!databaseName.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                    error = databaseNameInvalidError
                    return@AppButton
                }
                if (databaseName in existingDatabases) {
                    error = databaseNameExistsError
                    return@AppButton
                }
                onCreate(databaseName, charset)
            }) {
                Text(createDatabaseTitle)
            }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) {
                Text(cancelText)
            }
        }
    )
}
