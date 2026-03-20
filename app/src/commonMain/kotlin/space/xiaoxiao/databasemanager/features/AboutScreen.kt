package space.xiaoxiao.databasemanager.features

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import space.xiaoxiao.databasemanager.core.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.AppWebView
import space.xiaoxiao.databasemanager.components.AppIcons
import space.xiaoxiao.databasemanager.components.AppTopBar
import space.xiaoxiao.databasemanager.components.CardVariant
import space.xiaoxiao.databasemanager.components.DatabaseLogo
import space.xiaoxiao.databasemanager.components.SvgLogo

/**
 * 关于页面 - 使用新设计系统重构
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    language: Language = Language.CHINESE,
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource("about", language),
                navigationIcon = AppIcons.arrowBack,
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 应用信息卡片
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                variant = CardVariant.Default
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SvgLogo(
                        logo = DatabaseLogo.DATABASES,
                        modifier = Modifier.size(72.dp),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource("app_name", language),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "v1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 支持的平台
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == Language.CHINESE) "支持的平台" else "Supported Platforms",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text("Android") },
                            leadingIcon = {
                                Icon(
                                    AppIcons.android,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text("JVM/Desktop") },
                            leadingIcon = {
                                Icon(
                                    AppIcons.computer,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 支持的数据库
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == Language.CHINESE) "支持的数据库" else "Supported Databases",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = true,
                                onClick = {},
                                label = { Text("MySQL") },
                                leadingIcon = {
                                    SvgLogo(
                                        logo = DatabaseLogo.MYSQL,
                                        modifier = Modifier.size(18.dp),
                                        contentDescription = null
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            FilterChip(
                                selected = true,
                                onClick = {},
                                label = { Text("PostgreSQL") },
                                leadingIcon = {
                                    SvgLogo(
                                        logo = DatabaseLogo.POSTGRESQL,
                                        modifier = Modifier.size(18.dp),
                                        contentDescription = null
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            FilterChip(
                                selected = true,
                                onClick = {},
                                label = { Text("Redis") },
                                leadingIcon = {
                                    SvgLogo(
                                        logo = DatabaseLogo.REDIS,
                                        modifier = Modifier.size(18.dp),
                                        contentDescription = null
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 更新日志（网页）
            AppCard(modifier = Modifier.fillMaxWidth()) {
                AppWebView(
                    url = "https://databasemanager.xiaoxiao.space/en/changelog.html",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                )
            }
        }
    }
}
