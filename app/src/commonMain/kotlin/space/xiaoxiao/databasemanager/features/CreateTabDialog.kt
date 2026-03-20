package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.features.DatabaseConfigInfo
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.CardVariant
import space.xiaoxiao.databasemanager.components.DatabaseTypeIcon
import space.xiaoxiao.databasemanager.theme.AppSpacing

@Composable
fun CreateTabDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onCreate: (DatabaseConfigInfo, String) -> Unit,
    databases: List<DatabaseConfigInfo>,
    language: Language
) {
    if (!showDialog) return

    var selectedDb by remember { mutableStateOf<DatabaseConfigInfo?>(null) }
    var sessionName by remember { mutableStateOf("") }
    var showDbSelector by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource("new_query_session", language)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 数据库选择
                Column {
                    Text(
                        text = stringResource("select_database", language),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = selectedDb?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text(text = stringResource("select_database", language)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = true) { showDbSelector = true },
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            Icon(Icons.Filled.ArrowDropDown, null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Session 名输入
                Column {
                    Text(
                        text = stringResource("session_name", language),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = sessionName,
                        onValueChange = { sessionName = it },
                        label = { Text(text = stringResource("session_name", language)) },
                        placeholder = { Text(text = stringResource("session_name_placeholder", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedDb?.let { onCreate(it, sessionName) } },
                enabled = selectedDb != null,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = stringResource("create", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, shape = RoundedCornerShape(10.dp)) {
                Text(text = stringResource("cancel", language))
            }
        }
    )

    // 数据库选择对话框
    if (showDbSelector) {
        DatabaseSelectorForCreateTabDialog(
            showDialog = showDbSelector,
            onDismissRequest = { showDbSelector = false },
            onSelect = { config ->
                selectedDb = config
                showDbSelector = false
            },
            databases = databases,
            language = language
        )
    }
}

@Composable
fun DatabaseSelectorForCreateTabDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onSelect: (DatabaseConfigInfo) -> Unit,
    databases: List<DatabaseConfigInfo>,
    language: Language
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource("select_database", language)) },
        text = {
            if (databases.isEmpty()) {
                Text(text = stringResource("no_database_configured", language))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(databases) { db ->
                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSelect(db) },
                            variant = CardVariant.Default
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(AppSpacing.spaceLg),
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
                                        style = MaterialTheme.typography.titleMedium
                                    )
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
                Text(text = stringResource("cancel", language))
            }
        }
    )
}
