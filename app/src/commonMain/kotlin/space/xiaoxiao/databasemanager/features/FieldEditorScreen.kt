package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.core.ColumnDefinition
import kotlinx.coroutines.launch

/**
 * 字段编辑二级页面
 * 用于添加或编辑表字段
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldEditorScreen(
    language: Language = Language.CHINESE,
    tableName: String,
    isEditMode: Boolean = false,
    initialColumn: ColumnDefinition? = null,
    existingColumnNames: List<String> = emptyList(),
    onNavigateBack: () -> Unit,
    onSave: suspend (ColumnDefinition) -> Result<Unit>
) {
    val scope = rememberCoroutineScope()
    var showSaveDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // 字段属性状态
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
        "utf8mb4" to "UTF-8 MB4 (推荐)",
        "utf8" to "UTF-8",
        "latin1" to "Latin1",
        "gbk" to "GBK"
    )

    val title = if (isEditMode) "编辑字段" else "添加字段"
    val nameExistsError = "字段名 '$name' 已存在"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "字段名不能为空"
                            } else if (!isEditMode && name in existingColumnNames) {
                                errorMessage = nameExistsError
                            } else {
                                showSaveDialog = true
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 字段名
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("字段名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !isEditMode && name in existingColumnNames,
                supportingText = {
                    if (!isEditMode && name in existingColumnNames) {
                        Text("此字段名已存在", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

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
                    label = { Text("数据类型") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
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

            // 长度
            OutlinedTextField(
                value = length,
                onValueChange = { length = it.filter { c -> c.isDigit() } },
                label = { Text("长度 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("例如：255") }
            )

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
            }

            // 默认值
            OutlinedTextField(
                value = defaultValue,
                onValueChange = { defaultValue = it },
                label = { Text("默认值 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 注释
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("注释 (可选)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            // 可空
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("允许 NULL")
                Switch(
                    checked = isNullable,
                    onCheckedChange = { isNullable = it }
                )
            }

            // 自增
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("自增 (AUTO_INCREMENT)")
                Switch(
                    checked = isAutoIncrement,
                    onCheckedChange = { isAutoIncrement = it }
                )
            }
        }
    }

    val requiresLengthTypes = listOf("VARCHAR", "CHAR", "DECIMAL", "NUMERIC", "BIT")

    // 保存确认对话框
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            icon = {
                Icon(Icons.Filled.Save, contentDescription = null)
            },
            title = { Text("确认保存") },
            text = {
                Column {
                    Text("字段名：$name")
                    val fullName = if (length.isNotEmpty()) "$typeName($length)" else typeName
                    Text("类型：$fullName")
                    if (isNullable) Text("允许 NULL")
                    if (isAutoIncrement) Text("自增")
                    if (charset.isNotEmpty()) Text("字符集：$charset")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 验证：需要长度的类型必须指定长度
                        if (typeName in requiresLengthTypes && length.isEmpty()) {
                            errorMessage = "数据类型 '$typeName' 必须指定长度"
                            return@Button
                        }
                        isSaving = true
                        scope.launch {
                            val fullName = if (length.isNotEmpty()) "$typeName($length)" else typeName
                            val columnDef = ColumnDefinition(
                                name = name,
                                typeName = fullName,
                                isNullable = isNullable,
                                isPrimaryKey = false,
                                isAutoIncrement = isAutoIncrement,
                                defaultValue = defaultValue.ifBlank { null },
                                comment = comment.ifBlank { null },
                                charset = charset.ifBlank { null }
                            )
                            val result = onSave(columnDef)
                            result.fold(
                                onSuccess = {
                                    onNavigateBack()
                                },
                                onFailure = { error ->
                                    errorMessage = error.message ?: "保存失败，请稍后重试"
                                }
                            )
                            isSaving = false
                        }
                        showSaveDialog = false
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存中...")
                    } else {
                        Text("确认")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 错误对话框
    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            icon = {
                Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("错误") },
            text = { Text(msg) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("确定")
                }
            }
        )
    }
}
