package space.xiaoxiao.databasemanager.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * 编辑器语言接口
 * 支持多种数据库语言的语法高亮和自动补全
 */
interface EditorLanguageSupport {
    /**
     * 语言名称
     */
    val name: String

    /**
     * 语言关键词
     */
    val keywords: Set<String>

    /**
     * 内置函数/命令
     */
    val functions: Set<String>

    /**
     * 进行语法高亮
     */
    fun highlight(code: String, colors: CodeEditorColors): AnnotatedString

    /**
     * 获取自动补全建议
     */
    fun getAutoCompleteSuggestions(prefix: String, limit: Int): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val upperPrefix = prefix.uppercase()
        return (keywords.filter { it.startsWith(upperPrefix) } +
                functions.filter { it.startsWith(upperPrefix) })
            .take(limit)
    }

    /**
     * 格式化代码
     */
    fun format(code: String): String = code
}

/**
 * SQL 语言支持实现
 */
object SqlLanguageSupport : EditorLanguageSupport {
    override val name = "SQL"

    override val keywords = setOf(
        // DML
        "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "VALUES", "SET", "INTO",
        // DDL
        "CREATE", "DROP", "ALTER", "TABLE", "INDEX", "VIEW", "DATABASE", "SCHEMA",
        "TRUNCATE", "RENAME", "ADD", "COLUMN", "CONSTRAINT",
        // DCL
        "GRANT", "REVOKE",
        // JOIN
        "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "FULL", "CROSS", "ON", "USING",
        // 条件
        "AND", "OR", "NOT", "IN", "LIKE", "BETWEEN", "EXISTS", "IS", "NULL",
        "CASE", "WHEN", "THEN", "ELSE", "END",
        // 排序和分组
        "ORDER", "BY", "GROUP", "HAVING", "LIMIT", "OFFSET", "ASC", "DESC",
        // 聚合函数
        "COUNT", "SUM", "AVG", "MIN", "MAX", "DISTINCT",
        // 约束
        "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "UNIQUE", "CHECK", "DEFAULT",
        // 数据类型
        "INT", "INTEGER", "BIGINT", "SMALLINT", "TINYINT",
        "VARCHAR", "CHAR", "TEXT", "LONGTEXT", "MEDIUMTEXT",
        "DECIMAL", "NUMERIC", "FLOAT", "DOUBLE", "REAL",
        "DATE", "DATETIME", "TIMESTAMP", "TIME", "YEAR",
        "BOOLEAN", "BOOL", "BLOB", "JSON",
        // 其他
        "AS", "FROM", "TO", "WITH", "RECURSIVE", "UNION", "ALL", "INTERSECT", "EXCEPT",
        "AUTO_INCREMENT", "AUTOINCREMENT", "IDENTITY",
        "ENGINE", "CHARSET", "COLLATE", "COMMENT",
        "CASCADE", "RESTRICT", "NO", "ACTION",
        "TRUE", "FALSE", "UNKNOWN"
    )

    override val functions = setOf(
        // 通用函数
        "IF", "IFNULL", "NULLIF", "COALESCE", "CONCAT", "CONCAT_WS",
        "SUBSTRING", "SUBSTR", "LENGTH", "CHAR_LENGTH", "UPPER", "LOWER", "TRIM", "LTRIM", "RTRIM",
        "REPLACE", "REVERSE", "LEFT", "RIGHT", "LPAD", "RPAD", "INSTR", "LOCATE", "POSITION",
        // 日期函数
        "NOW", "CURDATE", "CURTIME", "DATE_FORMAT", "STR_TO_DATE", "DATEDIFF", "DATE_ADD", "DATE_SUB",
        "YEAR", "MONTH", "DAY", "HOUR", "MINUTE", "SECOND", "WEEK", "QUARTER",
        // 数学函数
        "ABS", "CEIL", "CEILING", "FLOOR", "ROUND", "TRUNCATE", "MOD", "POW", "POWER", "SQRT",
        "RAND", "SIGN", "PI",
        // 类型转换
        "CAST", "CONVERT", "BINARY",
        // 聚合相关
        "GROUP_CONCAT", "FIND_IN_SET", "FIELD", "ELT",
        // 窗口函数
        "ROW_NUMBER", "RANK", "DENSE_RANK", "LAG", "LEAD", "FIRST_VALUE", "LAST_VALUE",
        // 加密函数
        "UUID", "MD5", "SHA1", "SHA2", "AES_ENCRYPT", "AES_DECRYPT"
    )

    override fun highlight(code: String, colors: CodeEditorColors): AnnotatedString {
        return SqlSyntaxHighlighter.highlight(code, colors)
    }

    override fun format(code: String): String {
        return SqlFormatter.format(code)
    }
}

/**
 * Redis 语言支持实现
 */
object RedisLanguageSupport : EditorLanguageSupport {
    override val name = "Redis"

