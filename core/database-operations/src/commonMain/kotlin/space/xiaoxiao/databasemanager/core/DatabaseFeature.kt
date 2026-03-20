package space.xiaoxiao.databasemanager.core

/**
 * 数据库特性枚举
 * 用于声明数据库实现支持的功能
 */
enum class DatabaseFeature {
    // ==================== 基础查询 ====================
    /** 执行查询语句 (SELECT) */
    EXECUTE_QUERY,
    /** 执行更新语句 (INSERT/UPDATE/DELETE) */
    EXECUTE_UPDATE,
    /** 执行任意语句（自动判断类型） */
    EXECUTE_ANY,

    // ==================== 流式查询 ====================
    /** 游标查询 */
    CURSOR_QUERY,
    /** 分页查询 */
    PAGED_QUERY,

    // ==================== 表结构 ====================
    /** 列出表 */
    LIST_TABLES,
    /** 获取表结构 */
    GET_TABLE_SCHEMA,
    /** 获取表数据 */
    GET_TABLE_DATA,

    // ==================== 表管理 ====================
    /** 创建表 */
    CREATE_TABLE,
    /** 删除表 */
    DROP_TABLE,
    /** 重命名表 */
    RENAME_TABLE,
    /** 清空表 */
    TRUNCATE_TABLE,

    // ==================== 字段管理 ====================
    /** 添加字段 */
    ADD_COLUMN,
    /** 修改字段 */
    MODIFY_COLUMN,
    /** 删除字段 */
    DROP_COLUMN,

    // ==================== 索引管理 ====================
    /** 获取索引 */
    GET_INDEXES,
    /** 创建索引 */
    CREATE_INDEX,
    /** 删除索引 */
    DROP_INDEX,

    // ==================== 统计 ====================
    /** 获取表统计 */
    GET_TABLE_STATS,
    /** 获取数据库大小 */
    GET_DATABASE_SIZE,

    // ==================== 数据库管理 ====================
    /** 列出所有数据库 */
    LIST_DATABASES,
    /** 切换数据库 */
    SWITCH_DATABASE,
    /** 创建数据库 */
    CREATE_DATABASE,
    /** 删除数据库 */
    DROP_DATABASE,

    // ==================== 事务 ====================
    /** 事务支持 */
    TRANSACTION,

    // ==================== Redis 特有 ====================
    /** Key 操作 (GET/SET/DEL 等) */
    REDIS_KEY_OPERATIONS,
    /** Hash 操作 (HGET/HSET/HGETALL 等) */
    REDIS_HASH_OPERATIONS,
    /** List 操作 (LRANGE/LPUSH/RPOP 等) */
    REDIS_LIST_OPERATIONS,
    /** Set 操作 (SMEMBERS/SADD 等) */
    REDIS_SET_OPERATIONS,
    /** ZSet 操作 (ZRANGE/ZADD 等) */
    REDIS_ZSET_OPERATIONS,
    /** 发布订阅 */
    REDIS_PUB_SUB,
    /** Lua 脚本 */
    REDIS_SCRIPT,
}
