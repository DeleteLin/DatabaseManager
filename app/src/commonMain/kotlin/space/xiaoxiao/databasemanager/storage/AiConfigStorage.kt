package space.xiaoxiao.databasemanager.storage

import space.xiaoxiao.databasemanager.core.DatabaseType

/**
 * AI 配置存储
 * 用于存储和加载 AI 接口配置
 */
class AiConfigStorage(
    private val secureStorage: SecureStorage,
    private val encryptionManager: EncryptionManager
) {
    companion object {
        private const val KEY_AI_CONFIG = "ai_config"
    }

    /**
     * 加载 AI 配置
     */
    fun loadConfig(): AiConfig? {
        val json = secureStorage.getString(KEY_AI_CONFIG) ?: return null
        return try {
            ConfigSerializer.json.decodeFromString(AiConfig.serializer(), json)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 保存 AI 配置
     */
    fun saveConfig(config: AiConfig) {
        val json = ConfigSerializer.json.encodeToString(AiConfig.serializer(), config)
        secureStorage.setString(KEY_AI_CONFIG, json)
    }

    /**
     * 删除 AI 配置
     */
    fun deleteConfig() {
        secureStorage.remove(KEY_AI_CONFIG)
    }
}

/**
 * 扩展函数：根据数据库类型获取提示词
 */
fun AiConfig.getPrompt(
    databaseType: DatabaseType,
    tableSchema: String?,
    userInput: String,
    language: space.xiaoxiao.databasemanager.i18n.Language
): String {
    val defaultPrompt = if (language == space.xiaoxiao.databasemanager.i18n.Language.CHINESE) {
        customPromptZh
    } else {
        customPromptEn
    }

    val dbTypeStr = when (databaseType) {
        DatabaseType.MYSQL -> "MySQL"
        DatabaseType.POSTGRESQL -> "PostgreSQL"
        DatabaseType.REDIS -> "Redis"
    }

    return defaultPrompt
        .replace("{dbType}", dbTypeStr)
        .replace("{tableSchema}", tableSchema ?: "无表结构信息")
        .replace("{userInput}", userInput)
}
