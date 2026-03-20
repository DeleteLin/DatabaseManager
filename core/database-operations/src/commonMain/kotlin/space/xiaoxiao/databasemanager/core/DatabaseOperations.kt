package space.xiaoxiao.databasemanager.core

/**
 * 数据库连接配置
 */
data class DatabaseConfig(
    val type: DatabaseType,
    val host: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val charset: String? = null,  // 字符集（MySQL/PostgreSQL）
    val schema: String? = null,  // PostgreSQL schema (默认 public)
    val extra: Map<String, String> = emptyMap()
)

/**
 * 数据库类型
 */
enum class DatabaseType {
    MYSQL,
    POSTGRESQL,
    REDIS
}

/**
 * 连接状态
 */
sealed class ConnectionStatus {
    object Connected : ConnectionStatus()
    object Disconnected : ConnectionStatus()
    data class Error(val message: String, val exception: Throwable? = null) : ConnectionStatus()
}

/**
 * 查询结果
 */
data class QueryResult(
    val columns: List<Column>,
    val rows: List<Row>,
    val rowCount: Int,
    val executionTimeMs: Long
)

/**
 * 列信息
 */
data class Column(
    val name: String,
    val typeName: String,
    val isNullable: Boolean = true
)

/**
 * 行数据
 */
data class Row(
    val values: List<Any?>
)

/**
 * 更新结果
 */
data class UpdateResult(
    val affectedRows: Int,
    val executionTimeMs: Long
)

/**
 * 执行结果 - 统一表示命令的执行结果
 */
sealed class ExecutionResult {
    /**
     * 查询命令（SELECT / GET 等）的结果
     */
    data class Query(val result: QueryResult) : ExecutionResult()
    /**
     * 更新命令（INSERT/UPDATE/DELETE / SET/DEL 等）或 DDL（CREATE/DROP/ALTER）的结果
     */
    data class Update(val result: UpdateResult) : ExecutionResult()
}

/**
 * 表信息
 */
data class TableInfo(
    val name: String,
    val schema: String? = null,
    val type: String = "TABLE"
)

/**
 * 表结构
 */
data class TableSchema(
    val tableName: String,
    val columns: List<ColumnDefinition>,
    val primaryKeys: List<String> = emptyList(),
    val foreignKeys: List<ForeignKey> = emptyList(),
    val indexes: List<Index> = emptyList()
)

/**
 * 列定义
 */
data class ColumnDefinition(
    val name: String,
    val typeName: String,
    val isNullable: Boolean = true,
    val isPrimaryKey: Boolean = false,
    val isAutoIncrement: Boolean = false,
    val defaultValue: String? = null,
    val comment: String? = null,
    val charset: String? = null  // 字符集（仅文本类型：VARCHAR, CHAR, TEXT 等）
)

/**
 * 外键
 */
data class ForeignKey(
    val columnName: String,
    val referencedTable: String,
    val referencedColumn: String
)

/**
 * 索引
 */
data class Index(
    val name: String,
    val columns: List<String>,
    val isUnique: Boolean = false
)

/**
 * 表统计信息
 */
data class TableStats(
    val tableName: String,
    val rowCount: Long,
    val dataSize: Long,      // 数据大小(字节)
    val indexSize: Long,     // 索引大小(字节)
    val autoIncrementValue: Long?,
    val createTime: String?,
    val updateTime: String?
)

/**
 * 索引详细信息
 */
data class IndexInfo(
    val name: String,
    val tableName: String,
    val columns: List<String>,
    val isUnique: Boolean,
    val isPrimary: Boolean,
    val type: String         // BTREE, HASH, etc.
)

/**
 * 字段修改定义
 */
data class ColumnModification(
    val oldName: String?,    // 修改字段时的原名称，null表示新增
    val newName: String,
    val typeName: String,
    val isNullable: Boolean,
    val defaultValue: String?,
    val comment: String?,
    val charset: String? = null  // 字符集（仅文本类型：VARCHAR, CHAR, TEXT 等）
)

/**
 * 表创建定义
 */
data class TableDefinition(
    val name: String,
    val columns: List<ColumnDefinition>,
    val primaryKeys: List<String>,
    val ifNotExists: Boolean = true
)

/**
 * 无状态通用命令执行器
 *
 * 不再负责连接生命周期，只依赖传入的 DbExecutionContext：
 * - 上层（或工厂）保证 context 已经 connect()
 * - 执行器只负责「给定上下文 + 执行通道」如何发起命令并解析结果
 */
interface CommandExecutor {
    /** 该执行器面向的数据库类型（MYSQL / POSTGRESQL / REDIS 等） */
    val databaseType: DatabaseType

