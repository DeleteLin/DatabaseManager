package space.xiaoxiao.databasemanager.utils

/**
 * 平台检测工具
 */
expect object Platform {
    val isJvm: Boolean
    val isAndroid: Boolean
}