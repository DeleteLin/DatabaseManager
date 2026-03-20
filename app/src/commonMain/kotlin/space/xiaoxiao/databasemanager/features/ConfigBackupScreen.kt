package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigBackupScreen(
    language: Language,
    onNavigateBack: () -> Unit,
    onExportConfig: suspend (password: String) -> Result<Unit>,
    onImportConfig: suspend (password: String, fileContent: String) -> Result<Unit>,
    onClearConfigAndExit: suspend () -> Unit,
    pickFile: suspend () -> String?
) {
    val scope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val titleText = stringResource("config_backup", language)
    val descText = stringResource("config_backup_desc", language)
    val exportText = stringResource("export_config", language)
    val importText = stringResource("import_config", language)
    val clearText = stringResource("clear_config_and_exit", language)
    val exportDetail = stringResource("config_backup_export_detail", language)
    val importDetail = stringResource("config_backup_import_detail", language)
    val clearDetail = stringResource("config_backup_clear_detail", language)
    val okText = stringResource("ok", language)
    val cancelText = stringResource("cancel", language)
    val confirmText = stringResource("confirm", language)
    val exportSuccess = stringResource("export_success", language)
    val exportFailed = stringResource("export_failed", language)
    val importSuccess = stringResource("import_success", language)
    val importFailedTpl = stringResource("import_failed", language)
    val confirmClearText = stringResource("confirm_clear_config_exit", language)

    if (toastMessage != null) {
        AlertDialog(
            onDismissRequest = { toastMessage = null },
            confirmButton = {
                TextButton(onClick = { toastMessage = null }) {
                    Text(okText)
                }
            },
            text = { Text(toastMessage ?: "") }
        )
    }

    if (showExportDialog) {
        PasswordConfirmDialog(
            language = language,
            title = exportText,
            onDismiss = { if (!busy) showExportDialog = false },
            onConfirm = { password ->
                if (busy) return@PasswordConfirmDialog
                busy = true
                scope.launch {
                    try {
                        val result = onExportConfig(password)
                        toastMessage = if (result.isSuccess) exportSuccess else exportFailed
                    } finally {
                        busy = false
                        showExportDialog = false
                    }
                }
            }
        )
    }

    if (showImportDialog) {
        PasswordInputDialog(
            language = language,
            title = importText,
            onDismiss = { if (!busy) showImportDialog = false },
            onConfirm = { password ->
                if (busy) return@PasswordInputDialog
                busy = true
                scope.launch {
                    try {
                        val fileContent = pickFile()
                        if (fileContent == null) return@launch
                        val result = onImportConfig(password, fileContent)
                        toastMessage = if (result.isSuccess) {
                            importSuccess
                        } else {
                            importFailedTpl.replace("{message}", result.exceptionOrNull()?.message ?: "")
                        }
                    } finally {
                        busy = false
                        showImportDialog = false
                    }
                }
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { if (!busy) showClearDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (busy) return@TextButton
                        busy = true
                        scope.launch {
                            try {
                                onClearConfigAndExit()
                            } finally {
                                busy = false
                                showClearDialog = false
                            }
                        }
                    }
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!busy) showClearDialog = false }) {
                    Text(cancelText)
                }
            },
            text = { Text(confirmClearText) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(AppSpacing.spaceLg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
        ) {
            Text(
                text = descText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 导出配置
            ListItem(
                headlineContent = { Text(exportText) },
                supportingContent = { Text(exportDetail) },
                leadingContent = {
                    Icon(Icons.Filled.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (!busy) showExportDialog = true }
            )

            // 导入配置
            ListItem(
                headlineContent = { Text(importText) },
                supportingContent = { Text(importDetail) },
                leadingContent = {
                    Icon(Icons.Filled.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (!busy) showImportDialog = true }
            )

            // 清空配置并退出
            ListItem(
                headlineContent = { Text(clearText, color = MaterialTheme.colorScheme.error) },
                supportingContent = {
                    Text(
                        text = clearDetail,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Filled.DeleteForever,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (!busy) showClearDialog = true }
            )
        }
    }
}

@Composable
private fun PasswordInputDialog(
    language: Language,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: suspend (password: String) -> Unit,
    minLength: Int = 8
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val passwordLabel = stringResource("enter_backup_password", language)
    val tooShortTpl = stringResource("password_too_short", language)
    val confirmText = stringResource("confirm", language)
    val cancelText = stringResource("cancel", language)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        error = null
                    },
                    label = { Text(passwordLabel) },
                    singleLine = true
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (password.length < minLength) {
                        error = tooShortTpl.replace("{min}", minLength.toString())
                        return@TextButton
                    }
                    scope.launch { onConfirm(password) }
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(cancelText) }
        }
    )
}

@Composable
private fun PasswordConfirmDialog(
    language: Language,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: suspend (password: String) -> Unit,
    minLength: Int = 8
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val passwordLabel = stringResource("enter_backup_password", language)
    val confirmLabel = stringResource("confirm_backup_password", language)
    val tooShortTpl = stringResource("password_too_short", language)
    val mismatchText = stringResource("password_mismatch", language)
    val confirmText = stringResource("confirm", language)
    val cancelText = stringResource("cancel", language)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        error = null
                    },
                    label = { Text(passwordLabel) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = {
                        confirm = it
                        error = null
                    },
                    label = { Text(confirmLabel) },
                    singleLine = true
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (password.length < minLength) {
                        error = tooShortTpl.replace("{min}", minLength.toString())
                        return@TextButton
                    }
                    if (password != confirm) {
                        error = mismatchText
                        return@TextButton
                    }
                    scope.launch { onConfirm(password) }
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(cancelText) }
        }
    )
}
