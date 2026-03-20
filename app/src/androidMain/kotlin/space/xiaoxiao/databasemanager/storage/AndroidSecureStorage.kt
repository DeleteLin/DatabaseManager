package space.xiaoxiao.databasemanager.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Android 平台安全存储实现
 * 使用 EncryptedSharedPreferences 进行加密存储
 */
class AndroidSecureStorage(context: Context) : SecureStorage {
    private val prefs: SharedPreferences

    init {
        // 创建主密钥
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // 创建加密的 SharedPreferences
        prefs = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun setString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    override fun getAllKeys(): Set<String> {
        return prefs.all.keys
    }
}

/**
 * Android 平台加密管理器实现
 * 使用 EncryptedSharedPreferences 内部加密，这里只做简单处理
 */
class AndroidEncryptionManager : EncryptionManager {
    override fun encrypt(plaintext: String): String {
        // 在 EncryptedSharedPreferences 中，值会自动加密存储
        // 这里我们直接返回原文，因为存储层已经加密
        return plaintext
    }

    override fun decrypt(ciphertext: String): String {
        // 存储层会自动解密，这里直接返回
        return ciphertext
    }

    override fun isEncryptionAvailable(): Boolean = true

    override fun isSecureEncryption(): Boolean = true
}

/**
 * Base64 编码实现（Android）
 */
actual fun String.encodeBase64(): String {
    return android.util.Base64.encodeToString(toByteArray(), android.util.Base64.NO_WRAP)
}

actual fun String.decodeBase64(): String {
    return String(android.util.Base64.decode(this, android.util.Base64.NO_WRAP))
}
