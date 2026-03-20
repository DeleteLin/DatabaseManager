package space.xiaoxiao.databasemanager.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource

/**
 * 卡片变体
 */
enum class CardVariant {
    Default,    // 默认：使用 surfaceContainerHigh
    Elevated,   // 更高层级：使用 surfaceContainerHighest
    Surface     // 纯净表面：使用 surface
}

/**
 * 统一卡片样式 - 手机触摸优化（无阴影设计）
 * 使用背景色层级区分，不使用阴影
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: CardVariant = CardVariant.Default,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = when (variant) {
        CardVariant.Default -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
        CardVariant.Elevated -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
        CardVariant.Surface -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (onClick != null) {
        Card(
            modifier = modifier,
            onClick = onClick,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = shape
        ) {
            content()
        }
    } else {
        Card(
            modifier = modifier,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = shape
        ) {
            content()
        }
    }
}

/**
 * 统一顶部导航栏 - 固定高度 56dp (手机优化)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = Icons.Filled.ArrowBack,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (navigationIcon != null && onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(navigationIcon, contentDescription = null)
                }
            }
        },
        actions = actions,
        colors = colors,
        modifier = modifier
    )
}

/**
 * 统一按钮样式 - 最小高度 48dp (手机触摸友好)
 */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    content: @Composable RowScope.() -> Unit
) {
    val containerColor = when (variant) {
        ButtonVariant.Primary -> MaterialTheme.colorScheme.primary
        ButtonVariant.Secondary -> MaterialTheme.colorScheme.secondary
        ButtonVariant.Tertiary -> MaterialTheme.colorScheme.tertiary
        ButtonVariant.Error -> MaterialTheme.colorScheme.error
    }

    val onContentColor = when (variant) {
        ButtonVariant.Primary -> MaterialTheme.colorScheme.onPrimary
        ButtonVariant.Secondary -> MaterialTheme.colorScheme.onSecondary
        ButtonVariant.Tertiary -> MaterialTheme.colorScheme.onTertiary
        ButtonVariant.Error -> MaterialTheme.colorScheme.onError
    }

    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = onContentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.38f),
            disabledContentColor = onContentColor.copy(alpha = 0.38f)
        )
    ) {
        content()
    }
}

/**
 * 按钮变体
 */
enum class ButtonVariant {
    Primary,
    Secondary,
    Tertiary,
    Error
}

/**
 * 统一文本按钮样式
 */
@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    content: @Composable RowScope.() -> Unit
) {
    val contentColor = when (variant) {
        ButtonVariant.Primary -> MaterialTheme.colorScheme.primary
        ButtonVariant.Secondary -> MaterialTheme.colorScheme.secondary
        ButtonVariant.Tertiary -> MaterialTheme.colorScheme.tertiary
        ButtonVariant.Error -> MaterialTheme.colorScheme.error
    }

    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = contentColor
        )
    ) {
        content()
    }
}

/**
 * 统一 outlined 按钮样式
 */
@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    content: @Composable RowScope.() -> Unit
) {
    val contentColor = when (variant) {
        ButtonVariant.Primary -> MaterialTheme.colorScheme.primary
        ButtonVariant.Secondary -> MaterialTheme.colorScheme.secondary
        ButtonVariant.Tertiary -> MaterialTheme.colorScheme.tertiary
        ButtonVariant.Error -> MaterialTheme.colorScheme.error
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor
        )
    ) {
        content()
    }
}

/**
 * 统一输入框样式 - 适合手机输入
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

/**
 * 统一底部弹层 (ModalBottomSheet)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        content = content
    )
}

/**
 * 统一图标按钮 - 最小触摸区域 48dp
 */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    description: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = Color.Unspecified
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 统一分隔线
 */
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    thickness: androidx.compose.ui.unit.Dp = 1.dp
) {
    HorizontalDivider(
        modifier = modifier,
        color = color,
        thickness = thickness
    )
}

/**
 * 统一空状态组件
 */
@Composable
fun AppEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            AppButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * 统一错误状态组件
 */
@Composable
fun AppErrorState(
    message: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    language: Language = Language.CHINESE
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            AppTextButton(onClick = onDismiss) {
                Text(stringResource("close", language))
            }
            AppTextButton(onClick = onRetry) {
                Text(stringResource("retry", language))
            }
        }
    }
}