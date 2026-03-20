package space.xiaoxiao.databasemanager.core.exceptions

/**
 * 数据库操作基础异常
 */
sealed class DatabaseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 连接异常
 */
class DatabaseConnectionException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)

/**
 * 查询异常
 */
class DatabaseQueryException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)

/**
 * 事务异常
 */
class DatabaseTransactionException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)

/**
 * 游标异常
 */
class DatabaseCursorException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)

/**
 * 表操作异常
 */
class DatabaseTableException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)

/**
 * 索引操作异常
 */
class DatabaseIndexException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)

/**
 * 不支持的操作异常
 */
class DatabaseNotSupportedException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)

/**
 * 参数无效异常
 */
class DatabaseInvalidArgumentException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)

/**
 * 超时异常
 */
class DatabaseTimeoutException(message: String, cause: Throwable? = null) : DatabaseException(message, cause)
