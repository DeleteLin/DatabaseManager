package java.lang.management

/**
 * MemoryUsage - Android 兼容实现
 */
class MemoryUsage(
    private val init: Long,
    private val used: Long,
    private val committed: Long,
    private val max: Long
) {
    fun getInit(): Long = init
    fun getUsed(): Long = used
    fun getCommitted(): Long = committed
    fun getMax(): Long = max

    companion object {
        fun from(builder: Builder): MemoryUsage {
            return MemoryUsage(builder.init, builder.used, builder.committed, builder.max)
        }
    }

    class Builder {
        var init: Long = -1
        var used: Long = -1
        var committed: Long = -1
        var max: Long = -1
    }
}
