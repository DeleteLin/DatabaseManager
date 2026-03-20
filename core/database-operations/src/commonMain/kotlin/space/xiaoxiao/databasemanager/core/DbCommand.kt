package space.xiaoxiao.databasemanager.core

/**
 * 结构化执行命令（跨平台）
 *
 * - SQL：支持原始 SQL 与参数化 SQL（JDBC PreparedStatement）
 * - Redis：支持 argv 形式（避免字符串 split/去引号带来的歧义）
 */
sealed class DbCommand {
    data class RawSql(val sql: String) : DbCommand()

    /**
     * SQL + 有序参数列表。SQL 中的值参数必须使用 '?' 占位。
     *
     * 注意：标识符（schema/table/column）不能参数化绑定，需在模板渲染阶段完成引用/校验。
     */
    data class PreparedSql(
        val sql: String,
        val params: List<DbParam>
    ) : DbCommand()

    /**
     * Redis argv 命令（命令名 + 参数列表）。
     *
     * - command 不含参数（例如 SET/GET/DEL）
     * - args 以 argv 形式逐个传给驱动
     */
    data class RedisArgv(
        val command: String,
        val args: List<DbArg> = emptyList()
    ) : DbCommand()
}

sealed class DbArg {
    data class Param(val value: DbParam) : DbArg()
    data class Literal(val value: String) : DbArg()
}

/**
 * 显式参数类型（避免反射/弱类型）
 */
sealed class DbParam {
    data object Null : DbParam()
    data class Str(val value: String) : DbParam()
    data class Int32(val value: Int) : DbParam()
    data class Int64(val value: Long) : DbParam()
    data class Float64(val value: Double) : DbParam()
    data class Bool(val value: Boolean) : DbParam()
    data class Bytes(val value: ByteArray) : DbParam()
}

