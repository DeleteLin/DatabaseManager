package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.core.ColumnDefinition

/**
 * 字段编辑对话框
 * 用于添加或编辑字段
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldEditorDialog(
    language: Language,
    title: String,
    initialColumn: ColumnDefinition? = null,
    onDismiss: () -> Unit,
    onSave: (ColumnDefinition) -> Unit
) {
    var name by remember { mutableStateOf(initialColumn?.name ?: "") }
    var typeName by remember { mutableStateOf(initialColumn?.typeName ?: "INT") }
    var length by remember { mutableStateOf("") }
    var charset by remember { mutableStateOf(initialColumn?.charset ?: "") }
    var isNullable by remember { mutableStateOf(initialColumn?.isNullable ?: true) }
    var defaultValue by remember { mutableStateOf(initialColumn?.defaultValue ?: "") }
    var comment by remember { mutableStateOf(initialColumn?.comment ?: "") }
    var isAutoIncrement by remember { mutableStateOf(initialColumn?.isAutoIncrement ?: false) }

    var charsetExpanded by remember { mutableStateOf(false) }

    // 解析初始类型，提取长度
    LaunchedEffect(initialColumn?.typeName) {
        initialColumn?.typeName?.let { type ->
            val lengthMatch = Regex("(.+)\\((\\d+)\\)").find(type)
            if (lengthMatch != null) {
                typeName = lengthMatch.groupValues[1]
                length = lengthMatch.groupValues[2]
            } else {
                typeName = type
            }
        }
    }

    // 解析初始字符集
    LaunchedEffect(initialColumn?.charset) {
        charset = initialColumn?.charset ?: ""
    }

    val commonTypes = listOf(
        "INT", "BIGINT", "SMALLINT", "TINYINT",
        "VARCHAR", "CHAR", "TEXT", "LONGTEXT",
        "DECIMAL", "DOUBLE", "FLOAT",
        "DATE", "DATETIME", "TIMESTAMP", "TIME",
        "BOOLEAN", "BLOB"
    )

    // 文本类型需要字符集选择
    val isTextType by derivedStateOf {
        typeName in listOf("VARCHAR", "CHAR", "TEXT", "LONGTEXT", "MEDIUMTEXT", "TINYTEXT")
    }

    val charsetOptions = listOf(
        "" to "默认",
        "utf8mb4" to "UTF-8 MB4",
        "utf8" to "UTF-8",
        "latin1" to "Latin1",
        "gbk" to "GBK"
    )

    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 字段名
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

                // 字符集选择器（仅文本类型显示）
                if (isTextType) {
                    ExposedDropdownMenuBox(
                        expanded = charsetExpanded,
                        onExpandedChange = { charsetExpanded = !charsetExpanded }
                    ) {
                        OutlinedTextField(
                            value = charset,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("字符集 (可选)") },
                            placeholder = { Text("选择字符集") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = charsetExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = charsetExpanded,
                            onDismissRequest = { charsetExpanded = false }
                        ) {
                            charsetOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        charset = value
                                        charsetExpanded = false
                                    }
                                )
                            }
                        }
                    }
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
                    // 验证：需要长度的类型必须指定长度
                    if (typeName in listOf("VARCHAR", "CHAR", "DECIMAL", "NUMERIC", "BIT") && length.isEmpty()) {
                        validationError = "数据类型 '$typeName' 必须指定长度"
                        return@Button
                    }
                    val fullType = if (length.isNotEmpty()) {
                        "$typeName($length)"
                    } else {
                        typeName
                    }
                    onSave(
                        ColumnDefinition(
                            name = name,
                            typeName = fullType,
                            isNullable = isNullable,
                            isAutoIncrement = isAutoIncrement,
                            defaultValue = defaultValue.ifBlank { null },
                            comment = comment.ifBlank { null },
                            charset = charset.ifBlank { null }
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource("save", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource("cancel", language))
            }
        }
    )

    validationError?.let { error ->
        AlertDialog(
            onDismissRequest = { validationError = null },
            title = { Text("验证失败") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = { validationError = null }) {
                    Text("确定")
                }
            }
        )
    }
}