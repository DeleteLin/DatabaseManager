package space.xiaoxiao.databasemanager.core

/**
 * 数据库驱动能力声明
 * 由每个数据库驱动声明其支持的功能集合，UI 层据此自适应
 */
data class DatabaseCapabilities(
    val supportsTransactions: Boolean = false,
    val supportsIndexes: Boolean = false,
    val supportsForeignKeys: Boolean = false,
    val supportsStoredProcedures: Boolean = false,
    val supportsViews: Boolean = false,
    val supportsTriggers: Boolean = false,
    val supportsSchemas: Boolean = false,
    val supportsMultipleDatabases: Boolean = false,
    val supportsTableCreation: Boolean = false,
    val supportsTableAlteration: Boolean = false,
    val supportedFeatures: Set<DatabaseFeature> = emptySet()
)

/**
 * 数据库驱动接口
 * 每个数据库类型实现此接口，注册到 DriverRegistry 即可扩展
 * iOS 未来可实现此接口，无需修改 commonMain
 */
interface DatabaseDriver {
    val driverId: String
    val databaseType: DatabaseType
    val capabilities: DatabaseCapabilities

    suspend fun createClient(config: DatabaseConfig): DatabaseClient
}

/**
 * 数据库驱动注册中心
 * 驱动在应用启动时注册，createDatabaseClient() 通过注册中心查找对应驱动
 */
object DriverRegistry {
    private val drivers = mutableMapOf<DatabaseType, DatabaseDriver>()

    fun register(driver: DatabaseDriver) {
        drivers[driver.databaseType] = driver
    }

    fun get(type: DatabaseType): DatabaseDriver? = drivers[type]

    fun listAll(): List<DatabaseDriver> = drivers.values.toList()
}

/**
 * 创建数据库客户端
 * 通过 DriverRegistry 查找注册的驱动，由驱动负责组装 DatabaseClient
 */
suspend fun createDatabaseClient(config: DatabaseConfig): DatabaseClient {
    return DriverRegistry.get(config.type)
        ?.createClient(config)
        ?: throw IllegalStateException("未注册的数据库类型: ${config.type}")
}
