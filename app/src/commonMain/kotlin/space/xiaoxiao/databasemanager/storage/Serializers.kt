package space.xiaoxiao.databasemanager.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 用于存储字符串列表的序列化器
 */
class StringListSerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor = ListSerializer(String.serializer()).descriptor

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.encodeString(value.joinToString("\n"))
    }

    override fun deserialize(decoder: Decoder): List<String> {
        return decoder.decodeString().split("\n").filter { it.isNotEmpty() }
    }
}
