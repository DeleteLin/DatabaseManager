package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource

/**
 * 创建索引对话框
 */
@Composable
fun IndexCreateDialog(
    language: Language,
    columns: List<String>,
    onDismiss: () -> Unit,
    onCreate: (indexName: String, columns: List<String>, isUnique: Boolean) -> Unit
) {
    var indexName by remember { mutableStateOf("") }
    var selectedColumns by remember { mutableStateOf(setOf<String>()) }
    var isUnique by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // 预计算字符串资源
    val indexNameErrorStr = stringResource("index_name", language) + " " + stringResource("error", language)
    val indexColumnsErrorStr = stringResource("index_columns", language) + " " + stringResource("error", language)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource("create_index", language)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(350.dp)) {
                // 索引名
                OutlinedTextField(
                    value = indexName,
                    onValueChange = {
                        indexName = it
                        error = null
                    },
                    label = { Text(stringResource("index_name", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null
                )

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 唯一索引选项
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isUnique,
                        onCheckedChange = { isUnique = it }
                    )
                    Text(stringResource("index_unique", language))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 选择字段
                Text(
                    text = stringResource("index_columns", language) + ":",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (columns.isEmpty()) {
                    Text(
                        text = stringResource("no_data", language),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(columns) { column ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = column in selectedColumns,
                                    onCheckedChange = { checked ->
                                        selectedColumns = if (checked) {
                                            selectedColumns + column
                                        } else {
                                            selectedColumns - column
                                        }
                                    }
                                )
                                Text(column)
                            }
                        }
                    }
                }

                // 显示选中数量
                Text(
                    text = "${selectedColumns.size} ${stringResource("fields", language).lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        indexName.isBlank() -> error = indexNameErrorStr
                        selectedColumns.isEmpty() -> error = indexColumnsErrorStr
                        else -> onCreate(indexName, selectedColumns.toList(), isUnique)
                    }
                },
                enabled = indexName.isNotBlank() && selectedColumns.isNotEmpty()
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
}