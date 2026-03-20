package space.xiaoxiao.databasemanager.backup

import space.xiaoxiao.databasemanager.storage.ConfigSerializer

internal object BackupCryptoCommon {
    const val SALT_BYTES = 16
    const val IV_BYTES = 12
    const val KEY_BITS = 256
    const val TAG_BITS = 128

    fun encodeFile(file: EncryptedBackupFile): String =
        ConfigSerializer.json.encodeToString(EncryptedBackupFile.serializer(), file)

    fun decodeFile(fileJson: String): EncryptedBackupFile =
        ConfigSerializer.json.decodeFromString(EncryptedBackupFile.serializer(), fileJson)
}

