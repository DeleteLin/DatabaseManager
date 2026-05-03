package space.xiaoxiao.databasemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection

class JdbcTransactionHandler(
    private val context: JdbcExecutionContext
) : TransactionOperations {

    override suspend fun beginTransaction(): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            conn.autoCommit = false
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun commitTransaction(): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            conn.commit()
            conn.autoCommit = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rollbackTransaction(): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            conn.rollback()
            conn.autoCommit = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setTransactionIsolation(level: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                conn.transactionIsolation = when (level) {
                    1 -> Connection.TRANSACTION_READ_UNCOMMITTED
                    2 -> Connection.TRANSACTION_READ_COMMITTED
                    3 -> Connection.TRANSACTION_REPEATABLE_READ
                    4 -> Connection.TRANSACTION_SERIALIZABLE
                    else -> Connection.TRANSACTION_READ_COMMITTED
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
