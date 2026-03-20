package java.sql

/**
 * SQLType - Android 兼容实现
 * MySQL Connector-J 需要此接口
 */
interface SQLType {
    fun getName(): String
    fun getVendor(): Int
    fun getVendorTypeIfNeeded(): Int?
}
