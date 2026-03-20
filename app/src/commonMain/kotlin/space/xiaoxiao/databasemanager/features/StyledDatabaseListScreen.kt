package space.xiaoxiao.databasemanager.features

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.core.DatabaseType

import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.AppDivider
import space.xiaoxiao.databasemanager.components.AppIconButton
import space.xiaoxiao.databasemanager.components.AppIcons
import space.xiaoxiao.databasemanager.components.AppTextButton
import space.xiaoxiao.databasemanager.components.CardVariant
import space.xiaoxiao.databasemanager.components.AppTopBar
import space.xiaoxiao.databasemanager.components.DatabaseTypeIconWithBackground
import space.xiaoxiao.databasemanager.components.ErrorDialog
import space.xiaoxiao.databasemanager.components.StyledEmptyState
import space.xiaoxiao.databasemanager.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledDatabaseListScreen(
    language: Language = Language.CHINESE,
    databases: List<DatabaseConfigInfo>,
    onDatabaseAdd: (DatabaseConfigInfo) -> Unit,
    onDatabaseEdit: (DatabaseConfigInfo) -> Unit,
    onDatabaseDelete: (DatabaseConfigInfo) -> Unit,
    onDatabaseConnect: (DatabaseConfigInfo) -> Unit,
    onNavigateToAddDatabase: () -> Unit,
    onNavigateToEditDatabase: (DatabaseConfigInfo) -> Unit,
    onNavigateToDatabaseManage: (DatabaseConfigInfo) -> Unit
) {
    var showDatabaseConfigScreen by remember { mutableStateOf(false) }
    var configToEdit by remember { mutableStateOf<DatabaseConfigInfo?>(null) }
    var pendingDeleteDatabase by remember { mutableStateOf<DatabaseConfigInfo?>(null) }

    // 删除确认对话框
    pendingDeleteDatabase?.let { database ->
        AlertDialog(
            onDismissRequest = { pendingDeleteDatabase = null },
            icon = {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(stringResource("confirm_delete_database", language))
            },
            text = {
                Column {
                    Text(
                        text = stringResource("delete_database_warning", language),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource("delete_session_warning", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDatabaseDelete(database)
                        pendingDeleteDatabase = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource("delete", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteDatabase = null }) {
                    Text(stringResource("cancel", language))
                }
            }
        )
    }

    // 二级页面：数据库配置
    if (showDatabaseConfigScreen) {
        DatabaseConfigScreen(
            language = language,
            configToEdit = configToEdit,
            onNavigateBack = {
                showDatabaseConfigScreen = false
                configToEdit = null
            },
            onSave = { configInfo ->
                if (configToEdit != null) {
                    onDatabaseEdit(configInfo)
                } else {
                    onDatabaseAdd(configInfo)
                }
                showDatabaseConfigScreen = false
                configToEdit = null
            }
        )
        return // 全屏显示二级页面，不渲染下面的内容
    }

    Scaffold(
        floatingActionButton = {
            BoxWithConstraints {
                if (maxWidth < 360.dp) {
                    FloatingActionButton(
                        onClick = { showDatabaseConfigScreen = true }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource("add", language), modifier = Modifier.size(24.dp))
                    }
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { showDatabaseConfigScreen = true }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource("add", language))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AppTopBar(
                title = "${stringResource("nav_database_list", language)} (${databases.size})",
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = null,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            if (databases.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    StyledEmptyState(
                        icon = AppIcons.databaseEmpty,
                        title = stringResource("empty_database_list", language),
                        message = stringResource("add_database_hint", language)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(AppSpacing.spaceMd), verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
                    items(databases) { database ->
                        AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                            StyledDatabaseConnectionCard(
                                connection = database,
                                onEdit = { configToEdit = database; showDatabaseConfigScreen = true },
                                onDelete = { pendingDeleteDatabase = database },
                                language = language,
                                onNavigateToDatabaseManage = { onNavigateToDatabaseManage(database) }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun StyledDatabaseConnectionCard(
    connection: DatabaseConfigInfo,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToDatabaseManage: () -> Unit,
    language: Language = Language.CHINESE
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Default
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.spaceMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // 左侧：数据库类型图标 - 使用统一背景样式
                    DatabaseTypeIconWithBackground(
                        databaseType = connection.type,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.spaceMd))
                    // 右侧：标题 + 连接信息
                    Column {
                        Text(
                            text = connection.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.spaceXxs))
                        // 连接信息：{用户}@{地址}/{数据库}
                        val address = "${connection.host}:${connection.port}"
                        val configText = when {
                            connection.username.isNotBlank() && connection.database.isNotBlank() -> "${connection.username}@$address/${connection.database}"
                            connection.username.isNotBlank() -> "${connection.username}@$address"
                            connection.database.isNotBlank() -> "$address/${connection.database}"
                            else -> address
                        }
                        Text(
                            text = configText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AppSpacing.spaceMd))
            AppDivider()
            Spacer(modifier = Modifier.height(AppSpacing.spaceSm))
            // 三个按钮放一行：删除、编辑、更多操作
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppTextButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource("delete", language),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                AppTextButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource("edit", language),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                AppIconButton(
                    onClick = onNavigateToDatabaseManage,
                    icon = Icons.Filled.MoreHoriz,
                    description = stringResource("more", language),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ConnectionInfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, language: Language = Language.CHINESE) {
    Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp), tonalElevation = 2.dp) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}