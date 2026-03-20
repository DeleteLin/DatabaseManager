package space.xiaoxiao.databasemanager.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.core.DatabaseType
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource

/**
 * 数据库类型对应的默认图标颜色
 */
fun DatabaseType.getDefaultColor(): Color = when (this) {
    DatabaseType.MYSQL -> Color(0xFF00758F)      // MySQL 青色
    DatabaseType.POSTGRESQL -> Color(0xFF336791) // PostgreSQL 蓝色
    DatabaseType.REDIS -> Color(0xFFDC382D)      // Redis 红色
}

/**
 * 数据库类型对应的默认系统图标（回退用）
 */
fun DatabaseType.getFallbackIcon(): ImageVector = when (this) {
    DatabaseType.MYSQL -> Icons.Filled.Storage
    DatabaseType.POSTGRESQL -> Icons.Filled.Storage
    DatabaseType.REDIS -> Icons.Filled.Storage
}

/**
 * 数据库类型图标组件
 *
 * 使用各数据库类型对应的官方 SVG 图标
 * 通过 expect/actual 实现跨平台支持：
 * - Android: 使用 Coil 库加载 SVG
 * - JVM/Desktop: 使用 painterResource 加载 SVG
 *
 * @param databaseType 数据库类型
 * @param modifier 修饰符
 * @param contentDescription 内容描述
 * @param tint 颜色过滤（SVG 图标使用原色，此参数保留以兼容旧接口）
 */
@Composable
fun DatabaseTypeIcon(
    databaseType: DatabaseType,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color? = null
) {
    // 使用跨平台 SVG 加载组件
    SvgDatabaseIcon(
        databaseType = databaseType,
        modifier = modifier,
        contentDescription = contentDescription
    )
}

/**
 * 带背景的数据库类型图标
 */
@Composable
fun DatabaseTypeIconWithBackground(
    databaseType: DatabaseType,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentDescription: String? = null
) {
    val iconColor = databaseType.getDefaultColor()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        DatabaseTypeIcon(
            databaseType = databaseType,
            modifier = Modifier.size(24.dp),
            contentDescription = contentDescription,
            tint = iconColor
        )
    }
}

/**
 * 图标集合
 */
object AppIcons {
    val databaseEmpty = Icons.Filled.Dns
    val queryEmpty = Icons.Filled.FindInPage
    val error = Icons.Filled.Error
    val connection = Icons.Filled.Dns
    val database = Icons.Filled.Dns
    val storage = Icons.Filled.Storage
    val person = Icons.Filled.Person
    val edit = Icons.Filled.Edit
    val delete = Icons.Filled.Delete
    val add = Icons.Filled.Add
    val close = Icons.Filled.Close
    val check = Icons.Filled.Check
    val checkCircle = Icons.Filled.CheckCircle
    val playArrow = Icons.Filled.PlayArrow
    val arrowBack = Icons.Filled.ArrowBack
    val chevronRight = Icons.Filled.ChevronRight
    val info = Icons.Filled.Info
    val language = Icons.Filled.Language
    val lightMode = Icons.Filled.LightMode
    val darkMode = Icons.Filled.DarkMode
    val search = Icons.Filled.Search
    val refresh = Icons.Filled.Refresh
    val settings = Icons.Filled.Settings
    val history = Icons.Filled.History
    val table = Icons.Filled.GridView
    val android = Icons.Filled.Android
    val computer = Icons.Filled.Computer
    val palette = Icons.Filled.Palette
}

/**
 * 空状态组件 - 使用新设计系统
 */
@Composable
fun StyledEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    language: Language = Language.CHINESE
) {
    AppEmptyState(
        icon = icon,
        title = title,
        message = message,
        modifier = modifier,
        actionLabel = actionLabel,
        onAction = onAction
    )
}

/**
 * 错误状态组件 - 使用新设计系统
 */
@Composable
fun StyledErrorState(
    message: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    language: Language = Language.CHINESE,
    modifier: Modifier = Modifier
) {
    AppErrorState(
        message = message,
        onDismiss = onDismiss,
        onRetry = onRetry,
        modifier = modifier,
        language = language
    )
}

/**
 * 错误提示对话框 - 用于二级页面显示错误
 */
@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    language: Language = Language.CHINESE,
    title: String? = null,
    confirmText: String? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(imageVector = Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        },
        title = { Text(title ?: stringResource("error", language)) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(confirmText ?: stringResource("ok", language))
            }
        }
    )
}

/**
 * 连接信息 Chip - 统一样式
 */
@Composable
fun ConnectionInfoChip(
    icon: ImageVector,
    text: String,
    language: Language = Language.CHINESE
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * 使用数据库类型图标的连接信息 Chip
 */
@Composable
fun ConnectionInfoChipWithDatabaseType(
    databaseType: DatabaseType,
    text: String,
    language: Language = Language.CHINESE
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DatabaseTypeIcon(
                databaseType = databaseType,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * 数据库类型 Badge
 */
@Composable
fun DatabaseTypeBadge(
    typeText: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Badge(
        containerColor = containerColor
    ) {
        Text(
            text = typeText,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

/**
 * 测试结果显示卡片
 */
@Composable
fun TestResultCard(
    result: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                contentDescription = null,
                tint = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = result,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 加载状态指示器
 */
@Composable
fun AppLoadingIndicator(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        strokeWidth = 2.dp,
        color = color
    )
}

/**
 * 小型加载状态 (用于按钮内)
 */
@Composable
fun SmallLoadingIndicator(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimary
) {
    CircularProgressIndicator(
        modifier = modifier.size(14.dp),
        strokeWidth = 2.dp,
        color = color
    )
}