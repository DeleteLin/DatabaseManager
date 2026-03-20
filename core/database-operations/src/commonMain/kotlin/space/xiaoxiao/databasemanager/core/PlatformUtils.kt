package space.xiaoxiao.databasemanager.core

/**
 * 获取 JVM/应用运行时间（毫秒）
 * 平台特定实现
 */
expect fun getUptimeMillis(): Long
