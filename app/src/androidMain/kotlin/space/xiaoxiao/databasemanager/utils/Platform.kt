package space.xiaoxiao.databasemanager.utils

/**
 * Android 平台检测实现
 */
actual object Platform {
    actual val isJvm: Boolean = false
    actual val isAndroid: Boolean = true
}