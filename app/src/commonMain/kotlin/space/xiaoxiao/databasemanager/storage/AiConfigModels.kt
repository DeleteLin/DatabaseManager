package space.xiaoxiao.databasemanager.storage

import kotlinx.serialization.Serializable
import space.xiaoxiao.databasemanager.core.DatabaseType

/**
 * AI 接口类型
 */
enum class ApiType {
    OPENAI,     // OpenAI 兼容接口
    CLAUDE      // Claude 兼容接口
}

/**
 * AI 配置信息
 */
@Serializable
data class AiConfig(
    val apiType: ApiType = ApiType.OPENAI,
    val baseUrl: String = "",
    val apiKey: String = "",
    val customPromptZh: String = DEFAULT_AI_PROMPT_ZH,
    val customPromptEn: String = DEFAULT_AI_PROMPT_EN
) {
    companion object {
        /**
         * 中文默认提示词
         */
        const val DEFAULT_AI_PROMPT_ZH = """你是一个 SQL 专家。请根据以下数据库信息生成 SQL 语句：

数据库类型：{dbType}
表结构信息：
{tableSchema}

用户需求：{userInput}

请生成符合 {dbType} 语法的 SQL 语句，只返回 SQL 语句本身，不要解释。"""

        /**
         * 英文默认提示词
         */
        const val DEFAULT_AI_PROMPT_EN = """You are an SQL expert. Please generate SQL statement based on the following database information:

Database Type: {dbType}
Table Schema:
{tableSchema}

User Request: {userInput}

Please generate SQL statement that complies with {dbType} syntax. Return only the SQL statement itself, no explanation needed."""
    }
}

/**
 * SQL 模板
 */
data class SqlTemplate(
    val id: String,
    val name: String,
    val description: String,
    val sql: String,
    val databaseTypes: List<DatabaseType>
)

/**
 * SQL 模板库 - 提供常用 SQL 模板
 */
