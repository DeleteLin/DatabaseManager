package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.window.Dialog
import space.xiaoxiao.databasemanager.core.DatabaseType
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.storage.SqlTemplate
import space.xiaoxiao.databasemanager.storage.SqlTemplateLibrary
import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.CardVariant

/**
 * 模板选择对话框
 * 用于展示和选择常用 SQL 模板
 */
@Composable
fun TemplateSelectorDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onSelectTemplate: (SqlTemplate) -> Unit,
    databaseType: DatabaseType,
    language: Language = Language.CHINESE
) {
    if (!showDialog) return

    val templates = remember(databaseType) {
        SqlTemplateLibrary.getTemplates(databaseType)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                Icons.Filled.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource("template_common_sql", language),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            if (templates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource("no_data", language),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(templates) { template ->
                        TemplateListItem(
                            template = template,
                            onClick = { onSelectTemplate(template) },
                            language = language
                        )
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

/**
 * 模板列表项
 */
@Composable
private fun TemplateListItem(
    template: SqlTemplate,
    onClick: () -> Unit,
    language: Language
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        variant = CardVariant.Default
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            // SQL 预览
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = template.sql,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
