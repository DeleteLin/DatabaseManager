package java.lang.management

/**
 * ManagementFactory - Android 兼容实现
 *
 * PostgreSQL JDBC 驱动通过反射调用 ManagementFactory.getRuntimeMXBean().uptime()
 * 此实现提供兼容的接口以避免 NoClassDefFoundError
 */
object ManagementFactory {

    private val startTime = System.currentTimeMillis()
    private var runtimeMXBean: RuntimeMXBean? = null

    /**
     * 获取 RuntimeMXBean 实例
     */
    @JvmStatic
    fun getRuntimeMXBean(): RuntimeMXBean {
        return runtimeMXBean ?: AndroidRuntimeMXBean().also { runtimeMXBean = it }
    }

    /**
     * Android 平台 RuntimeMXBean 实现
     */
    private class AndroidRuntimeMXBean : RuntimeMXBean {

        override fun uptime(): Long {
            return System.currentTimeMillis() - startTime
        }

        override fun name(): String {
            return "${System.getProperty("java.vm.name", "Dalvik")}@${hashCode()}"
        }

        override fun inputArguments(): MutableList<String> {
            return mutableListOf()
        }

        override fun classPath(): String {
            return System.getProperty("java.class.path", "")
        }

        override fun systemProperties(): MutableMap<String, String> {
            return System.getProperties().mapKeys { it.key.toString() }
                .mapValues { it.value?.toString() ?: "" }
                .toMutableMap()
        }

        override fun vmName(): String {
            return System.getProperty("java.vm.name", "Dalvik VM")
        }

        override fun vmVersion(): String {
            return System.getProperty("java.vm.version", System.getProperty("os.version", ""))
        }

        override fun vmVendor(): String {
            return System.getProperty("java.vm.vendor", "Android")
        }

        override fun specName(): String {
            return System.getProperty("java.vm.specification.name", "Java Virtual Machine Specification")
        }

        override fun specVersion(): String {
            return System.getProperty("java.vm.specification.version", "17")
        }

        override fun specVendor(): String {
            return System.getProperty("java.vm.specification.vendor", "Oracle Corporation")
        }

        override fun managementSpecVersion(): String {
            return "1.2"
        }

        override fun startTime(): Long {
            return startTime
        }

        override fun totalCpuTime(): Long {
            return -1 // 不支持
        }
    }

    // ==================== 其他可能被调用的方法 ====================

    private var memoryMXBean: MemoryMXBean? = null

    @JvmStatic
    fun getMemoryMXBean(): MemoryMXBean {
        return memoryMXBean ?: AndroidMemoryMXBean().also { memoryMXBean = it }
    }

    @JvmStatic
    fun getThreadMXBean(): Any {
        throw UnsupportedOperationException("ThreadMXBean not supported on Android")
    }

    @JvmStatic
    fun getOperatingSystemMXBean(): Any {
        return AndroidOperatingSystemMXBean()
    }

    @JvmStatic
    fun getCompilationMXBean(): Any {
        throw UnsupportedOperationException("CompilationMXBean not supported on Android")
    }

    @JvmStatic
    fun getClassLoadingMXBean(): Any {
        throw UnsupportedOperationException("ClassLoadingMXBean not supported on Android")
    }

    @JvmStatic
    fun getGarbageCollectorMXBeans(): List<Any> {
        return emptyList()
    }

    @JvmStatic
    fun getMemoryManagerMXBeans(): List<Any> {
        return emptyList()
    }

    @JvmStatic
    fun getMemoryPoolMXBeans(): List<Any> {
        return emptyList()
    }

    /**
     * Android MemoryMXBean 实现
     */
    private class AndroidMemoryMXBean : MemoryMXBean {
        override fun getHeapMemoryUsage(): MemoryUsage? {
            val runtime = Runtime.getRuntime()
            return MemoryUsage(
                runtime.maxMemory() / 4, // 初始堆大小
                runtime.totalMemory() - runtime.freeMemory(), // 已使用
                runtime.totalMemory(), // 已提交
                runtime.maxMemory() // 最大堆大小
            )
        }

        override fun getNonHeapMemoryUsage(): MemoryUsage {
            return MemoryUsage(-1, -1, -1, -1) // 不支持
        }

        override fun isVerbose(): Boolean = false

        override fun setVerbose(value: Boolean) { }

        override fun gc() {
            Runtime.getRuntime().gc()
        }

        override fun getObjectPendingFinalizationCount(): Long = 0
    }

    /**
     * Android OperatingSystemMXBean 实现
     */
    private class AndroidOperatingSystemMXBean {
        fun getName(): String = System.getProperty("os.name", "Linux")
        fun getArch(): String = System.getProperty("os.arch", "aarch64")
        fun getVersion(): String = System.getProperty("os.version", "")
    }
}