    // ==================== 通用命令执行 ====================
    /** 执行查询命令（关系型 SQL SELECT / Redis GET 等），返回结构化结果（小结果集） */
    suspend fun executeQuery(ctx: DbExecutionContext, channel: DbExecutionChannel): Result<QueryResult>

    /** 执行更新命令（关系型 DML / Redis SET/DEL 等），返回受影响信息 */
    suspend fun executeUpdate(ctx: DbExecutionContext, channel: DbExecutionChannel): Result<UpdateResult>

    /** 执行任意命令，自动判断是查询还是更新 */
    suspend fun execute(ctx: DbExecutionContext, channel: DbExecutionChannel): Result<ExecutionResult>

    /**
     * 打开流式游标，用于大结果集
     * - 关系型：通常基于 JDBC 游标实现
     * - Redis：可以内部一次性获取再按批次吐出，实现“伪流式”
     */
    suspend fun openCursor(ctx: DbExecutionContext, channel: DbExecutionChannel): Result<CommandCursor>
}

/**
 * 通用流式游标接口（按批次拉取数据）
 * 对上层隐藏具体数据库/驱动细节
 */
interface CommandCursor {
    /** 是否还有下一批数据 */
    suspend fun hasNext(): Boolean

    /** 获取下一批数据 */
    suspend fun nextBatch(): Result<CommandBatch>

    /** 关闭游标并释放资源 */
    suspend fun close(): Result<Unit>
}

/**
 * 一批通用查询结果
 */
data class CommandBatch(
    val columns: List<Column>,
    val rows: List<Row>
)

/**
 * 执行用途（参考 DBeaver ExecutionPurpose）
 */
enum class ExecutionPurpose {
    /** 用户发起的普通查询/更新 */
    USER,

    /** 元数据查询（表/字段/索引等结构信息） */
    META,

    /** 后台任务、统计等内部用途 */
    UTIL
}

/**
 * 一次执行请求的描述（用于在通道/上下文之间传递）
 */
data class DbExecutionChannel(
    val command: DbCommand,
    val purpose: ExecutionPurpose = ExecutionPurpose.USER,
    val timeoutMillis: Long? = null
) {
    constructor(
        sql: String,
        purpose: ExecutionPurpose = ExecutionPurpose.USER,
        timeoutMillis: Long? = null
    ) : this(
        command = DbCommand.RawSql(sql),
        purpose = purpose,
        timeoutMillis = timeoutMillis
    )

    /**
     * 兼容旧代码与日志展示的便捷访问。
     *
     * - RawSql/PreparedSql 返回 SQL 文本
     * - RedisArgv 返回将 argv 拼成的可读命令行（仅用于展示，不保证可被再次解析）
     */
    val sql: String
        get() = when (val cmd = command) {
            is DbCommand.RawSql -> cmd.sql
            is DbCommand.PreparedSql -> cmd.sql
            is DbCommand.RedisArgv -> buildString {
                append(cmd.command)
                cmd.args.forEach { arg ->
                    append(' ')
                    append(
                        when (arg) {
                            is DbArg.Literal -> arg.value
                            is DbArg.Param -> arg.value.toDisplayString()
                        }
                    )
                }
            }
        }
}

private fun DbParam.toDisplayString(): String = when (this) {
    DbParam.Null -> "NULL"
    is DbParam.Str -> value
    is DbParam.Int32 -> value.toString()
    is DbParam.Int64 -> value.toString()
    is DbParam.Float64 -> value.toString()
    is DbParam.Bool -> value.toString()
    is DbParam.Bytes -> "<bytes:${value.size}>"
}

/**
 * 执行上下文（类似 DBeaver DBCExecutionContext）
 *
 * - 绑定一个物理连接或逻辑会话
 * - 负责连接生命周期（connect/disconnect/isConnected）
 * - 记录当前 database/schema 等上下文信息
 *
 * 实际执行由无状态 CommandExecutor 完成。
 */
interface DbExecutionContext {
    val databaseType: DatabaseType
    val config: DatabaseConfig

    /** 当前 schema（对于 PostgreSQL 等）或 catalog（对于 MySQL 等），由上层按需维护 */
    var currentSchema: String?

    /** 当前逻辑 database（如 MySQL 的 database，PostgreSQL 的 database 名） */
    var currentDatabase: String?

    // ==================== 连接生命周期 ====================
    suspend fun connect(): ConnectionStatus
    suspend fun disconnect()
    fun isConnected(): Boolean
}

