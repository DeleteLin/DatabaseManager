package java.lang.management

/**
 * MemoryMXBean 接口 - Android 兼容实现
 */
interface MemoryMXBean {
    fun getHeapMemoryUsage(): MemoryUsage?
    fun getNonHeapMemoryUsage(): MemoryUsage?
    fun isVerbose(): Boolean
    fun setVerbose(value: Boolean)
    fun gc()
    fun getObjectPendingFinalizationCount(): Long
}
