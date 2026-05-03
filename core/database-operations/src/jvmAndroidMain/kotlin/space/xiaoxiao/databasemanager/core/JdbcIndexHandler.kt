package space.xiaoxiao.databasemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JdbcIndexHandler(
    private val context: JdbcExecutionContext
) : IndexOperations {

    override suspend fun getIndexes(tableName: String, schema: String?): Result<List<IndexInfo>> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val indexes = mutableListOf<IndexInfo>()
                val dbType = context.config?.type

                when (dbType) {
                    DatabaseType.MYSQL -> {
                        val sql = "SHOW INDEX FROM ${quoteIdentifier(tableName, dbType)}"
                        withStatement(conn) { statement ->
                            statement.executeQuery(sql).useAndClose { resultSet ->
                                val indexMap = mutableMapOf<String, MutableList<Triple<String, Boolean, String>>>()

                                while (resultSet.next()) {
                                    val indexName = resultSet.getString("Key_name")
                                    val columnName = resultSet.getString("Column_name")
                                    val nonUnique = resultSet.getInt("Non_unique") == 0
                                    val indexType = resultSet.getString("Index_type") ?: "BTREE"
                                    indexMap.getOrPut(indexName) { mutableListOf() }.add(
                                        Triple(columnName, nonUnique, indexType)
                                    )
                                }

                                indexMap.forEach { (indexName, columns) ->
                                    indexes.add(
                                        IndexInfo(
                                            name = indexName,
                                            tableName = tableName,
                                            columns = columns.map { it.first },
                                            isUnique = columns.first().second,
                                            isPrimary = indexName == "PRIMARY",
                                            type = columns.first().third
                                        )
                                    )
                                }
                            }
                        }
                    }
                    DatabaseType.POSTGRESQL -> {
                        val targetSchema = schema ?: context.config?.schema ?: "public"
                        val sql = """
                            SELECT
                                i.relname as index_name,
                                a.attname as column_name,
                                ix.indisunique as is_unique,
                                ix.indisprimary as is_primary,
                                am.amname as index_type
                            FROM pg_class t
                            JOIN pg_index ix ON t.oid = ix.indrelid
                            JOIN pg_class i ON i.oid = ix.indexrelid
                            JOIN pg_am am ON i.relam = am.oid
                            JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = ANY(ix.indkey)
                            WHERE t.relname = ? AND a.attrelid = (
                                SELECT oid FROM pg_class
                                WHERE relname = ? AND relnamespace = (
                                    SELECT oid FROM pg_namespace WHERE nspname = ?
                                )
                            )
                            ORDER BY i.relname, a.attnum
                        """.trimIndent()
                        withPreparedStatement(
                            conn,
                            sql,
                            binder = { ps ->
                                ps.setString(1, tableName)
                                ps.setString(2, tableName)
                                ps.setString(3, targetSchema)
                            }
                        ) { ps ->
                            ps.executeQuery().useAndClose { resultSet ->
                                val indexMap = mutableMapOf<String, MutableList<Triple<String, Boolean, String>>>()

                                while (resultSet.next()) {
                                    val indexName = resultSet.getString("index_name")
                                    val columnName = resultSet.getString("column_name")
                                    val isUnique = resultSet.getBoolean("is_unique")
                                    val isPrimary = resultSet.getBoolean("is_primary")
                                    val indexType = resultSet.getString("index_type") ?: "btree"
                                    indexMap.getOrPut(indexName) { mutableListOf() }.add(
                                        Triple(columnName, isUnique || isPrimary, indexType.uppercase())
                                    )
                                }

                                indexMap.forEach { (indexName, columns) ->
                                    val first = columns.first()
                                    indexes.add(
                                        IndexInfo(
                                            name = indexName,
                                            tableName = tableName,
                                            columns = columns.map { it.first },
                                            isUnique = first.second,
                                            isPrimary = first.second && first.third == "BTREE",
                                            type = first.third
                                        )
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        val resultSet = conn.metaData.getIndexInfo(null, null, tableName, false, false)
                        val indexMap = mutableMapOf<String, MutableList<String>>()
                        while (resultSet.next()) {
                            val indexName = resultSet.getString("INDEX_NAME")
                            val columnName = resultSet.getString("COLUMN_NAME")
                            val isUnique = !resultSet.getBoolean("NON_UNIQUE")
                            if (indexName != null && columnName != null) {
                                indexMap.getOrPut(indexName) { mutableListOf() }.add(columnName!!)
                            }
                        }
                        resultSet.close()

                        indexMap.forEach { (indexName, columns) ->
                            indexes.add(
                                IndexInfo(
                                    name = indexName,
                                    tableName = tableName,
                                    columns = columns,
                                    isUnique = indexName.contains("UNIQUE") || indexName.contains("unique"),
                                    isPrimary = indexName == "PRIMARY" || indexName.contains("PRIMARY"),
                                    type = "BTREE"
                                )
                            )
                        }
                    }
                }
                Result.success(indexes)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun createIndex(
        tableName: String,
        indexName: String,
        columns: List<String>,
        isUnique: Boolean,
        schema: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            val dbType = context.config?.type
            val uniqueClause = if (isUnique) "UNIQUE " else ""
            val columnsClause = columns.joinToString(", ") { quoteIdentifier(it, dbType) }
            val fullTableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                "${quoteIdentifier(schema, dbType)}.${quoteIdentifier(tableName, dbType)}"
            } else {
                quoteIdentifier(tableName, dbType)
            }
            val sql = "CREATE ${uniqueClause}INDEX ${quoteIdentifier(indexName, dbType)} ON ${fullTableName} ($columnsClause)"
            withStatement(conn) { it.execute(sql) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun dropIndex(tableName: String, indexName: String, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = context.config?.type
                val sql = when (dbType) {
                    DatabaseType.MYSQL -> "DROP INDEX ${quoteIdentifier(indexName, dbType)} ON ${quoteIdentifier(tableName, dbType)}"
                    DatabaseType.POSTGRESQL -> "DROP INDEX ${quoteIdentifier(indexName, dbType)}"
                    else -> "DROP INDEX ${quoteIdentifier(indexName, dbType)}"
                }
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