/**
 * 关系型数据库能力接口
 *
 * 覆盖：
 * - 表 / 字段 / 索引 / 事务 / 统计 / 数据库管理 等典型关系型能力
 *
 * 未来如需接入其他关系型（例如 SQLServer、Oracle），可实现此接口
 */
interface RelationalMetadataOperations {
    // ==================== 表结构操作 ====================
    suspend fun listTables(schema: String? = null): Result<List<TableInfo>>
    suspend fun getTableSchema(tableName: String, schema: String? = null): Result<TableSchema>

    // ==================== 数据/统计 ====================
    suspend fun getTableData(
        tableName: String,
        schema: String? = null,
        limit: Int = 100,
        offset: Int = 0
    ): Result<QueryResult>
    suspend fun getTableStats(tableName: String, schema: String? = null): Result<TableStats>
    suspend fun getDatabaseSize(): Result<Long>

    // ==================== 事务管理 ====================
    suspend fun beginTransaction(): Result<Unit>
    suspend fun commitTransaction(): Result<Unit>
    suspend fun rollbackTransaction(): Result<Unit>
    suspend fun setTransactionIsolation(level: Int): Result<Unit>

    // ==================== 表管理 ====================
    suspend fun createTable(definition: TableDefinition, schema: String? = null): Result<Unit>
    suspend fun dropTable(tableName: String, schema: String? = null): Result<Unit>
    suspend fun renameTable(oldName: String, newName: String, schema: String? = null): Result<Unit>
    suspend fun truncateTable(tableName: String, schema: String? = null): Result<Unit>

    // ==================== 字段管理 ====================
    suspend fun addColumn(tableName: String, column: ColumnDefinition, schema: String? = null): Result<Unit>
    suspend fun modifyColumn(tableName: String, modification: ColumnModification, schema: String? = null): Result<Unit>
    suspend fun dropColumn(tableName: String, columnName: String, schema: String? = null): Result<Unit>

    // ==================== 索引管理 ====================
    suspend fun getIndexes(tableName: String, schema: String? = null): Result<List<IndexInfo>>
    suspend fun createIndex(
        tableName: String,
        indexName: String,
        columns: List<String>,
        isUnique: Boolean,
        schema: String? = null
    ): Result<Unit>
    suspend fun dropIndex(tableName: String, indexName: String, schema: String? = null): Result<Unit>

    // ==================== 数据库管理 ====================
    suspend fun listDatabases(): Result<List<String>>
    suspend fun switchDatabase(database: String): Result<Unit>
    suspend fun getCurrentDatabase(): Result<String>
    suspend fun createDatabase(name: String, charset: String? = null): Result<Unit>
    suspend fun dropDatabase(name: String): Result<Unit>
}

/**
 * 键值型数据库能力接口（当前主要是 Redis）
 *
 * 说明：
 * - 只声明键值存储相关操作，不包含表/事务等关系型能力
 * - 可用于未来扩展到其他 KV 存储（如 KeyDB、Dragonfly 等）
 */
interface KeyValueMetadataOperations {
    // ==================== Redis / KV 通用操作（业务属性） ====================
    suspend fun keys(pattern: String = "*"): Result<List<String>>
    suspend fun keyType(key: String): Result<String>

    // ==================== KV 数据库级别能力 ====================
    suspend fun getDatabaseSize(): Result<Long>
    suspend fun listLogicalDatabases(): Result<List<String>>
    suspend fun switchLogicalDatabase(database: String): Result<Unit>
    suspend fun getCurrentLogicalDatabase(): Result<String>
}

/**
 * 关系型数据库客户端：通用命令 + 业务元数据
 */
data class RelationalClient(
    val context: DbExecutionContext,
    val executor: CommandExecutor,
    val metadata: RelationalMetadataOperations
)

/**
 * 键值型数据库客户端：通用命令 + 业务元数据
 */
data class KeyValueClient(
    val context: DbExecutionContext,
    val executor: CommandExecutor,
    val metadata: KeyValueMetadataOperations
)

/**
 * 顶层数据库客户端类型，用于区分关系型 / 键值型
 */
sealed class DatabaseClient {
    abstract val context: DbExecutionContext
    abstract val executor: CommandExecutor

    data class Relational(
        override val context: DbExecutionContext,
        override val executor: CommandExecutor,
        val metadata: RelationalMetadataOperations
    ) : DatabaseClient()

    data class KeyValue(
        override val context: DbExecutionContext,
        override val executor: CommandExecutor,
        val metadata: KeyValueMetadataOperations
    ) : DatabaseClient()
}

expect fun createDatabaseClient(config: DatabaseConfig): DatabaseClient