    override val keywords = setOf(
        // 键操作
        "DEL", "DUMP", "EXISTS", "EXPIRE", "EXPIREAT", "KEYS", "MIGRATE", "MOVE",
        "OBJECT", "PERSIST", "PEXPIRE", "PEXPIREAT", "PTTL", "RANDOMKEY", "RENAME",
        "RENAMENX", "RESTORE", "SORT", "TTL", "TYPE", "SCAN",
        // 字符串
        "APPEND", "BITCOUNT", "BITOP", "DECR", "DECRBY", "GET", "GETBIT", "GETRANGE",
        "GETSET", "INCR", "INCRBY", "INCRBYFLOAT", "MGET", "MSET", "MSETNX", "PSETEX",
        "SET", "SETBIT", "SETEX", "SETNX", "SETRANGE", "STRLEN",
        // 哈希
        "HDEL", "HEXISTS", "HGET", "HGETALL", "HINCRBY", "HINCRBYFLOAT", "HKEYS",
        "HLEN", "HMGET", "HMSET", "HSET", "HSETNX", "HVALS", "HSCAN",
        // 列表
        "BLPOP", "BRPOP", "BRPOPLPUSH", "LINDEX", "LINSERT", "LLEN", "LPOP", "LPUSH",
        "LPUSHX", "LRANGE", "LREM", "LSET", "LTRIM", "RPOP", "RPOPLPUSH", "RPUSH",
        "RPUSHX",
        // 集合
        "SADD", "SCARD", "SDIFF", "SDIFFSTORE", "SINTER", "SINTERSTORE", "SISMEMBER",
        "SMEMBERS", "SMOVE", "SPOP", "SRANDMEMBER", "SREM", "SUNION", "SUNIONSTORE", "SSCAN",
        // 有序集合
        "ZADD", "ZCARD", "ZCOUNT", "ZINCRBY", "ZINTERSTORE", "ZRANGE", "ZRANGEBYSCORE",
        "ZRANK", "ZREM", "ZREMRANGEBYRANK", "ZREMRANGEBYSCORE", "ZREVRANGE",
        "ZREVRANGEBYSCORE", "ZREVRANK", "ZSCORE", "ZUNIONSTORE", "ZSCAN",
        // HyperLogLog
        "PFADD", "PFCOUNT", "PFMERGE",
        // 发布订阅
        "PSUBSCRIBE", "PUBLISH", "PUBSUB", "PUNSUBSCRIBE", "SUBSCRIBE", "UNSUBSCRIBE",
        // 事务
        "DISCARD", "EXEC", "MULTI", "UNWATCH", "WATCH",
        // 脚本
        "EVAL", "EVALSHA", "SCRIPT",
        // 连接
        "AUTH", "ECHO", "PING", "QUIT", "SELECT",
        // 服务器
        "BGREWRITEAOF", "BGSAVE", "CLIENT", "CONFIG", "DBSIZE", "DEBUG", "FLUSHALL",
        "FLUSHDB", "INFO", "LASTSAVE", "MONITOR", "ROLE", "SAVE", "SHUTDOWN", "SLAVEOF",
        "SLOWLOG", "SYNC", "TIME",
        // 集群
        "CLUSTER", "READONLY", "READWRITE"
    )

    override val functions = emptySet<String>()

    override fun highlight(code: String, colors: CodeEditorColors): AnnotatedString {
        return RedisSyntaxHighlighter.highlight(code, colors)
    }

    override fun format(code: String): String {
        // Redis 命令通常不需要格式化
        return code.trim()
    }
}

/**
 * Redis 语法高亮器
 */
object RedisSyntaxHighlighter {
    fun highlight(code: String, colors: CodeEditorColors): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            val parts = code.split(Regex("\\s+"))
            parts.forEachIndexed { index, part ->
                if (index > 0) append(" ")

                when {
                    // 第一个词是命令
                    index == 0 && part.uppercase() in RedisLanguageSupport.keywords -> {
                        appendWithStyle(part, colors.keywordStyle)
                    }
                    // 数字
                    part.matches(Regex("-?\\d+(\\.\\d+)?")) -> {
                        appendWithStyle(part, colors.numberStyle)
                    }
                    // 字符串（带引号）
                    part.startsWith("\"") || part.startsWith("'") -> {
                        appendWithStyle(part, colors.stringStyle)
                    }
                    // 其他
                    else -> append(part)
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendWithStyle(text: String, style: SpanStyle) {
        withStyle(style) {
            append(text)
        }
    }
}

/**
 * 语言支持工厂
 */
object LanguageSupportFactory {
    /**
     * 根据数据库类型获取语言支持
     */
    fun getSupport(language: EditorLanguage): EditorLanguageSupport = when (language) {
        EditorLanguage.SQL -> SqlLanguageSupport
        EditorLanguage.REDIS -> RedisLanguageSupport
    }

    /**
     * 从数据库类型获取编辑器语言
     */
    fun fromDatabaseType(type: space.xiaoxiao.databasemanager.core.DatabaseType): EditorLanguage = when (type) {
        space.xiaoxiao.databasemanager.core.DatabaseType.REDIS -> EditorLanguage.REDIS
        else -> EditorLanguage.SQL
    }
}

/**
 * 扩展 EditorLanguage 枚举以支持 Redis
 */
enum class EditorLanguage {
    SQL,
    REDIS
}