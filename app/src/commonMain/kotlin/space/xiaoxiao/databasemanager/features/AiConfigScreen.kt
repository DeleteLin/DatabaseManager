package space.xiaoxiao.databasemanager.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.components.AppCard
import space.xiaoxiao.databasemanager.components.CardVariant
import space.xiaoxiao.databasemanager.i18n.Language
import space.xiaoxiao.databasemanager.i18n.stringResource
import space.xiaoxiao.databasemanager.storage.*

/**
 * AI 配置页面 - 独立页面，位于"更多" > "AI 配置"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfigScreen(
    language: Language = Language.CHINESE,
    aiConfigStorage: AiConfigStorage,
    onNavigateBack: () -> Unit
) {
    var apiType by remember { mutableStateOf(ApiType.OPENAI) }
    var baseUrl by remember { mutableStateOf(TextFieldValue("")) }
    var apiKey by remember { mutableStateOf(TextFieldValue("")) }
    var customPromptZh by remember { mutableStateOf(TextFieldValue(AiConfig.DEFAULT_AI_PROMPT_ZH)) }
    var customPromptEn by remember { mutableStateOf(TextFieldValue(AiConfig.DEFAULT_AI_PROMPT_EN)) }
    var showPassword by remember { mutableStateOf(false) }
    var showPromptEditor by remember { mutableStateOf(false) }
    var showSaveSuccess by remember { mutableStateOf(false) }

    // 加载已有配置
    LaunchedEffect(Unit) {
        val savedConfig = aiConfigStorage.loadConfig()
        savedConfig?.let { config ->
            apiType = config.apiType
            baseUrl = TextFieldValue(config.baseUrl)
            apiKey = TextFieldValue(config.apiKey)
            customPromptZh = TextFieldValue(config.customPromptZh)
            customPromptEn = TextFieldValue(config.customPromptEn)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource("ai_config_menu", language)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            aiConfigStorage.saveConfig(
                                AiConfig(
                                    apiType = apiType,
                                    baseUrl = baseUrl.text,
                                    apiKey = apiKey.text,
                                    customPromptZh = customPromptZh.text,
                                    customPromptEn = customPromptEn.text
                                )
                            )
                            showSaveSuccess = true
                        },
                        enabled = baseUrl.text.isNotBlank() && apiKey.text.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
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
            // API 类型选择
            Column {
                Text(
                    text = stringResource("ai_api_type", language),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = apiType == ApiType.OPENAI,
                        onClick = { apiType = ApiType.OPENAI },
                        label = { Text(stringResource("ai_api_type_openai", language)) },
                        leadingIcon = if (apiType == ApiType.OPENAI) {
                            { Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                    FilterChip(
                        selected = apiType == ApiType.CLAUDE,
                        onClick = { apiType = ApiType.CLAUDE },
                        label = { Text(stringResource("ai_api_type_claude", language)) },
                        leadingIcon = if (apiType == ApiType.CLAUDE) {
                            { Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            // Base URL
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text(stringResource("ai_base_url", language)) },
                placeholder = { Text(stringResource("ai_base_url_hint", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Link, contentDescription = null)
                }
            )

            // API Key
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text(stringResource("ai_api_key", language)) },
                placeholder = { Text(stringResource("ai_api_key_hint", language)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                leadingIcon = {
                    Icon(Icons.Filled.VpnKey, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            // 提示词编辑（可折叠）
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
                            text = stringResource("ai_prompt", language),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { showPromptEditor = !showPromptEditor }
                        ) {
                            Text(
                                if (showPromptEditor) "收起" else "编辑",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    if (showPromptEditor) {
                        // 中文提示词
                        OutlinedTextField(
                            value = customPromptZh,
                            onValueChange = { customPromptZh = it },
                            label = { Text("中文提示词") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 6,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        // 英文提示词
                        OutlinedTextField(
                            value = customPromptEn,
                            onValueChange = { customPromptEn = it },
                            label = { Text("English Prompt") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 6,
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        // 重置按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    customPromptZh = TextFieldValue(AiConfig.DEFAULT_AI_PROMPT_ZH)
                                    customPromptEn = TextFieldValue(AiConfig.DEFAULT_AI_PROMPT_EN)
                                }
                            ) {
                                Text(stringResource("ai_reset_prompt", language))
                            }
                        }
                    } else {
                        // 显示预览
                        Text(
                            text = stringResource("ai_prompt_hint", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 保存成功提示
            if (showSaveSuccess) {
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
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = stringResource("ai_config_saved", language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}
