package space.xiaoxiao.databasemanager.backup

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual object BackupCrypto {
    actual fun encryptToFileJson(plaintextJson: String, password: String, iterations: Int): String {
        require(password.isNotBlank()) { "密码不能为空" }
        require(iterations > 0) { "iterations 非法" }

        val salt = ByteArray(BackupCryptoCommon.SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(BackupCryptoCommon.IV_BYTES).also { SecureRandom().nextBytes(it) }

        val keyBytes = deriveKey(password, salt, iterations)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.ENCRYPT_MODE,
            SecretKeySpec(keyBytes, "AES"),
            GCMParameterSpec(BackupCryptoCommon.TAG_BITS, iv)
        )

        val ciphertext = cipher.doFinal(plaintextJson.toByteArray(Charsets.UTF_8))

        val file = EncryptedBackupFile(
            kdf = KdfParams(
                iterations = iterations,
                saltBase64 = Base64.getEncoder().encodeToString(salt)
            ),
            cipher = CipherParams(
                ivBase64 = Base64.getEncoder().encodeToString(iv),
                tagBits = BackupCryptoCommon.TAG_BITS
            ),
            ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertext)
        )

        return BackupCryptoCommon.encodeFile(file)
    }

    actual fun decryptFromFileJson(fileJson: String, password: String): String {
        require(password.isNotBlank()) { "密码不能为空" }

        val file = BackupCryptoCommon.decodeFile(fileJson)
        require(file.format == "dbmconf") { "不支持的备份格式：${file.format}" }
        require(file.version == 1) { "不支持的备份版本：${file.version}" }
        require(file.kdf.name == "PBKDF2-HMAC-SHA256") { "不支持的 KDF：${file.kdf.name}" }
        require(file.cipher.name == "AES-256-GCM") { "不支持的 cipher：${file.cipher.name}" }
        require(file.cipher.tagBits == BackupCryptoCommon.TAG_BITS) { "不支持的 tagBits：${file.cipher.tagBits}" }

        val salt = Base64.getDecoder().decode(file.kdf.saltBase64)
        val iv = Base64.getDecoder().decode(file.cipher.ivBase64)
        val ciphertext = Base64.getDecoder().decode(file.ciphertextBase64)

        val keyBytes = deriveKey(password, salt, file.kdf.iterations)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(keyBytes, "AES"),
            GCMParameterSpec(BackupCryptoCommon.TAG_BITS, iv)
        )

        val plaintextBytes = cipher.doFinal(ciphertext)
        return plaintextBytes.toString(Charsets.UTF_8)
    }

    private fun deriveKey(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, BackupCryptoCommon.KEY_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}

