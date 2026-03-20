package java.lang.management

/**
 * 运行时 MBean 接口 - Android 兼容实现
 * PostgreSQL JDBC 驱动需要此接口
 */
interface RuntimeMXBean {
    /**
     * 获取 JVM 启动以来的时间（毫秒）
     */
    fun uptime(): Long

    /**
     * 获取 JVM 名称
     */
    fun name(): String

    /**
     * 获取输入参数列表
     */
    fun inputArguments(): MutableList<String>

    /**
     * 获取类路径
     */
    fun classPath(): String

    /**
     * 获取系统属性
     */
    fun systemProperties(): MutableMap<String, String>

    /**
     * 获取 VM 名称
     */
    fun vmName(): String

    /**
     * 获取 VM 版本
     */
    fun vmVersion(): String

    /**
     * 获取 VM 供应商
     */
    fun vmVendor(): String

    /**
     * 获取规范名称
     */
    fun specName(): String

    /**
     * 获取规范版本
     */
    fun specVersion(): String

    /**
     * 获取规范供应商
     */
    fun specVendor(): String

    /**
     * 获取管理规范化名称
     */
    fun managementSpecVersion(): String

    /**
     * 获取系统启动时间
     */
    fun startTime(): Long

    /**
     * 获取总编译时间
     */
    fun totalCpuTime(): Long
}
