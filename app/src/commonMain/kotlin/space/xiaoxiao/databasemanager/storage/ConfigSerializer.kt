package space.xiaoxiao.databasemanager.storage

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * JSON 序列化器
 * 使用 kotlinx.serialization 进行 JSON 序列化和反序列化
 */
object ConfigSerializer {
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * 序列化数据库配置列表
     */
    fun serializeDatabaseConfigs(configs: List<SerializableDatabaseConfig>): String {
        return json.encodeToString(ListSerializer(SerializableDatabaseConfig.serializer()), configs)
    }

    /**
     * 反序列化数据库配置列表
     */
    fun deserializeDatabaseConfigs(jsonStr: String): List<SerializableDatabaseConfig> {
        return if (jsonStr.isBlank()) emptyList()
        else json.decodeFromString(ListSerializer(SerializableDatabaseConfig.serializer()), jsonStr)
    }

    /**
     * 序列化查询历史列表
     */
    fun serializeQueryHistory(items: List<SerializableQueryHistoryItem>): String {
        return json.encodeToString(ListSerializer(SerializableQueryHistoryItem.serializer()), items)
    }

    /**
     * 反序列化查询历史列表
     */
    fun deserializeQueryHistory(jsonStr: String): List<SerializableQueryHistoryItem> {
        return if (jsonStr.isBlank()) emptyList()
        else json.decodeFromString(ListSerializer(SerializableQueryHistoryItem.serializer()), jsonStr)
    }

    /**
     * 序列化应用配置
     */
    fun serializeAppConfig(config: SerializableAppConfig): String {
        return json.encodeToString(SerializableAppConfig.serializer(), config)
    }

    /**
     * 反序列化应用配置
     */
    fun deserializeAppConfig(jsonStr: String): SerializableAppConfig {
        return if (jsonStr.isBlank()) SerializableAppConfig()
        else json.decodeFromString(SerializableAppConfig.serializer(), jsonStr)
    }
}
