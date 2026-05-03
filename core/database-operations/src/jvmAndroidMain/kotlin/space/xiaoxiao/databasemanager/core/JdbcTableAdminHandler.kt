package space.xiaoxiao.databasemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JdbcTableAdminHandler(
    private val context: JdbcExecutionContext
) : TableAdminOperations {

    override suspend fun createTable(definition: TableDefinition, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = context.config.type
                val columnDefs = definition.columns.joinToString(",\n    ") { col ->
                    buildColumnDefinition(col, definition.primaryKeys.contains(col.name), dbType)
                }
                val pkClause = if (definition.primaryKeys.isNotEmpty()) {
                    ",\n    PRIMARY KEY (${definition.primaryKeys.joinToString(", ") { quoteIdentifier(it, dbType) }})"
                } else ""
                val ifNotExistsClause = if (definition.ifNotExists) "IF NOT EXISTS " else ""

                val tableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                    "${quoteIdentifier(schema, dbType)}.${quoteIdentifier(definition.name, dbType)}"
                } else {
                    quoteIdentifier(definition.name, dbType)
                }

                val sql = """
                    CREATE TABLE ${ifNotExistsClause}${tableName} (
                        $columnDefs$pkClause
                    )
                """.trimIndent()
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun dropTable(tableName: String, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = context.config.type
                val fullTableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                    "${quoteIdentifier(schema, dbType)}.${quoteIdentifier(tableName, dbType)}"
                } else {
                    quoteIdentifier(tableName, dbType)
                }
                val sql = "DROP TABLE IF EXISTS ${fullTableName}"
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun renameTable(oldName: String, newName: String, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = context.config.type
                val sql = when (dbType) {
                    DatabaseType.MYSQL -> "RENAME TABLE ${quoteIdentifier(oldName, dbType)} TO ${quoteIdentifier(newName, dbType)}"
                    DatabaseType.POSTGRESQL -> {
                        val targetSchema = schema ?: context.config.schema ?: "public"
                        "ALTER TABLE ${quoteIdentifier(targetSchema, dbType)}.${quoteIdentifier(oldName, dbType)} RENAME TO ${quoteIdentifier(newName, dbType)}"
                    }
                    else -> "ALTER TABLE ${quoteIdentifier(oldName, dbType)} RENAME TO ${quoteIdentifier(newName, dbType)}"
                }
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun truncateTable(tableName: String, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = context.config.type
                val fullTableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                    "${quoteIdentifier(schema, dbType)}.${quoteIdentifier(tableName, dbType)}"
                } else {
                    quoteIdentifier(tableName, dbType)
                }
                val sql = when (dbType) {
                    DatabaseType.MYSQL -> "TRUNCATE TABLE ${fullTableName}"
                    DatabaseType.POSTGRESQL -> "TRUNCATE ${fullTableName} RESTART IDENTITY CASCADE"
                    else -> "TRUNCATE TABLE ${fullTableName}"
                }
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