object SqlTemplateLibrary {
    /**
     * MySQL 模板
     */
    val mysqlTemplates: List<SqlTemplate> = listOf(
        SqlTemplate(
            id = "mysql_select_all",
            name = "SELECT * FROM table",
            description = "查询表的所有数据",
            sql = "SELECT * FROM `table_name`;",
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_select_where",
            name = "SELECT ... WHERE ...",
            description = "带条件的查询",
            sql = """SELECT column1, column2
FROM `table_name`
WHERE condition
ORDER BY column1 DESC
LIMIT 10;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_insert",
            name = "INSERT INTO ... VALUES ...",
            description = "插入数据",
            sql = """INSERT INTO `table_name` (column1, column2, column3)
VALUES ('value1', 'value2', 'value3');""".trimIndent(),
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_update",
            name = "UPDATE ... SET ... WHERE ...",
            description = "更新数据",
            sql = """UPDATE `table_name`
SET column1 = 'value1', column2 = 'value2'
WHERE condition;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_delete",
            name = "DELETE FROM ... WHERE ...",
            description = "删除数据",
            sql = """DELETE FROM `table_name`
WHERE condition;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_create_table",
            name = "CREATE TABLE ...",
            description = "创建表",
            sql = """CREATE TABLE `table_name` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) UNIQUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_add_column",
            name = "ALTER TABLE ADD COLUMN ...",
            description = "添加列",
            sql = """ALTER TABLE `table_name`
ADD COLUMN `new_column` VARCHAR(255) NOT NULL DEFAULT '';""".trimIndent(),
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_show_tables",
            name = "SHOW TABLES",
            description = "列出所有表",
            sql = "SHOW TABLES;",
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_show_create_table",
            name = "SHOW CREATE TABLE ...",
            description = "查看表的创建语句",
            sql = "SHOW CREATE TABLE `table_name`;",
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_explain",
            name = "EXPLAIN SELECT ...",
            description = "分析查询执行计划",
            sql = "EXPLAIN SELECT * FROM `table_name` WHERE condition;",
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_join",
            name = "SELECT ... JOIN ...",
            description = "多表联接查询",
            sql = """SELECT a.column1, b.column2
FROM `table_a` AS a
INNER JOIN `table_b` AS b ON a.id = b.a_id
WHERE a.status = 1;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.MYSQL)
        ),
        SqlTemplate(
            id = "mysql_group_by",
            name = "SELECT ... GROUP BY ...",
            description = "分组统计查询",
            sql = """SELECT category, COUNT(*) as count, AVG(price) as avg_price
FROM `products`
GROUP BY category
HAVING count > 10;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.MYSQL)
        )
    )

    /**
     * PostgreSQL 模板
     */
    val postgresqlTemplates: List<SqlTemplate> = listOf(
        SqlTemplate(
            id = "pg_select_all",
            name = "SELECT * FROM table",
            description = "查询表的所有数据",
            sql = "SELECT * FROM table_name;",
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_select_where",
            name = "SELECT ... WHERE ...",
            description = "带条件的查询",
            sql = """SELECT column1, column2
FROM table_name
WHERE condition
ORDER BY column1 DESC
LIMIT 10;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_insert",
            name = "INSERT INTO ... VALUES ...",
            description = "插入数据",
            sql = """INSERT INTO table_name (column1, column2, column3)
VALUES ('value1', 'value2', 'value3')
RETURNING id;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_update",
            name = "UPDATE ... SET ... WHERE ...",
            description = "更新数据",
            sql = """UPDATE table_name
SET column1 = 'value1', column2 = 'value2'
WHERE condition;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_delete",
            name = "DELETE FROM ... WHERE ...",
            description = "删除数据",
            sql = """DELETE FROM table_name
WHERE condition;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_create_table",
            name = "CREATE TABLE ...",
            description = "创建表",
            sql = """CREATE TABLE table_name (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);""".trimIndent(),
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_add_column",
            name = "ALTER TABLE ADD COLUMN ...",
            description = "添加列",
            sql = """ALTER TABLE table_name
ADD COLUMN new_column VARCHAR(255) NOT NULL DEFAULT '';""".trimIndent(),
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_list_tables",
            name = "\\dt (列出表)",
            description = "列出所有表 (psql 命令)",
            sql = "\\dt",
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_describe_table",
            name = "\\d table_name (查看表结构)",
            description = "查看表结构 (psql 命令)",
            sql = "\\d table_name",
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_explain_analyze",
            name = "EXPLAIN ANALYZE SELECT ...",
            description = "分析查询执行计划",
            sql = "EXPLAIN ANALYZE SELECT * FROM table_name WHERE condition;",
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_join",
            name = "SELECT ... JOIN ...",
            description = "多表联接查询",
            sql = """SELECT a.column1, b.column2
FROM table_a AS a
INNER JOIN table_b AS b ON a.id = b.a_id
WHERE a.status = 1;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        ),
        SqlTemplate(
            id = "pg_cte",
            name = "WITH ... AS (CTE)",
            description = "使用公用表表达式 (CTE)",
            sql = """WITH filtered_data AS (
    SELECT * FROM table_name WHERE status = 1
)
SELECT * FROM filtered_data ORDER BY created_at DESC;""".trimIndent(),
            databaseTypes = listOf(DatabaseType.POSTGRESQL)
        )
    )

    /**
     * Redis 模板
     */
    val redisTemplates: List<SqlTemplate> = listOf(
        SqlTemplate(
            id = "redis_get",
            name = "GET key",
            description = "获取字符串值",
            sql = "GET mykey",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_set",
            name = "SET key value",
            description = "设置字符串值",
            sql = "SET mykey 'myvalue'",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_keys",
            name = "KEYS pattern",
            description = "查找匹配模式的键",
            sql = "KEYS pattern:*",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_hgetall",
            name = "HGETALL key",
            description = "获取哈希的所有字段和值",
            sql = "HGETALL myhash",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_hset",
            name = "HSET key field value",
            description = "设置哈希字段",
            sql = "HSET myhash field1 'value1'",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_lrange",
            name = "LRANGE key 0 -1",
            description = "获取列表的所有元素",
            sql = "LRANGE mylist 0 -1",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_lpush",
            name = "LPUSH key value",
            description = "向列表左侧推入元素",
            sql = "LPUSH mylist 'value'",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_smembers",
            name = "SMEMBERS key",
            description = "获取集合的所有成员",
            sql = "SMEMBERS myset",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_sadd",
            name = "SADD key member",
            description = "向集合添加成员",
            sql = "SADD myset 'member'",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_del",
            name = "DEL key",
            description = "删除键",
            sql = "DEL mykey",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_exists",
            name = "EXISTS key",
            description = "检查键是否存在",
            sql = "EXISTS mykey",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_ttl",
            name = "TTL key",
            description = "查看键的剩余过期时间",
            sql = "TTL mykey",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_type",
            name = "TYPE key",
            description = "查看键的数据类型",
            sql = "TYPE mykey",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_expire",
            name = "EXPIRE key seconds",
            description = "设置键的过期时间",
            sql = "EXPIRE mykey 3600",
            databaseTypes = listOf(DatabaseType.REDIS)
        ),
        SqlTemplate(
            id = "redis_scan",
            name = "SCAN cursor MATCH pattern",
            description = "扫描键（适合生产环境）",
            sql = "SCAN 0 MATCH pattern:* COUNT 100",
            databaseTypes = listOf(DatabaseType.REDIS)
        )
    )

    /**
     * 根据数据库类型获取模板列表
     */
    fun getTemplates(databaseType: DatabaseType): List<SqlTemplate> {
        return when (databaseType) {
            DatabaseType.MYSQL -> mysqlTemplates
            DatabaseType.POSTGRESQL -> postgresqlTemplates
            DatabaseType.REDIS -> redisTemplates
        }
    }
}
