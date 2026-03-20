package space.xiaoxiao.databasemanager.storage

/**
 * 安全存储接口
 * 提供统一的加密存储抽象，屏蔽平台差异
 */
interface SecureStorage {
    /**
     * 获取字符串值
     * @param key 键
     * @return 值，不存在返回 null
     */
    fun getString(key: String): String?

    /**
     * 设置字符串值
     * @param key 键
     * @param value 值
     */
    fun setString(key: String, value: String)

    /**
     * 删除键值对
     * @param key 键
     */
    fun remove(key: String)

    /**
     * 清空所有存储
     */
    fun clear()

    /**
     * 获取所有键
     */
    fun getAllKeys(): Set<String>
}
