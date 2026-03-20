package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.theme.AppSpacing
import space.xiaoxiao.databasemanager.theme.ColorTheme
import space.xiaoxiao.databasemanager.theme.ThemeState
import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.AppIcons
import space.xiaoxiao.databasemanager.components.SvgCountryFlag
import space.xiaoxiao.databasemanager.components.toCountryFlag
import space.xiaoxiao.databasemanager.utils.FileUtils

/**
 * 更多页面 - 设置和系统功能入口
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    themeState: ThemeState,
    localizationState: space.xiaoxiao.databasemanager.i18n.LocalizationState,
    onNavigateToAbout: () -> Unit = {},
    onNavigateToDesignSystem: () -> Unit = {},
    onNavigateToAiConfig: () -> Unit = {},
    onNavigateToConfigBackup: () -> Unit = {},
    onColorThemeChanged: (ColorTheme) -> Unit = {},
    onLanguageChanged: (Language) -> Unit = {}
) {
    val language by localizationState.language.collectAsState()
    val colorTheme by themeState.colorTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.spaceLg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceLg)
    ) {
        // 颜色主题设置
        AppCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.spaceLg)
            ) {
                Text(
                    text = stringResource("color_theme_settings", language),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppSpacing.spaceMd))

                ColorThemeSelector(
                    selectedTheme = colorTheme,
                    onThemeSelected = { onColorThemeChanged(it) },
                    language = language
                )
            }
        }

        // 语言设置
        AppCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.spaceLg)
            ) {
                Text(
                    text = stringResource("language_settings", language),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppSpacing.spaceMd))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
                ) {
                    Language.values().forEach { lang ->
                        val isSelected = language == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onLanguageChanged(lang) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                SvgCountryFlag(
                                    flag = lang.toCountryFlag(),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(AppSpacing.spaceSm))
                                Text(
                                    text = when (lang) {
                                        Language.CHINESE -> "中文"
                                        Language.ENGLISH -> "English"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI 配置入口
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToAiConfig
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.spaceLg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.spaceMd))
                Column {
                    Text(
                        text = stringResource("ai_config_menu", language),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == Language.CHINESE) "配置 AI 接口地址和密钥" else "Configure AI API URL and key",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    AppIcons.chevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // UI 规范入口
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToDesignSystem
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.spaceLg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    AppIcons.palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.spaceMd))
                Column {
                    Text(
                        text = stringResource("design_system", language),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == Language.CHINESE) "UI 规范、颜色、字体、组件展示" else "UI specs, colors, typography, components",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    AppIcons.chevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 配置备份入口（跳转到二级页面，低频，放在最下面）
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToConfigBackup
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.spaceLg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.SettingsBackupRestore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.spaceMd))
                Column {
                    Text(
                        text = stringResource("config_backup", language),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource("config_backup_desc", language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    AppIcons.chevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 关于导航
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToAbout
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.spaceLg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    AppIcons.info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.spaceMd))
                Column {
                    Text(
                        text = stringResource("about", language),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == Language.CHINESE) "应用信息、版本、支持的平台和数据库" else "App info, version, supported platforms and databases",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    AppIcons.chevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 颜色主题选择器 - 网格布局
 */
@Composable
private fun ColorThemeSelector(
    selectedTheme: ColorTheme,
    onThemeSelected: (ColorTheme) -> Unit,
    language: Language
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
    ) {
        ColorTheme.entries.forEach { theme ->
            ColorThemeItem(
                theme = theme,
                isSelected = selectedTheme == theme,
                onClick = { onThemeSelected(theme) }
            )
        }
    }
}

/**
 * 单个主题色块
 */
@Composable
private fun ColorThemeItem(
    theme: ColorTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(theme.colorScheme.lightScheme.primary)
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.onSurface,
                        RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = theme.colorScheme.lightScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}