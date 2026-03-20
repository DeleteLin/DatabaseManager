package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.CardVariant
import space.xiaoxiao.databasemanager.core.DatabaseType
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.storage.*

/**
 * AI 生成语句对话框
 * 如果未配置 AI，提示用户去"更多"页面配置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSqlGeneratorDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onInsertSql: (String) -> Unit,
    databaseType: DatabaseType,
    tableSchema: String?,
    language: Language = Language.CHINESE,
    aiConfigStorage: AiConfigStorage? = null,
    onNavigateToAiConfig: (() -> Unit)? = null
) {
    if (!showDialog) return

    val scope = rememberCoroutineScope()

    // 加载 AI 配置
    val savedConfig = remember { aiConfigStorage?.loadConfig() }
    val hasConfigured = savedConfig != null && savedConfig.apiKey.isNotBlank()

    // 未配置时显示提示对话框
    if (!hasConfigured) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            icon = {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = stringResource("ai_config_required", language),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource("ai_please_go_to_more_page", language),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissRequest()
                        onNavigateToAiConfig?.invoke()
                    }
                ) {
                    Text(stringResource("go_to_more_page", language))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource("cancel", language))
                }
            }
        )
        return
    }

    // 已配置，显示 AI 生成界面
    AiGeneratorScreen(
        showDialog = true,
        onDismissRequest = onDismissRequest,
        onInsertSql = onInsertSql,
        databaseType = databaseType,
        tableSchema = tableSchema,
        language = language,
        aiConfigStorage = aiConfigStorage
    )
}

/**
 * AI 生成界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiGeneratorScreen(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onInsertSql: (String) -> Unit,
    databaseType: DatabaseType,
    tableSchema: String?,
    language: Language,
    aiConfigStorage: AiConfigStorage?
) {
    val scope = rememberCoroutineScope()
    var userInput by remember { mutableStateOf(TextFieldValue("")) }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedSql by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 缓存字符串资源，避免在 lambda 中调用 composable 函数
    val configRequiredMsg = stringResource("ai_config_required", language)
    val noResultMsg = stringResource("ai_no_result", language)
    val generateFailedMsg = stringResource("ai_generate_failed", language)

    AlertDialog(
        onDismissRequest = {
            if (!isGenerating) {
                onDismissRequest()
            }
        },
        icon = {
            Icon(
                Icons.Filled.SmartToy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource("ai_generate", language),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 数据库信息提示
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    variant = CardVariant.Surface
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "数据库类型：${databaseType.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (tableSchema != null) {
                            Text(
                                text = "表结构：${tableSchema.take(100)}${if (tableSchema.length > 100) "..." else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 用户输入
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    label = { Text(stringResource("ai_user_input", language)) },
                    placeholder = { Text(stringResource("ai_user_input_hint", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isGenerating
                )

                // 生成结果
                if (generatedSql != null) {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = CardVariant.Default
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "生成的 SQL:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(onClick = { onInsertSql(generatedSql!!) }) {
                                    Icon(
                                        Icons.Filled.ContentPasteGo,
                                        contentDescription = stringResource("ai_insert", language),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = generatedSql!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                // 错误信息
                if (errorMessage != null) {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        variant = CardVariant.Default
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // 生成中
                if (isGenerating) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource("ai_generating", language),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (generatedSql != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            generatedSql = null
                            errorMessage = null
                        }
                    ) {
                        Text(stringResource("ai_retry", language))
                    }
                    Button(
                        onClick = { onInsertSql(generatedSql!!) }
                    ) {
                        Icon(
                            Icons.Filled.ContentPasteGo,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource("ai_insert", language))
                    }
                }
            } else {
                Button(
                    onClick = {
                        if (userInput.text.isBlank()) return@Button

                        isGenerating = true
                        errorMessage = null

                        scope.launch {
                            val config = aiConfigStorage?.loadConfig()
                            if (config == null || config.apiKey.isBlank()) {
                                errorMessage = configRequiredMsg
                                isGenerating = false
                                return@launch
                            }

                            runCatching {
                                callAiApi(
                                    config = config,
                                    databaseType = databaseType,
                                    tableSchema = tableSchema,
                                    userInput = userInput.text,
                                    language = language
                                )
                            }.onSuccess { result ->
                                if (result != null) {
                                    generatedSql = result.trim()
                                } else {
                                    errorMessage = noResultMsg
                                }
                            }.onFailure { e ->
                                errorMessage = "$generateFailedMsg: ${e.message}"
                            }

                            isGenerating = false
                        }
                    },
                    enabled = userInput.text.isNotBlank() && !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Filled.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource("ai_generate", language))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                enabled = !isGenerating
            ) {
                Text(stringResource("ai_close", language))
            }
        }
    )
}

/**
 * 调用 AI API 生成 SQL
 * 使用 Ktor HTTP 客户端
 */
private suspend fun callAiApi(
    config: AiConfig,
    databaseType: DatabaseType,
    tableSchema: String?,
    userInput: String,
    language: Language
): String? {
    val prompt = config.getPrompt(databaseType, tableSchema, userInput, language)

    val baseUrl = config.baseUrl.trimEnd('/')
    val url = when (config.apiType) {
        ApiType.OPENAI -> "$baseUrl/chat/completions"
        ApiType.CLAUDE -> "$baseUrl/messages"
    }

    // 构建请求 JSON（使用字符串拼接避免序列化问题）
    val requestJson = when (config.apiType) {
        ApiType.OPENAI -> """
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {
                        "role": "user",
                        "content": ${escapeJson(prompt)}
                    }
                ],
                "temperature": 0.3,
                "max_tokens": 1000
            }
        """.trimIndent()
        ApiType.CLAUDE -> """
            {
                "model": "claude-3-haiku-20240307",
                "messages": [
                    {
                        "role": "user",
                        "content": ${escapeJson(prompt)}
                    }
                ],
                "max_tokens": 1000
            }
        """.trimIndent()
    }

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        expectSuccess = false
    }

    try {
        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${config.apiKey}")
            setBody(requestJson)
        }

        if (response.status.value != 200) {
            val errorBody = response.body<String>()
            throw Exception("API 返回错误：${response.status} - $errorBody")
        }

        val responseJson = response.body<String>()
        return parseAiResponse(responseJson, config.apiType)
    } finally {
        client.close()
    }
}

/**
 * 转义 JSON 字符串
 */
private fun escapeJson(str: String): String {
    return str.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

/**
 * 解析 AI 响应
 */
private fun parseAiResponse(responseJson: String, apiType: ApiType): String? {
    val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    return try {
        when (apiType) {
            ApiType.OPENAI -> {
                // OpenAI 格式：{"choices": [{"message": {"content": "..."}}]}
                val jsonObject = json.parseToJsonElement(responseJson).jsonObject
                val choices = jsonObject["choices"]?.jsonArray
                choices?.getOrNull(0)?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content
            }
            ApiType.CLAUDE -> {
                // Claude 格式：{"content": [{"type": "text", "text": "..."}]}
                val jsonObject = json.parseToJsonElement(responseJson).jsonObject
                val content = jsonObject["content"]?.jsonArray
                content?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
            }
        }
    } catch (e: Exception) {
        null
    }
}
