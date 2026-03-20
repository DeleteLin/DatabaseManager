package space.xiaoxiao.databasemanager.core

/**
 * JVM/Android 共享实现
 * 使用反射检查 ManagementFactory 是否可用，否则使用备用方法
 */
actual fun getUptimeMillis(): Long {
    return try {
        // 尝试使用 ManagementFactory（JVM 可用）
        val mxBeanClass = Class.forName("java.lang.management.ManagementFactory")
        val runtimeMxBeanMethod = mxBeanClass.getMethod("getRuntimeMXBean")
        val runtimeMxBean = runtimeMxBeanMethod.invoke(null)
        val uptimeMethod = runtimeMxBean.javaClass.getMethod("uptime")
        uptimeMethod.invoke(runtimeMxBean) as Long
    } catch (e: Exception) {
        // Android 或 ManagementFactory 不可用时，使用系统时间
        System.currentTimeMillis()
    }
}
