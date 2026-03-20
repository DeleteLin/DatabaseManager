package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.CardVariant
import space.xiaoxiao.databasemanager.theme.AppSpacing
import space.xiaoxiao.databasemanager.core.*

/**
 * 新建表对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableCreateDialog(
    language: Language,
    onDismiss: () -> Unit,
    onCreate: (TableDefinition) -> Unit
) {
    var tableName by remember { mutableStateOf("") }
    var columns by remember { mutableStateOf(listOf<ColumnDefinition>()) }
    var primaryKeys by remember { mutableStateOf(setOf<String>()) }
    var showAddColumnDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource("create_table", language)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                // 表名输入
                OutlinedTextField(
                    value = tableName,
                    onValueChange = { tableName = it },
                    label = { Text(stringResource("table_name", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 字段列表
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource("fields", language) + ": ${columns.size}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    IconButton(onClick = { showAddColumnDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource("add", language))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (columns.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource("no_data", language),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(columns) { column ->
                            ColumnItem(
                                column = column,
                                isPrimaryKey = column.name in primaryKeys,
                                onTogglePrimaryKey = {
                                    primaryKeys = if (column.name in primaryKeys) {
                                        primaryKeys - column.name
                                    } else {
                                        primaryKeys + column.name
                                    }
                                },
                                onDelete = {
                                    columns = columns.filter { it.name != column.name }
                                    primaryKeys = primaryKeys - column.name
                                },
                                language = language
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tableName.isNotBlank() && columns.isNotEmpty()) {
                        onCreate(
                            TableDefinition(
                                name = tableName,
                                columns = columns,
                                primaryKeys = primaryKeys.toList()
                            )
                        )
                    }
                },
                enabled = tableName.isNotBlank() && columns.isNotEmpty()
            ) {
                Text(stringResource("ok", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource("cancel", language))
            }
        }
    )

    // 添加字段对话框
    if (showAddColumnDialog) {
        AddColumnDialog(
            language = language,
            existingColumnNames = columns.map { it.name },
            onDismiss = { showAddColumnDialog = false },
            onAdd = { column ->
                columns = columns + column
                showAddColumnDialog = false
            }
        )
    }
}

@Composable
private fun ColumnItem(
    column: ColumnDefinition,
    isPrimaryKey: Boolean,
    onTogglePrimaryKey: () -> Unit,
    onDelete: () -> Unit,
    language: Language
) {
    AppCard(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.spaceXs),
        variant = CardVariant.Default
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = column.name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = column.typeName +
                            (if (!column.isNullable) " NOT NULL" else "") +
                            (column.defaultValue?.let { " DEFAULT $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 主键切换
            FilterChip(
                selected = isPrimaryKey,
                onClick = onTogglePrimaryKey,
                label = { Text(stringResource("pk_badge", language)) }
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource("delete", language))
            }
        }
    }
}

/**
 * 添加字段对话框（用于新建表）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddColumnDialog(
    language: Language,
    existingColumnNames: List<String>,
    onDismiss: () -> Unit,
    onAdd: (ColumnDefinition) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var typeName by remember { mutableStateOf("INT") }
    var length by remember { mutableStateOf("") }
    var isNullable by remember { mutableStateOf(true) }
    var defaultValue by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var isAutoIncrement by remember { mutableStateOf(false) }

    val commonTypes = listOf(
        "INT", "BIGINT", "SMALLINT", "TINYINT",
        "VARCHAR", "CHAR", "TEXT", "LONGTEXT",
        "DECIMAL", "DOUBLE", "FLOAT",
        "DATE", "DATETIME", "TIMESTAMP", "TIME",
        "BOOLEAN", "BLOB"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource("add_field", language)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource("field_name", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 类型选择
                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = typeName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource("field_type", language)) },
                        modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        commonTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    typeName = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 长度（可选）
                if (typeName in listOf("VARCHAR", "CHAR", "DECIMAL")) {
                    OutlinedTextField(
                        value = length,
                        onValueChange = { length = it },
                        label = { Text(stringResource("field_length", language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 默认值
                OutlinedTextField(
                    value = defaultValue,
                    onValueChange = { defaultValue = it },
                    label = { Text(stringResource("field_default", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 注释
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(stringResource("field_comment", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 选项
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = isNullable,
                            onCheckedChange = { isNullable = it }
                        )
                        Text(stringResource("field_nullable", language))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = isAutoIncrement,
                            onCheckedChange = { isAutoIncrement = it }
                        )
                        Text(stringResource("field_auto_increment", language))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fullType = if (length.isNotEmpty() && typeName in listOf("VARCHAR", "CHAR", "DECIMAL")) {
                        "$typeName($length)"
                    } else {
                        typeName
                    }
                    onAdd(
                        ColumnDefinition(
                            name = name,
                            typeName = fullType,
                            isNullable = isNullable,
                            isAutoIncrement = isAutoIncrement,
                            defaultValue = defaultValue.ifBlank { null },
                            comment = comment.ifBlank { null }
                        )
                    )
                },
                enabled = name.isNotBlank() && name !in existingColumnNames
            ) {
                Text(stringResource("add", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource("cancel", language))
            }
        }
    )
}