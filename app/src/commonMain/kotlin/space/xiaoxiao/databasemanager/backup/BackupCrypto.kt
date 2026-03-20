package space.xiaoxiao.databasemanager.backup

/**
 * 备份文件的口令加密（跨平台一致）
 *
 * - KDF: PBKDF2-HMAC-SHA256
 * - Cipher: AES-256-GCM
 * - iterations 默认 300000（更安全）
 */
expect object BackupCrypto {
    fun encryptToFileJson(plaintextJson: String, password: String, iterations: Int = 300_000): String
    fun decryptFromFileJson(fileJson: String, password: String): String
}

