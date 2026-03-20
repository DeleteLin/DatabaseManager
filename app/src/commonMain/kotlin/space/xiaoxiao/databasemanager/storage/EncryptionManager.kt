package space.xiaoxiao.databasemanager.storage

/**
 * 加密管理器接口
 * 提供敏感的加密解密能力
 */
interface EncryptionManager {
    /**
     * 加密明文
     * @param plaintext 明文
     * @return 密文（Base64 编码）
     */
    fun encrypt(plaintext: String): String

    /**
     * 解密密文
     * @param ciphertext 密文（Base64 编码）
     * @return 明文
     * @throws Exception 解密失败时抛出异常
     */
    fun decrypt(ciphertext: String): String

    /**
     * 检查加密是否可用
     * @return true 如果加密可用
     */
    fun isEncryptionAvailable(): Boolean

    /**
     * 检查是否为安全加密
     * @return true 如果是真正的加密，false 如果只是编码
     */
    fun isSecureEncryption(): Boolean
}

/**
 * 默认加密管理器（无加密，用于降级）
 * 注意：这只是降级方案，应该使用平台特定的加密实现
 *
 * 安全警告：
 * - 此实现仅使用 Base64 编码，不提供任何安全性
 * - 数据以可读形式存储，任何人均可解码
 * - 不应用于存储敏感信息（如数据库密码）
 */
class NoOpEncryptionManager : EncryptionManager {
    override fun encrypt(plaintext: String): String {
        // 简单的 Base64 编码，不是真正的加密
        return plaintext.encodeBase64()
    }

    override fun decrypt(ciphertext: String): String {
        return ciphertext.decodeBase64()
    }

    override fun isEncryptionAvailable(): Boolean = false

    override fun isSecureEncryption(): Boolean = false
}

/**
 * Base64 编码工具（commonMain 实现）
 */
expect fun String.encodeBase64(): String
expect fun String.decodeBase64(): String
