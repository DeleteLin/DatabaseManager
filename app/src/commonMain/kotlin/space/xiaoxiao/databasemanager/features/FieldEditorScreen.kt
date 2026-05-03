package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.components.AppButton
import space.xiaoxiao.databasemanager.components.AppConfirmDialog
import space.xiaoxiao.databasemanager.components.AppTextButton
import space.xiaoxiao.databasemanager.components.AppTextField
import space.xiaoxiao.databasemanager.components.AppTopBar
import space.xiaoxiao.databasemanager.components.SmallLoadingIndicator
import space.xiaoxiao.databasemanager.core.ColumnDefinition
import space.xiaoxiao.databasemanager.theme.AppSpacing
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

    // 预计算字符串资源（非 Composable 回调中无法使用 stringResource）
    val strFieldNameEmpty = stringResource("field_name_empty", language)
    val strTypeLengthRequired = stringResource("type_length_required", language)
    val strSaveFailed = stringResource("save_failed", language)

    val charsetOptions = listOf(
        "" to stringResource("charset_default", language),
        "utf8mb4" to stringResource("charset_utf8mb4", language),
        "utf8" to stringResource("charset_utf8", language),
        "latin1" to stringResource("charset_latin1", language),
        "gbk" to stringResource("charset_gbk", language)
    )

    val title = if (isEditMode) stringResource("edit_field", language) else stringResource("add_field", language)
    val nameExistsError = stringResource("field_name_exists_template", language).replace("{name}", name)

    Scaffold(
        topBar = {
            AppTopBar(
                title = title,
                onNavigationClick = onNavigateBack,
                actions = {
                    AppTextButton(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = strFieldNameEmpty
                            } else if (!isEditMode && name in existingColumnNames) {
                                errorMessage = nameExistsError
                            } else {
                                showSaveDialog = true
                            }
                        },
                        enabled = !isSaving
                    ) {
                        Text(stringResource("save", language))
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
                .padding(AppSpacing.spaceLg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceLg)
        ) {
            // 字段名
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource("field_name", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !isEditMode && name in existingColumnNames,
                supportingText = {
                    if (!isEditMode && name in existingColumnNames) {
                        Text(stringResource("field_name_exists", language), color = MaterialTheme.colorScheme.error)
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
                    label = { Text(stringResource("data_type", language)) },
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
            AppTextField(
                value = length,
                onValueChange = { length = it.filter { c -> c.isDigit() } },
                label = stringResource("length_optional", language),
                singleLine = true,
                placeholder = stringResource("length_placeholder", language)
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
                        label = { Text(stringResource("charset_optional", language)) },
                        placeholder = { Text(stringResource("select_charset", language)) },
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
            AppTextField(
                value = defaultValue,
                onValueChange = { defaultValue = it },
                label = stringResource("default_value_optional", language),
                singleLine = true
            )

            // 注释
            AppTextField(
                value = comment,
                onValueChange = { comment = it },
                label = stringResource("comment_optional", language),
                modifier = Modifier.height(100.dp),
                maxLines = 4
            )

            // 可空
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource("allow_null", language))
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
                Text(stringResource("auto_increment_label", language))
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
            title = { Text(stringResource("confirm_save", language)) },
            text = {
                Column {
                    Text(stringResource("field_name", language) + "：$name")
                    val fullName = if (length.isNotEmpty()) "$typeName($length)" else typeName
                    Text(stringResource("type", language) + "：$fullName")
                    if (isNullable) Text(stringResource("allow_null", language))
                    if (isAutoIncrement) Text(stringResource("field_auto_increment", language))
                    if (charset.isNotEmpty()) Text(stringResource("charset", language) + "：$charset")
                }
            },
            confirmButton = {
                AppButton(
                    onClick = {
                        // 验证：需要长度的类型必须指定长度
                        if (typeName in requiresLengthTypes && length.isEmpty()) {
                            errorMessage = strTypeLengthRequired.replace("{type}", typeName)
                            return@AppButton
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
                                    errorMessage = error.message ?: strSaveFailed
                                }
                            )
                            isSaving = false
                        }
                        showSaveDialog = false
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        SmallLoadingIndicator()
                        Spacer(modifier = Modifier.width(AppSpacing.spaceSm))
                        Text(stringResource("saving", language))
                    } else {
                        Text(stringResource("ok", language))
                    }
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showSaveDialog = false }) {
                    Text(stringResource("cancel", language))
                }
            }
        )
    }

    // 错误对话框
    errorMessage?.let { msg ->
        AppConfirmDialog(
            title = stringResource("error", language),
            message = msg,
            confirmText = stringResource("ok", language),
            cancelText = stringResource("ok", language),
            onConfirm = { errorMessage = null },
            onDismiss = { errorMessage = null },
            icon = Icons.Filled.Error
        )
    }
}
