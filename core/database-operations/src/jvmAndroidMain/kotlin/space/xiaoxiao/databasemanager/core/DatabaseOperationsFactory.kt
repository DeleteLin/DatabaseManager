package space.xiaoxiao.databasemanager.core

/**
 * 注册当前平台（JVM/Android）支持的数据库驱动
 * 应用启动时调用，iOS 平台可在 iosMain 中注册自己的驱动
 */
fun registerPlatformDrivers() {
    DriverRegistry.register(MysqlJdbcDriver)
    DriverRegistry.register(PostgresJdbcDriver)
    DriverRegistry.register(RedisDriver)
}
