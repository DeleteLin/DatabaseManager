package space.xiaoxiao.databasemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JdbcTableHandler(
    private val context: JdbcExecutionContext
) : TableOperations {

    override suspend fun listTables(schema: String?): Result<List<TableInfo>> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val metaData = conn.metaData
                val dbType = context.config.type

                if (dbType == DatabaseType.POSTGRESQL) {
                    val targetSchema = schema ?: context.config.schema ?: "public"
                    val resultSet = metaData.getTables(
                        null,
                        targetSchema,
                        "%",
                        arrayOf("TABLE", "VIEW")
                    )
                    val tables = mutableListOf<TableInfo>()
                    while (resultSet.next()) {
                        tables.add(
                            TableInfo(
                                name = resultSet.getString("TABLE_NAME"),
                                schema = resultSet.getString("TABLE_SCHEM"),
                                type = resultSet.getString("TABLE_TYPE")
                            )
                        )
                    }
                    resultSet.close()
                    Result.success(tables)
                } else {
                    val catalog = conn.catalog
                    val resultSet = metaData.getTables(
                        catalog,
                        catalog,
                        "%",
                        arrayOf("TABLE", "VIEW")
                    )
                    val tables = mutableListOf<TableInfo>()
                    while (resultSet.next()) {
                        tables.add(
                            TableInfo(
                                name = resultSet.getString("TABLE_NAME"),
                                schema = resultSet.getString("TABLE_SCHEM"),
                                type = resultSet.getString("TABLE_TYPE")
                            )
                        )
                    }
                    resultSet.close()
                    Result.success(tables)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getTableSchema(tableName: String, schema: String?): Result<TableSchema> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = context.config.type

                if (dbType == DatabaseType.POSTGRESQL) {
                    val targetSchema = schema ?: context.config.schema ?: "public"

                    val columnsResultSet = conn.metaData.getColumns(
                        null,
                        targetSchema,
                        tableName,
                        null
                    )
                    val columns = mutableListOf<ColumnDefinition>()
                    while (columnsResultSet.next()) {
                        columns.add(
                            ColumnDefinition(
                                name = columnsResultSet.getString("COLUMN_NAME"),
                                typeName = columnsResultSet.getString("TYPE_NAME"),
                                isNullable = columnsResultSet.getInt("NULLABLE") == java.sql.DatabaseMetaData.columnNullable,
                                isPrimaryKey = false,
                                isAutoIncrement = columnsResultSet.getString("IS_AUTOINCREMENT") == "YES",
                                defaultValue = columnsResultSet.getString("COLUMN_DEF"),
                                comment = null
                            )
                        )
                    }
                    columnsResultSet.close()

                    val primaryKeysResultSet = conn.metaData.getPrimaryKeys(
                        null,
                        targetSchema,
                        tableName
                    )
                    val primaryKeys = mutableListOf<String>()
                    while (primaryKeysResultSet.next()) {
                        primaryKeys.add(primaryKeysResultSet.getString("COLUMN_NAME"))
                    }
                    primaryKeysResultSet.close()

                    val foreignKeysResultSet = conn.metaData.getImportedKeys(
                        null,
                        targetSchema,
                        tableName
                    )
                    val foreignKeys = mutableListOf<ForeignKey>()
                    while (foreignKeysResultSet.next()) {
                        foreignKeys.add(
                            ForeignKey(
                                columnName = foreignKeysResultSet.getString("FKCOLUMN_NAME"),
                                referencedTable = foreignKeysResultSet.getString("PKTABLE_NAME"),
                                referencedColumn = foreignKeysResultSet.getString("PKCOLUMN_NAME")
                            )
                        )
                    }
                    foreignKeysResultSet.close()

                    val indexesResultSet = conn.metaData.getIndexInfo(
                        null,
                        targetSchema,
                        tableName,
                        false,
                        false
                    )
                    val indexes = mutableListOf<Index>()
                    val indexMap = mutableMapOf<String, MutableList<String>>()
                    while (indexesResultSet.next()) {
                        val indexName = indexesResultSet.getString("INDEX_NAME")
                        val columnName = indexesResultSet.getString("COLUMN_NAME")
                        val isUnique = !indexesResultSet.getBoolean("NON_UNIQUE")
                        if (indexName != null && columnName != null) {
                            indexMap.getOrPut(indexName) { mutableListOf() }.add(columnName)
                            if (!indexes.any { it.name == indexName }) {
                                indexes.add(
                                    Index(
                                        name = indexName,
                                        columns = emptyList(),
                                        isUnique = isUnique
                                    )
                                )
                            }
                        }
                    }
                    indexesResultSet.close()

                    val indexesWithColumns = indexes.map { index ->
                        index.copy(columns = indexMap[index.name] ?: emptyList())
                    }

                    Result.success(
                        TableSchema(
                            tableName = tableName,
                            columns = columns.map { it.copy(isPrimaryKey = it.name in primaryKeys) },
                            primaryKeys = primaryKeys,
                            foreignKeys = foreignKeys,
                            indexes = indexesWithColumns
                        )
                    )
                } else {
                    val catalog = conn.catalog

                    val columnsResultSet = conn.metaData.getColumns(
                        catalog,
                        catalog,
                        tableName,
                        null
                    )
                    val columns = mutableListOf<ColumnDefinition>()
                    while (columnsResultSet.next()) {
                        columns.add(
                            ColumnDefinition(
                                name = columnsResultSet.getString("COLUMN_NAME"),
                                typeName = columnsResultSet.getString("TYPE_NAME"),
                                isNullable = columnsResultSet.getInt("NULLABLE") == java.sql.DatabaseMetaData.columnNullable,
                                isPrimaryKey = false,
                                isAutoIncrement = columnsResultSet.getString("IS_AUTOINCREMENT") == "YES",
                                defaultValue = columnsResultSet.getString("COLUMN_DEF"),
                                comment = columnsResultSet.getString("REMARKS")
                            )
                        )
                    }
                    columnsResultSet.close()

                    val primaryKeysResultSet = conn.metaData.getPrimaryKeys(catalog, catalog, tableName)
                    val primaryKeys = mutableListOf<String>()
                    while (primaryKeysResultSet.next()) {
                        primaryKeys.add(primaryKeysResultSet.getString("COLUMN_NAME"))
                    }
                    primaryKeysResultSet.close()

                    val foreignKeysResultSet = conn.metaData.getImportedKeys(catalog, catalog, tableName)
                    val foreignKeys = mutableListOf<ForeignKey>()
                    while (foreignKeysResultSet.next()) {
                        foreignKeys.add(
                            ForeignKey(
                                columnName = foreignKeysResultSet.getString("FKCOLUMN_NAME"),
                                referencedTable = foreignKeysResultSet.getString("PKTABLE_NAME"),
                                referencedColumn = foreignKeysResultSet.getString("PKCOLUMN_NAME")
                            )
                        )
                    }
                    foreignKeysResultSet.close()

                    val indexesResultSet = conn.metaData.getIndexInfo(catalog, catalog, tableName, false, false)
                    val indexes = mutableListOf<Index>()
                    val indexMap = mutableMapOf<String, MutableList<String>>()
                    while (indexesResultSet.next()) {
                        val indexName = indexesResultSet.getString("INDEX_NAME")
                        val columnName = indexesResultSet.getString("COLUMN_NAME")
                        val isUnique = !indexesResultSet.getBoolean("NON_UNIQUE")
                        if (indexName != null && columnName != null) {
                            indexMap.getOrPut(indexName) { mutableListOf() }.add(columnName)
                            if (!indexes.any { it.name == indexName }) {
                                indexes.add(
                                    Index(
                                        name = indexName,
                                        columns = emptyList(),
                                        isUnique = isUnique
                                    )
                                )
                            }
                        }
                    }
                    indexesResultSet.close()

                    val indexesWithColumns = indexes.map { index ->
                        index.copy(columns = indexMap[index.name] ?: emptyList())
                    }

                    Result.success(
                        TableSchema(
                            tableName = tableName,
                            columns = columns.map { it.copy(isPrimaryKey = it.name in primaryKeys) },
                            primaryKeys = primaryKeys,
                            foreignKeys = foreignKeys,
                            indexes = indexesWithColumns
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getTableData(tableName: String, schema: String?, limit: Int, offset: Int): Result<QueryResult> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val startTime = System.currentTimeMillis()
                val dbType = context.config.type

                val sql = if (dbType == DatabaseType.POSTGRESQL) {
                    val targetSchema = schema ?: context.config.schema ?: "public"
                    "SELECT * FROM ${quoteIdentifier(targetSchema, dbType)}.${quoteIdentifier(tableName, dbType)} LIMIT $limit OFFSET $offset"
                } else {
                    "SELECT * FROM ${quoteIdentifier(tableName, dbType)} LIMIT $limit OFFSET $offset"
                }
                withStatement(conn) { statement ->
                    statement.executeQuery(sql).useAndClose { resultSet ->
                        val columns = mutableListOf<Column>()
                        val rows = mutableListOf<Row>()
                        val metaData = resultSet.metaData
                        val columnCount = metaData.columnCount
                        for (i in 1..columnCount) {
                            columns.add(Column(metaData.getColumnName(i), metaData.getColumnTypeName(i)))
                        }
                        while (resultSet.next()) {
                            val values = mutableListOf<Any?>()
                            for (i in 1..columnCount) {
                                values.add(resultSet.getObject(i))
                            }
                            rows.add(Row(values))
                        }
                        val executionTime = System.currentTimeMillis() - startTime
                        Result.success(QueryResult(columns, rows, rows.size, executionTime))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getTableStats(tableName: String, schema: String?): Result<TableStats> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                when (context.config.type) {
                    DatabaseType.MYSQL -> {
                        val catalog = conn.catalog
                        val sql = """
                            SELECT TABLE_ROWS, DATA_LENGTH, INDEX_LENGTH, AUTO_INCREMENT, CREATE_TIME, UPDATE_TIME
                            FROM information_schema.TABLES
                            WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
                        """
                        withPreparedStatement(
                            conn,
                            sql,
                            binder = { ps ->
                                ps.setString(1, catalog)
                                ps.setString(2, tableName)
                            }
                        ) { ps ->
                            ps.executeQuery().useAndClose { resultSet ->
                                if (resultSet.next()) {
                                    Result.success(
                                        TableStats(
                                            tableName = tableName,
                                            rowCount = resultSet.getLong("TABLE_ROWS"),
                                            dataSize = resultSet.getLong("DATA_LENGTH"),
                                            indexSize = resultSet.getLong("INDEX_LENGTH"),
                                            autoIncrementValue = resultSet.getObject("AUTO_INCREMENT") as? Long,
                                            createTime = resultSet.getString("CREATE_TIME"),
                                            updateTime = resultSet.getString("UPDATE_TIME")
                                        )
                                    )
                                } else {
                                    Result.failure(IllegalArgumentException("表 $tableName 不存在"))
                                }
                            }
                        }
                    }
                    DatabaseType.POSTGRESQL -> {
                        val targetSchema = schema ?: context.config.schema ?: "public"
                        val sql = """
                            SELECT
                                COALESCE(s.n_live_tup, 0) as row_count,
                                pg_relation_size(c.oid) as data_size,
                                pg_indexes_size(c.oid) as index_size,
                                s.last_vacuum::text as create_time,
                                s.last_autovacuum::text as update_time
                            FROM pg_class c
                            LEFT JOIN pg_stat_user_tables s ON s.relid = c.oid
                            JOIN pg_namespace n ON n.oid = c.relnamespace
                            WHERE c.relname = ? AND n.nspname = ? AND c.relkind = 'r'
                        """
                        withPreparedStatement(
                            conn,
                            sql,
                            binder = { ps ->
                                ps.setString(1, tableName)
                                ps.setString(2, targetSchema)
                            }
                        ) { ps ->
                            ps.executeQuery().useAndClose { resultSet ->
                                if (resultSet.next()) {
                                    Result.success(
                                        TableStats(
                                            tableName = tableName,
                                            rowCount = resultSet.getLong("row_count"),
                                            dataSize = resultSet.getLong("data_size"),
                                            indexSize = resultSet.getLong("index_size"),
                                            autoIncrementValue = null,
                                            createTime = resultSet.getString("create_time"),
                                            updateTime = resultSet.getString("update_time")
                                        )
                                    )
                                } else {
                                    Result.failure(IllegalArgumentException("表 $tableName 不存在"))
                                }
                            }
                        }
                    }
                    else -> Result.failure(IllegalArgumentException("不支持的数据库类型"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
