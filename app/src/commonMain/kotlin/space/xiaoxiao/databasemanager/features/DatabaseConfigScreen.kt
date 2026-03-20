package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.core.DatabaseType
import space.xiaoxiao.databasemanager.core.createDatabaseClient
import space.xiaoxiao.databasemanager.theme.AppSpacing
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 数据库配置屏幕 - 用于添加或编辑数据库配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseConfigScreen(
    language: Language = Language.CHINESE,
    configToEdit: DatabaseConfigInfo? = null,
    onNavigateBack: () -> Unit,
    onSave: (DatabaseConfigInfo) -> Unit
) {
    var name by remember { mutableStateOf(configToEdit?.name ?: "") }
    var type by remember { mutableStateOf(configToEdit?.type ?: DatabaseType.MYSQL) }
    var host by remember { mutableStateOf(configToEdit?.host ?: "localhost") }
    var port by remember { mutableStateOf(configToEdit?.port?.toString() ?: "3306") }
    var database by remember { mutableStateOf(configToEdit?.database ?: "") }
    var username by remember { mutableStateOf(configToEdit?.username ?: "root") }
    var password by remember { mutableStateOf(configToEdit?.password ?: "") }
    var charset by remember { mutableStateOf(configToEdit?.charset) }
    var passwordVisible by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var charsetExpanded by remember { mutableStateOf(false) }

    // 测试连接状态
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var testSuccess by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()

    // ==================== 表单验证 ====================

    // 端口验证
    val portError: String? = remember(port) {
        val portNum = port.toIntOrNull()
        when {
            port.isEmpty() -> null // 空值会在保存按钮禁用逻辑中处理
            portNum == null -> if (language == Language.CHINESE) "端口号必须是数字" else "Port must be a number"
            portNum !in 1..65535 -> if (language == Language.CHINESE) "端口号范围: 1-65535" else "Port range: 1-65535"
            else -> null
        }
    }

    // 主机名验证
    val hostError: String? = remember(host) {
        when {
            host.isEmpty() -> null // 空值会在保存按钮禁用逻辑中处理
            !host.matches(Regex("^[a-zA-Z0-9.\\-\\[\\]:]+$")) ->
                if (language == Language.CHINESE) "主机名包含非法字符" else "Host contains invalid characters"
            else -> null
        }
    }

    // 数据库名验证
    val databaseError: String? = remember(database) {
        when {
            database.isEmpty() -> null // 空值会在保存按钮禁用逻辑中处理
            database.length > 64 ->
                if (language == Language.CHINESE) "数据库名不能超过64个字符" else "Database name cannot exceed 64 characters"
            !database.matches(Regex("^[a-zA-Z0-9_-]+$")) ->
                if (language == Language.CHINESE) "数据库名只能包含字母、数字、下划线和短横线" else "Database name can only contain letters, numbers, underscore and hyphen"
            else -> null
        }
    }

    // 连接名称验证
    val nameError: String? = remember(name) {
        when {
            name.isEmpty() -> null // 空值会在保存按钮禁用逻辑中处理
            name.length > 100 ->
                if (language == Language.CHINESE) "连接名称不能超过100个字符" else "Connection name cannot exceed 100 characters"
            else -> null
        }
    }

    // 表单是否有效
    val isFormValid = remember(name, host, port, database, nameError, hostError, portError, databaseError) {
        name.isNotBlank() &&
        host.isNotBlank() &&
        database.isNotBlank() &&
        nameError == null &&
        hostError == null &&
        portError == null &&
        databaseError == null
    }

    // 构建当前配置
    fun buildConfig() = DatabaseConfigInfo(
        id = configToEdit?.id ?: UUID.randomUUID().toString(),
        name = name,
        type = type,
        host = host,
        port = port.toIntOrNull() ?: 3306,
        database = database,
        username = username,
        password = password,
        charset = charset
    )

    // 字符集选项（根据数据库类型）
    val charsetOptions = when (type) {
        DatabaseType.MYSQL -> listOf("utf8mb4", "utf8", "latin1", "gbk")
        DatabaseType.POSTGRESQL -> listOf("UTF8", "LATIN1", "GBK")
        else -> emptyList()
    }

    // 是否显示字符集选择（仅 MySQL 和 PostgreSQL）
    val showCharset = type == DatabaseType.MYSQL || type == DatabaseType.POSTGRESQL

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (configToEdit != null) "edit_database" else "add_database", language))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource("back", language))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val config = buildConfig()
                            onSave(config)
                            onNavigateBack()
                        },
                        enabled = isFormValid
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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceMd)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; testResult = null },
                label = { Text(stringResource("connection_name", language)) },
                placeholder = { Text(stringResource("connection_name_placeholder", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Label, contentDescription = null, modifier = Modifier.size(18.dp)) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = type.getDisplayName(language),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource("database_type", language)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource("db_type_mysql", language)) },
                        onClick = {
                            type = DatabaseType.MYSQL
                            port = DatabaseType.MYSQL.getDefaultPort()
                            charset = "utf8mb4"
                            expanded = false
                            testResult = null
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource("db_type_postgresql", language)) },
                        onClick = {
                            type = DatabaseType.POSTGRESQL
                            port = DatabaseType.POSTGRESQL.getDefaultPort()
                            charset = "UTF8"
                            expanded = false
                            testResult = null
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource("db_type_redis", language)) },
                        onClick = {
                            type = DatabaseType.REDIS
                            port = DatabaseType.REDIS.getDefaultPort()
                            charset = null
                            expanded = false
                            testResult = null
                        }
                    )
                }
            }
            OutlinedTextField(
                value = host,
                onValueChange = { host = it; testResult = null },
                label = { Text(stringResource("host", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = null, modifier = Modifier.size(18.dp)) },
                isError = hostError != null,
                supportingText = hostError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            OutlinedTextField(
                value = port,
                onValueChange = { port = it; testResult = null },
                label = { Text(stringResource("port", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Filled.SettingsEthernet, contentDescription = null, modifier = Modifier.size(18.dp)) },
                isError = portError != null,
                supportingText = portError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            OutlinedTextField(
                value = database,
                onValueChange = { database = it; testResult = null },
                label = { Text(stringResource("database_name", language)) },
                placeholder = { Text(stringResource("database_name_placeholder", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Storage, contentDescription = null, modifier = Modifier.size(18.dp)) },
                isError = databaseError != null,
                supportingText = databaseError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; testResult = null },
                label = { Text(stringResource("username", language)) },
                placeholder = { Text(stringResource("username_placeholder", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; testResult = null },
                label = { Text(stringResource("password", language)) },
                placeholder = { Text(stringResource("password_placeholder", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) stringResource("hide_password", language) else stringResource("show_password", language),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            // 字符集选择（仅 MySQL 和 PostgreSQL）
            if (showCharset) {
                ExposedDropdownMenuBox(
                    expanded = charsetExpanded,
                    onExpandedChange = { charsetExpanded = !charsetExpanded }
                ) {
                    OutlinedTextField(
                        value = charset ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource("charset", language)) },
                        placeholder = { Text(stringResource("select_charset", language)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = charsetExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable),
                        leadingIcon = { Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    ExposedDropdownMenu(
                        expanded = charsetExpanded,
                        onDismissRequest = { charsetExpanded = false }
                    ) {
                        charsetOptions.forEach { cs ->
                            DropdownMenuItem(
                                text = { Text(cs) },
                                onClick = {
                                    charset = cs
                                    charsetExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 测试连接按钮和结果
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        isTesting = true
                        testResult = null
                        testSuccess = null

                        try {
                            val config = buildConfig()
                            val dbConfig = config.toDatabaseConfig()
                            val client = createDatabaseClient(dbConfig)

                            when (val result = client.context.connect()) {
                                is space.xiaoxiao.databasemanager.core.ConnectionStatus.Connected -> {
                                    testSuccess = true
                                    testResult = if (language == Language.CHINESE) "连接成功！" else "Connection successful!"
                                    client.context.disconnect()
                                }
                                is space.xiaoxiao.databasemanager.core.ConnectionStatus.Error -> {
                                    testSuccess = false
                                    testResult = result.message
                                }
                                else -> {
                                    testSuccess = false
                                    testResult = if (language == Language.CHINESE) "连接失败" else "Connection failed"
                                }
                            }
                        } catch (e: Exception) {
                            testSuccess = false
                            testResult = e.message ?: (if (language == Language.CHINESE) "未知错误" else "Unknown error")
                        } finally {
                            isTesting = false
                        }
                    }
                },
                enabled = !isTesting && host.isNotBlank() && database.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (language == Language.CHINESE) "测试中..." else "Testing...")
                } else {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource("test_connection", language))
                }
            }

            // 显示测试结果
            testResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (testSuccess == true) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (testSuccess == true) Icons.Filled.CheckCircle else Icons.Filled.Error,
                            contentDescription = null,
                            tint = if (testSuccess == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (testSuccess == true) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { testResult = null; testSuccess = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
