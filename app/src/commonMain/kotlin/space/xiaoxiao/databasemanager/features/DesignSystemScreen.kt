package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.components.*
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.theme.AppSpacing

/**
 * UI 规范展示页面
 * 展示整个应用的设计系统：颜色、间距、字体、组件等
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemScreen(
    language: Language = Language.CHINESE,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource("design_system", language),
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.spaceLg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceXl)
        ) {
            // 颜色系统
            ColorSection(language)

            // 间距系统
            SpacingSection(language)

            // 字体系统
            TypographySection(language)

            // 圆角系统
            ShapeSection(language)

            // 按钮组件
            ButtonSection(language)

            // 输入组件
            TextFieldSection(language)

            // 卡片组件
            CardSection(language)

            // 标签组件
            BadgeSection(language)

            // 状态组件
            StateSection(language)

            // 分隔线
            DividerSection(language)

            // 图标集合
            IconsSection(language)
        }
    }
}

// ==================== 颜色系统 ====================

@Composable
private fun ColorSection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "颜色系统" else "Color System") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
            // 主题色
            Text(
                text = if (language == Language.CHINESE) "主题色" else "Theme Colors",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                ColorSwatch("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                ColorSwatch("Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
                ColorSwatch("Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
            }

            // 主题色容器
            Text(
                text = if (language == Language.CHINESE) "主题色容器" else "Theme Containers",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                ColorSwatch("Primary\nContainer", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                ColorSwatch("Secondary\nContainer", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                ColorSwatch("Tertiary\nContainer", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
            }

            // 语义色
            Text(
                text = if (language == Language.CHINESE) "语义色" else "Semantic Colors",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                ColorSwatch("Error", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
                ColorSwatch("Error\nContainer", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
            }

            // 背景色层级
            Text(
                text = if (language == Language.CHINESE) "背景色层级" else "Background Levels",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                ColorSwatch("Background", MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.onBackground)
                ColorSwatch("Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
                ColorSwatch("Surface\nVariant", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                ColorSwatch("Surface\nContainer", MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    name: String,
    color: Color,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(AppSpacing.spaceSm))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ==================== 间距系统 ====================

@Composable
private fun SpacingSection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "间距系统" else "Spacing System") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)) {
            SpacingRow("spaceNone", AppSpacing.spaceNone, if (language == Language.CHINESE) "无间距" else "None")
            SpacingRow("spaceXxs", AppSpacing.spaceXxs, if (language == Language.CHINESE) "极小间距" else "Extra Extra Small")
            SpacingRow("spaceXs", AppSpacing.spaceXs, if (language == Language.CHINESE) "元素内部间距" else "Extra Small")
            SpacingRow("spaceSm", AppSpacing.spaceSm, if (language == Language.CHINESE) "图标与文字间距" else "Small")
            SpacingRow("spaceMd", AppSpacing.spaceMd, if (language == Language.CHINESE) "列表项间距" else "Medium")
            SpacingRow("spaceLg", AppSpacing.spaceLg, if (language == Language.CHINESE) "页面内边距" else "Large")
            SpacingRow("spaceXl", AppSpacing.spaceXl, if (language == Language.CHINESE) "区块间距" else "Extra Large")
            SpacingRow("spaceXxl", AppSpacing.spaceXxl, if (language == Language.CHINESE) "大区块间距" else "Extra Extra Large")
        }
    }
}

@Composable
private fun SpacingRow(
    name: String,
    value: androidx.compose.ui.unit.Dp,
    usage: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 间距可视化
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .width(value)
                    .height(16.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(modifier = Modifier.width(AppSpacing.spaceMd))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = "${value.value}dp",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(50.dp)
        )
        Text(
            text = usage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ==================== 字体系统 ====================

@Composable
private fun TypographySection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "字体系统" else "Typography System") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
            TypographyRow("displayLarge", MaterialTheme.typography.displayLarge, if (language == Language.CHINESE) "大标题" else "Large Title")
            TypographyRow("displayMedium", MaterialTheme.typography.displayMedium, if (language == Language.CHINESE) "页面主标题" else "Page Title")
            TypographyRow("displaySmall", MaterialTheme.typography.displaySmall, if (language == Language.CHINESE) "区块标题" else "Section Title")
            TypographyRow("headlineLarge", MaterialTheme.typography.headlineLarge, if (language == Language.CHINESE) "卡片标题" else "Card Title")
            TypographyRow("headlineMedium", MaterialTheme.typography.headlineMedium, if (language == Language.CHINESE) "列表项标题" else "List Item Title")
            TypographyRow("headlineSmall", MaterialTheme.typography.headlineSmall, if (language == Language.CHINESE) "小标题" else "Small Title")
            TypographyRow("titleLarge", MaterialTheme.typography.titleLarge, if (language == Language.CHINESE) "TopAppBar 标题" else "AppBar Title")
            TypographyRow("titleMedium", MaterialTheme.typography.titleMedium, if (language == Language.CHINESE) "卡片内标题" else "Card Inner Title")
            TypographyRow("titleSmall", MaterialTheme.typography.titleSmall, if (language == Language.CHINESE) "小卡片标题" else "Small Card Title")
            TypographyRow("bodyLarge", MaterialTheme.typography.bodyLarge, if (language == Language.CHINESE) "正文内容" else "Body Text")
            TypographyRow("bodyMedium", MaterialTheme.typography.bodyMedium, if (language == Language.CHINESE) "次要内容、列表项" else "Secondary Text")
            TypographyRow("bodySmall", MaterialTheme.typography.bodySmall, if (language == Language.CHINESE) "辅助说明" else "Helper Text")
            TypographyRow("labelLarge", MaterialTheme.typography.labelLarge, if (language == Language.CHINESE) "按钮文字" else "Button Text")
            TypographyRow("labelMedium", MaterialTheme.typography.labelMedium, if (language == Language.CHINESE) "标签文字" else "Label Text")
            TypographyRow("labelSmall", MaterialTheme.typography.labelSmall, if (language == Language.CHINESE) "极小标签" else "Tiny Label")
        }
    }
}

@Composable
private fun TypographyRow(
    name: String,
    style: androidx.compose.ui.text.TextStyle,
    usage: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (name == "displayLarge") "Display Large" else name.replace("display", "D").replace("headline", "H").replace("title", "T").replace("body", "B").replace("label", "L"),
                style = style,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${style.fontSize.value.toInt()}sp",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = usage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== 圆角系统 ====================

@Composable
private fun ShapeSection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "圆角系统" else "Shape System") {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
        ) {
            ShapeSwatch("small\n8dp", MaterialTheme.shapes.small)
            ShapeSwatch("medium\n12dp", MaterialTheme.shapes.medium)
            ShapeSwatch("large\n16dp", MaterialTheme.shapes.large)
            ShapeSwatch("extraLarge\n24dp", MaterialTheme.shapes.extraLarge)
        }
    }
}

@Composable
private fun ShapeSwatch(
    name: String,
    shape: androidx.compose.ui.graphics.Shape
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = shape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {}
        Spacer(modifier = Modifier.height(AppSpacing.spaceXs))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ==================== 按钮组件 ====================

@Composable
private fun ButtonSection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "按钮组件" else "Button Components") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
            // Filled Buttons
            Text(
                text = "Filled Buttons",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                AppButton(onClick = {}, variant = ButtonVariant.Primary) { Text("Primary") }
                AppButton(onClick = {}, variant = ButtonVariant.Secondary) { Text("Secondary") }
                AppButton(onClick = {}, variant = ButtonVariant.Tertiary) { Text("Tertiary") }
                AppButton(onClick = {}, variant = ButtonVariant.Error) { Text("Error") }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                AppButton(onClick = {}, variant = ButtonVariant.Primary, enabled = false) { Text("Disabled") }
                AppButton(onClick = {}, variant = ButtonVariant.Primary) {
                    SmallLoadingIndicator()
                    Spacer(modifier = Modifier.width(AppSpacing.spaceXs))
                    Text(if (language == Language.CHINESE) "加载中" else "Loading")
                }
            }

            // Outlined Buttons
            Text(
                text = "Outlined Buttons",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                AppOutlinedButton(onClick = {}, variant = ButtonVariant.Primary) { Text("Primary") }
                AppOutlinedButton(onClick = {}, variant = ButtonVariant.Secondary) { Text("Secondary") }
                AppOutlinedButton(onClick = {}, variant = ButtonVariant.Error) { Text("Error") }
                AppOutlinedButton(onClick = {}, variant = ButtonVariant.Primary, enabled = false) { Text("Disabled") }
            }

            // Text Buttons
            Text(
                text = "Text Buttons",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                AppTextButton(onClick = {}, variant = ButtonVariant.Primary) { Text("Primary") }
                AppTextButton(onClick = {}, variant = ButtonVariant.Secondary) { Text("Secondary") }
                AppTextButton(onClick = {}, variant = ButtonVariant.Error) { Text("Error") }
            }

            // Icon Button
            Text(
                text = "Icon Button",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                AppIconButton(onClick = {}, icon = Icons.Filled.Add)
                AppIconButton(onClick = {}, icon = Icons.Filled.Edit)
                AppIconButton(onClick = {}, icon = Icons.Filled.Delete, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ==================== 输入组件 ====================

@Composable
private fun TextFieldSection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "输入组件" else "Text Field Components") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
            var text1 by remember { mutableStateOf("") }
            var text2 by remember { mutableStateOf("") }
            var text3 by remember { mutableStateOf("") }
            var text4 by remember { mutableStateOf("") }
            var text5 by remember { mutableStateOf("") }

            AppTextField(
                value = text1,
                onValueChange = { text1 = it },
                label = if (language == Language.CHINESE) "默认状态" else "Default State",
                placeholder = if (language == Language.CHINESE) "请输入内容..." else "Enter text..."
            )

            AppTextField(
                value = text2,
                onValueChange = { text2 = it },
                label = if (language == Language.CHINESE) "带图标" else "With Icon",
                leadingIcon = {
                    Icon(Icons.Filled.Person, contentDescription = null)
                }
            )

            AppTextField(
                value = text3,
                onValueChange = { text3 = it },
                label = if (language == Language.CHINESE) "错误状态" else "Error State",
                isError = true,
                trailingIcon = {
                    Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            )

            AppTextField(
                value = text4,
                onValueChange = { text4 = it },
                label = if (language == Language.CHINESE) "禁用状态" else "Disabled",
                enabled = false
            )

            AppTextField(
                value = text5,
                onValueChange = { text5 = it },
                label = if (language == Language.CHINESE) "多行输入" else "Multiline",
                minLines = 3
            )
        }
    }
}

// ==================== 卡片组件 ====================

@Composable
private fun CardSection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "卡片组件 (无阴影)" else "Card Components (No Shadow)") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
            // 默认卡片
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.spaceMd)) {
                    Text(
                        text = if (language == Language.CHINESE) "默认卡片" else "Default Card",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (language == Language.CHINESE) "无阴影，使用背景色区分层级" else "No shadow, using background colors for hierarchy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 可点击卡片
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.spaceMd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.TouchApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.spaceSm))
                    Text(
                        text = if (language == Language.CHINESE) "可点击卡片" else "Clickable Card",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 嵌套层级
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.spaceMd)) {
                    Text(
                        text = if (language == Language.CHINESE) "嵌套层级展示" else "Nested Hierarchy",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.spaceSm))
                    // 内层卡片使用 surfaceVariant 背景
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppSpacing.spaceSm))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(AppSpacing.spaceMd)
                    ) {
                        Text(
                            text = if (language == Language.CHINESE) "内层容器 (surfaceVariant)" else "Inner container (surfaceVariant)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ==================== 标签组件 ====================

@Composable
private fun BadgeSection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "标签组件" else "Badge Components") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
            // DatabaseTypeBadge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                DatabaseTypeBadge("MySQL")
                Spacer(modifier = Modifier.width(AppSpacing.spaceSm))
                DatabaseTypeBadge(
                    "PostgreSQL",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // ConnectionInfoChip
            ConnectionInfoChip(
                icon = Icons.Filled.Dns,
                text = "localhost:3306",
                language = language
            )

            // 自定义 Badge
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text("Label", style = MaterialTheme.typography.labelSmall)
                }
                Badge(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text("Error", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                }
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text("PK", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// ==================== 状态组件 ====================

@Composable
private fun StateSection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "状态组件" else "State Components") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
            // 空状态（带操作）
            AppEmptyState(
                icon = Icons.Filled.Inbox,
                title = if (language == Language.CHINESE) "暂无数据" else "No Data",
                message = if (language == Language.CHINESE) "点击下方按钮添加" else "Click button below to add",
                actionLabel = if (language == Language.CHINESE) "添加" else "Add",
                onAction = {}
            )

            // 加载指示器
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
            ) {
                AppLoadingIndicator()
                SmallLoadingIndicator()
            }

            // 测试结果卡片
            TestResultCard(
                result = if (language == Language.CHINESE) "连接成功" else "Connection successful",
                isSuccess = true,
                onDismiss = {}
            )
            TestResultCard(
                result = if (language == Language.CHINESE) "连接失败: 超时" else "Connection failed: Timeout",
                isSuccess = false,
                onDismiss = {}
            )
        }
    }
}

// ==================== 分隔线 ====================

@Composable
private fun DividerSection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "分隔线" else "Divider") {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)) {
            Text(
                text = if (language == Language.CHINESE) "默认分隔线" else "Default Divider",
                style = MaterialTheme.typography.bodyMedium
            )
            AppDivider()
            Text(
                text = if (language == Language.CHINESE) "自定义颜色分隔线" else "Custom Color Divider",
                style = MaterialTheme.typography.bodyMedium
            )
            AppDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        }
    }
}

// ==================== 图标集合 ====================

@Composable
private fun IconsSection(language: Language) {
    DesignSection(title = if (language == Language.CHINESE) "图标集合" else "Icons Collection") {
        val icons = listOf(
            Pair(AppIcons.database, "database"),
            Pair(AppIcons.storage, "storage"),
            Pair(AppIcons.person, "person"),
            Pair(AppIcons.edit, "edit"),
            Pair(AppIcons.delete, "delete"),
            Pair(AppIcons.add, "add"),
            Pair(AppIcons.close, "close"),
            Pair(AppIcons.check, "check"),
            Pair(AppIcons.checkCircle, "checkCircle"),
            Pair(AppIcons.playArrow, "playArrow"),
            Pair(AppIcons.arrowBack, "arrowBack"),
            Pair(AppIcons.chevronRight, "chevronRight"),
            Pair(AppIcons.info, "info"),
            Pair(AppIcons.language, "language"),
            Pair(AppIcons.lightMode, "lightMode"),
            Pair(AppIcons.darkMode, "darkMode"),
            Pair(AppIcons.search, "search"),
            Pair(AppIcons.refresh, "refresh"),
            Pair(AppIcons.settings, "settings"),
            Pair(AppIcons.history, "history"),
            Pair(AppIcons.table, "table"),
            Pair(AppIcons.android, "android"),
            Pair(AppIcons.computer, "computer"),
            Pair(AppIcons.palette, "palette")
        )

        val rows = icons.chunked(4)
        rows.forEach { rowIcons ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowIcons.forEach { (icon, name) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(AppSpacing.spaceXs)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = name,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ==================== 通用组件 ====================

/**
 * 设计规范区块标题
 */
@Composable
private fun DesignSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        AppDivider()
        content()
    }
}