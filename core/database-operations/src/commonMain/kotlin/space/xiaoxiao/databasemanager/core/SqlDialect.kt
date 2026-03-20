package space.xiaoxiao.databasemanager.core

interface SqlDialect {
    fun quoteIdentifier(raw: String): String
}

object MysqlDialect : SqlDialect {
    override fun quoteIdentifier(raw: String): String = "`${raw.replace("`", "``")}`"
}

object PostgresDialect : SqlDialect {
    override fun quoteIdentifier(raw: String): String = "\"${raw.replace("\"", "\"\"")}\""
}

object SqlDialectResolver {
    fun forDatabaseType(type: DatabaseType): SqlDialect = when (type) {
        DatabaseType.MYSQL -> MysqlDialect
        DatabaseType.POSTGRESQL -> PostgresDialect
        DatabaseType.REDIS -> MysqlDialect // 不会用于 Redis；给一个默认值避免 when 漏分支
    }
}

