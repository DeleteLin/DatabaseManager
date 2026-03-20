package space.xiaoxiao.databasemanager.utils

/**
 * JVM/Desktop 平台检测实现
 */
actual object Platform {
    actual val isJvm: Boolean = true
    actual val isAndroid: Boolean = false
}