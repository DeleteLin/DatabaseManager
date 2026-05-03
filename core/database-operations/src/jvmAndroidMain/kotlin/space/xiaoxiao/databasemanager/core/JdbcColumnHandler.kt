package space.xiaoxiao.databasemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JdbcColumnHandler(
    private val context: JdbcExecutionContext
) : ColumnOperations {

    override suspend fun addColumn(tableName: String, column: ColumnDefinition, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = context.config?.type
                val columnDef = buildColumnDefinition(column, false, dbType)
                val fullTableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                    "${quoteIdentifier(schema, dbType)}.${quoteIdentifier(tableName, dbType)}"
                } else {
                    quoteIdentifier(tableName, dbType)
                }
                val sql = "ALTER TABLE ${fullTableName} ADD COLUMN $columnDef"
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun modifyColumn(tableName: String, modification: ColumnModification, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = context.config?.type
                val fullTableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                    "${quoteIdentifier(schema, dbType)}.${quoteIdentifier(tableName, dbType)}"
                } else {
                    quoteIdentifier(tableName, dbType)
                }

                val sql = when (dbType) {
                    DatabaseType.MYSQL -> {
                        val charsetClause = if (modification.charset != null &&
                            modification.typeName.uppercase() in listOf("VARCHAR", "CHAR", "TEXT", "LONGTEXT", "MEDIUMTEXT", "TINYTEXT")) {
                            requireSafeCharsetName(modification.charset)
                            "CHARACTER SET ${modification.charset}"
                        } else ""
                        val nullableClause = if (modification.isNullable) "NULL" else "NOT NULL"
                        val defaultClause = renderDefaultValueClause(modification.typeName, modification.defaultValue, dbType)
                        val commentClause = modification.comment?.let {
                            "COMMENT '${escapeSqlStringLiteral(it)}'"
                        } ?: ""
                        val changeClause = if (modification.oldName != null && modification.oldName != modification.newName) {
                            "CHANGE COLUMN ${quoteIdentifier(modification.oldName, dbType)}"
                        } else {
                            "MODIFY COLUMN"
                        }
                        "ALTER TABLE ${fullTableName} $changeClause ${quoteIdentifier(modification.newName, dbType)} ${modification.typeName} $charsetClause $nullableClause $defaultClause $commentClause".trim()
                    }
                    DatabaseType.POSTGRESQL -> {
                        val statements = mutableListOf<String>()
                        if (modification.oldName != null && modification.oldName != modification.newName) {
                            statements.add("ALTER TABLE ${fullTableName} RENAME COLUMN ${quoteIdentifier(modification.oldName, dbType)} TO ${quoteIdentifier(modification.newName, dbType)}")
                        }
                        statements.add("ALTER TABLE ${fullTableName} ALTER COLUMN ${quoteIdentifier(modification.newName, dbType)} TYPE ${modification.typeName}")
                        statements.add("ALTER TABLE ${fullTableName} ALTER COLUMN ${quoteIdentifier(modification.newName, dbType)} ${if (modification.isNullable) "DROP NOT NULL" else "SET NOT NULL"}")
                        if (modification.defaultValue != null) {
                            val clause = renderDefaultValueClause(modification.typeName, modification.defaultValue, dbType)
                            statements.add("ALTER TABLE ${fullTableName} ALTER COLUMN ${quoteIdentifier(modification.newName, dbType)} SET $clause")
                        } else {
                            statements.add("ALTER TABLE ${fullTableName} ALTER COLUMN ${quoteIdentifier(modification.newName, dbType)} DROP DEFAULT")
                        }
                        statements.joinToString("; ")
                    }
                    else -> "ALTER TABLE ${fullTableName} MODIFY COLUMN ${quoteIdentifier(modification.newName, dbType)} ${modification.typeName}"
                }
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun dropColumn(tableName: String, columnName: String, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = context.config?.type
                val fullTableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                    "${quoteIdentifier(schema, dbType)}.${quoteIdentifier(tableName, dbType)}"
                } else {
                    quoteIdentifier(tableName, dbType)
                }
                val sql = "ALTER TABLE ${fullTableName} DROP COLUMN ${quoteIdentifier(columnName, dbType)}"
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
