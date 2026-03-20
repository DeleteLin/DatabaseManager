package space.xiaoxiao.databasemanager.storage

import java.io.File
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.util.Base64

/**
 * JVM 平台安全存储实现
 * 使用 AES-256-GCM 加密存储敏感数据
 */
class JvmSecureStorage : SecureStorage {
    private val prefs = Preferences.userNodeForPackage(JvmSecureStorage::class.java)
    private val encryptionManager = JvmEncryptionManager()

    // 存储目录（用于非敏感数据）
    private val storageDir: File by lazy {
        val userHome = System.getProperty("user.home")
        val appDir = File(userHome, ".database-manager")
        appDir.mkdirs()
        appDir
    }

    override fun getString(key: String): String? {
        return prefs.get(key, null)
    }

    override fun setString(key: String, value: String) {
        prefs.put(key, value)
    }

    override fun remove(key: String) {
        prefs.remove(key)
    }

    override fun clear() {
        prefs.clear()
    }

    override fun getAllKeys(): Set<String> {
        return prefs.keys().toSet()
    }

    /**
     * 读取 JSON 文件
     */
    fun readJsonFile(fileName: String): String? {
        val file = File(storageDir, fileName)
        return if (file.exists()) {
            // 读取加密的文件内容并解密
            val encryptedContent = file.readText()
            try {
                encryptionManager.decrypt(encryptedContent)
            } catch (e: Exception) {
                // 如果解密失败，尝试直接读取（兼容旧数据）
                encryptedContent
            }
        } else null
    }

    /**
     * 写入 JSON 文件（加密）
     */
    fun writeJsonFile(fileName: String, content: String) {
        val file = File(storageDir, fileName)
        // 加密后写入
        val encryptedContent = encryptionManager.encrypt(content)
        file.writeText(encryptedContent)
    }

    /**
     * 删除 JSON 文件
     */
    fun deleteJsonFile(fileName: String) {
        val file = File(storageDir, fileName)
        if (file.exists()) {
            file.delete()
        }
    }
}

/**
 * JVM 平台加密管理器
 * 使用 AES-256-GCM 加密
 */
class JvmEncryptionManager : EncryptionManager {
    private companion object {
        const val ALGORITHM = "AES/GCM/NoPadding"
        const val KEY_SIZE = 256
        const val GCM_IV_LENGTH = 12
        const val GCM_TAG_LENGTH = 128
        const val PREF_KEY_SECRET = "secure_storage_secret"
    }

    private val prefs = Preferences.userNodeForPackage(JvmEncryptionManager::class.java)
    private val secretKey: SecretKey by lazy { getOrCreateSecretKey() }

    private fun getOrCreateSecretKey(): SecretKey {
        val encodedKey = prefs.getByteArray(PREF_KEY_SECRET, null)
        return if (encodedKey != null) {
            SecretKeySpec(encodedKey, 0, encodedKey.size, "AES")
        } else {
            // 生成新密钥
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(KEY_SIZE)
            val key = keyGenerator.generateKey()
            prefs.putByteArray(PREF_KEY_SECRET, key.encoded)
            key
        }
    }

    override fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

        val encryptedBytes = cipher.doFinal(plaintext.toByteArray())
        val ivAndCiphertext = iv + encryptedBytes

        return Base64.getEncoder().encodeToString(ivAndCiphertext)
    }

    override fun decrypt(ciphertext: String): String {
        val decodedBytes = Base64.getDecoder().decode(ciphertext)
        val iv = decodedBytes.copyOfRange(0, GCM_IV_LENGTH)
        val cipherBytes = decodedBytes.copyOfRange(GCM_IV_LENGTH, decodedBytes.size)

        val cipher = Cipher.getInstance(ALGORITHM)
        val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

        val decryptedBytes = cipher.doFinal(cipherBytes)
        return String(decryptedBytes)
    }

    override fun isEncryptionAvailable(): Boolean = true

    override fun isSecureEncryption(): Boolean = true
}

/**
 * Base64 编码实现（JVM）
 */
actual fun String.encodeBase64(): String {
    return Base64.getEncoder().encodeToString(toByteArray())
}

actual fun String.decodeBase64(): String {
    return String(Base64.getDecoder().decode(this))
}
