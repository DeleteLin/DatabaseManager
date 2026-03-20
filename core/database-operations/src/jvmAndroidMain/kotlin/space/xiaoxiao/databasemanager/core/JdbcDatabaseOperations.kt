package space.xiaoxiao.databasemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

/**
 * JDBC 元数据实现
 * 仅依赖 JdbcExecutionContext 暴露的 Connection 与配置
 */
class JdbcDatabaseOperations(
    private val context: JdbcExecutionContext
) : RelationalMetadataOperations {

    private inline fun <C : AutoCloseable, R> C.useAndClose(block: (C) -> R): R {
        try {
            return block(this)
        } finally {
            try {
                close()
            } catch (_: Exception) {
            }
        }
    }

    private inline fun <R> withStatement(conn: Connection, block: (Statement) -> R): R =
        conn.createStatement().useAndClose(block)

    private inline fun <R> withPreparedStatement(
        conn: Connection,
        sql: String,
        binder: (PreparedStatement) -> Unit = {},
        block: (PreparedStatement) -> R
    ): R = conn.prepareStatement(sql).useAndClose { ps ->
        binder(ps)
        block(ps)
    }

    // 兼容旧实现中大量对 connection/currentConfig 的直接引用
    private val connection: Connection?
        get() = context.connection

    private val currentConfig: DatabaseConfig?
        get() = context.config

    override suspend fun listTables(schema: String?): Result<List<TableInfo>> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val metaData = conn.metaData
                val dbType = context.config.type

                // PostgreSQL 使用 schema，MySQL 使用 catalog（database）
                if (dbType == DatabaseType.POSTGRESQL) {
                    // PostgreSQL: schema 参数优先，否则使用配置的 schema 或默认 public
                    val targetSchema = schema ?: context.config.schema ?: "public"
                    val resultSet = metaData.getTables(
                        null,  // catalog - PostgreSQL 不使用
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
                    // MySQL: 使用 catalog（database）
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
                    // PostgreSQL: 使用 schema
                    val targetSchema = schema ?: context.config.schema ?: "public"

                    // 获取列信息
                    val columnsResultSet = conn.metaData.getColumns(
                        null,  // catalog
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

                    // 获取主键
                    val primaryKeysResultSet = conn.metaData.getPrimaryKeys(
                        null,  // catalog
                        targetSchema,
                        tableName
                    )
                    val primaryKeys = mutableListOf<String>()
                    while (primaryKeysResultSet.next()) {
                        primaryKeys.add(primaryKeysResultSet.getString("COLUMN_NAME"))
                    }
                    primaryKeysResultSet.close()

                    // 获取外键
                    val foreignKeysResultSet = conn.metaData.getImportedKeys(
                        null,  // catalog
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

                    // 获取索引
                    val indexesResultSet = conn.metaData.getIndexInfo(
                        null,  // catalog
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

                    // 填充索引列
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
                    // MySQL: 使用 catalog
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
                    // PostgreSQL: 使用 schema.table
                    val targetSchema = schema ?: currentConfig?.schema ?: "public"
                    "SELECT * FROM ${quoteIdentifier(targetSchema)}.${quoteIdentifier(tableName)} LIMIT $limit OFFSET $offset"
                } else {
                    // MySQL: 直接使用 table
                    "SELECT * FROM ${quoteIdentifier(tableName)} LIMIT $limit OFFSET $offset"
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

    override suspend fun beginTransaction(): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            conn.autoCommit = false
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun commitTransaction(): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = connection
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
        val conn = connection
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

    override suspend fun createTable(definition: TableDefinition, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = context.config.type
                val columnDefs = definition.columns.joinToString(",\n    ") { col ->
                    buildColumnDefinition(col, definition.primaryKeys.contains(col.name))
                }
                val pkClause = if (definition.primaryKeys.isNotEmpty()) {
                    ",\n    PRIMARY KEY (${definition.primaryKeys.joinToString(", ") { quoteIdentifier(it) }})"
                } else ""
                val ifNotExistsClause = if (definition.ifNotExists) "IF NOT EXISTS " else ""

                val tableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                    "${quoteIdentifier(schema)}.${quoteIdentifier(definition.name)}"
                } else {
                    quoteIdentifier(definition.name)
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
                    "${quoteIdentifier(schema)}.${quoteIdentifier(tableName)}"
                } else {
                    quoteIdentifier(tableName)
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
                    DatabaseType.MYSQL -> "RENAME TABLE ${quoteIdentifier(oldName)} TO ${quoteIdentifier(newName)}"
                    DatabaseType.POSTGRESQL -> {
                        val targetSchema = schema ?: currentConfig?.schema ?: "public"
                        "ALTER TABLE ${quoteIdentifier(targetSchema)}.${quoteIdentifier(oldName)} RENAME TO ${quoteIdentifier(newName)}"
                    }
                    else -> "ALTER TABLE ${quoteIdentifier(oldName)} RENAME TO ${quoteIdentifier(newName)}"
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
                    "${quoteIdentifier(schema)}.${quoteIdentifier(tableName)}"
                } else {
                    quoteIdentifier(tableName)
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

    override suspend fun addColumn(tableName: String, column: ColumnDefinition, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = context.connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = currentConfig?.type
                val columnDef = buildColumnDefinition(column, false)
                val fullTableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                    "${quoteIdentifier(schema)}.${quoteIdentifier(tableName)}"
                } else {
                    quoteIdentifier(tableName)
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
            val conn = connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = currentConfig?.type
                val fullTableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                    "${quoteIdentifier(schema)}.${quoteIdentifier(tableName)}"
                } else {
                    quoteIdentifier(tableName)
                }

                val sql = when (dbType) {
                    DatabaseType.MYSQL -> {
                        // 字符集子句（仅文本类型）- 必须紧跟在数据类型之后
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
                            "CHANGE COLUMN ${quoteIdentifier(modification.oldName)}"
                        } else {
                            "MODIFY COLUMN"
                        }
                        "ALTER TABLE ${fullTableName} $changeClause ${quoteIdentifier(modification.newName)} ${modification.typeName} $charsetClause $nullableClause $defaultClause $commentClause".trim()
                    }
                    DatabaseType.POSTGRESQL -> {
                        val statements = mutableListOf<String>()
                        if (modification.oldName != null && modification.oldName != modification.newName) {
                            statements.add("ALTER TABLE ${fullTableName} RENAME COLUMN ${quoteIdentifier(modification.oldName)} TO ${quoteIdentifier(modification.newName)}")
                        }
                        statements.add("ALTER TABLE ${fullTableName} ALTER COLUMN ${quoteIdentifier(modification.newName)} TYPE ${modification.typeName}")
                        statements.add("ALTER TABLE ${fullTableName} ALTER COLUMN ${quoteIdentifier(modification.newName)} ${if (modification.isNullable) "DROP NOT NULL" else "SET NOT NULL"}")
                        if (modification.defaultValue != null) {
                            val clause = renderDefaultValueClause(modification.typeName, modification.defaultValue, dbType)
                            statements.add("ALTER TABLE ${fullTableName} ALTER COLUMN ${quoteIdentifier(modification.newName)} SET $clause")
                        } else {
                            statements.add("ALTER TABLE ${fullTableName} ALTER COLUMN ${quoteIdentifier(modification.newName)} DROP DEFAULT")
                        }
                        statements.joinToString("; ")
                    }
                    else -> "ALTER TABLE ${fullTableName} MODIFY COLUMN ${quoteIdentifier(modification.newName)} ${modification.typeName}"
                }
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun dropColumn(tableName: String, columnName: String, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = currentConfig?.type
                val fullTableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                    "${quoteIdentifier(schema)}.${quoteIdentifier(tableName)}"
                } else {
                    quoteIdentifier(tableName)
                }
                val sql = "ALTER TABLE ${fullTableName} DROP COLUMN ${quoteIdentifier(columnName)}"
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getIndexes(tableName: String, schema: String?): Result<List<IndexInfo>> =
        withContext(Dispatchers.IO) {
            val conn = connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val indexes = mutableListOf<IndexInfo>()
                val dbType = currentConfig?.type

                when (dbType) {
                    DatabaseType.MYSQL -> {
                        val sql = "SHOW INDEX FROM ${quoteIdentifier(tableName)}"
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
                        val targetSchema = schema ?: currentConfig?.schema ?: "public"
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
        val conn = connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            val dbType = currentConfig?.type
            val uniqueClause = if (isUnique) "UNIQUE " else ""
            val columnsClause = columns.joinToString(", ") { quoteIdentifier(it) }
            val fullTableName = if (dbType == DatabaseType.POSTGRESQL && schema != null) {
                "${quoteIdentifier(schema)}.${quoteIdentifier(tableName)}"
            } else {
                quoteIdentifier(tableName)
            }
            val sql = "CREATE ${uniqueClause}INDEX ${quoteIdentifier(indexName)} ON ${fullTableName} ($columnsClause)"
            withStatement(conn) { it.execute(sql) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun dropIndex(tableName: String, indexName: String, schema: String?): Result<Unit> =
        withContext(Dispatchers.IO) {
            val conn = connection
                ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
            try {
                val dbType = currentConfig?.type
                val sql = when (dbType) {
                    DatabaseType.MYSQL -> "DROP INDEX ${quoteIdentifier(indexName)} ON ${quoteIdentifier(tableName)}"
                    DatabaseType.POSTGRESQL -> "DROP INDEX ${quoteIdentifier(indexName)}"
                    else -> "DROP INDEX ${quoteIdentifier(indexName)}"
                }
                withStatement(conn) { it.execute(sql) }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getTableStats(tableName: String, schema: String?): Result<TableStats> =
        withContext(Dispatchers.IO) {
            val conn = connection
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
                        val targetSchema = schema ?: currentConfig?.schema ?: "public"
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

    override suspend fun getDatabaseSize(): Result<Long> = withContext(Dispatchers.IO) {
            val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            when (context.config.type) {
                DatabaseType.MYSQL -> {
                    val sql = "SELECT SUM(DATA_LENGTH + INDEX_LENGTH) FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?"
                    withPreparedStatement(
                        conn,
                        sql,
                        binder = { ps -> ps.setString(1, conn.catalog) }
                    ) { ps ->
                        ps.executeQuery().useAndClose { resultSet ->
                            if (resultSet.next()) Result.success(resultSet.getLong(1)) else Result.success(0L)
                        }
                    }
                }
                DatabaseType.POSTGRESQL -> {
                    val sql = "SELECT pg_database_size(current_database())"
                    withStatement(conn) { statement ->
                        statement.executeQuery(sql).useAndClose { resultSet ->
                            if (resultSet.next()) Result.success(resultSet.getLong(1)) else Result.success(0L)
                        }
                    }
                }
                else -> Result.success(0L)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildJdbcUrl(config: DatabaseConfig): String {
        return when (config.type) {
            DatabaseType.MYSQL -> {
                val baseUrl = "jdbc:mysql://${config.host}:${config.port}/${config.database}"
                val params = listOf(
                    "useSSL=false",
                    "allowPublicKeyRetrieval=true",
                    "serverTimezone=UTC"
                ).joinToString("&")
                "$baseUrl?$params"
            }
            DatabaseType.POSTGRESQL -> "jdbc:postgresql://${config.host}:${config.port}/${config.database}"
            else -> throw UnsupportedOperationException("JDBC does not support ${config.type} database type")
        }
    }

    private fun getDriverClass(type: DatabaseType): String {
        return when (type) {
            DatabaseType.MYSQL -> "com.mysql.cj.jdbc.Driver"
            DatabaseType.POSTGRESQL -> "org.postgresql.Driver"
            else -> throw UnsupportedOperationException("JDBC does not support $type database type")
        }
    }

    private fun quoteIdentifier(name: String): String {
        return when (currentConfig?.type) {
            DatabaseType.MYSQL -> {
                // MySQL: 反引号需要翻倍转义，例如字段名 ccc` 变成 `ccc``
                "`${name.replace("`", "``")}`"
            }
            DatabaseType.POSTGRESQL -> {
                // PostgreSQL: 双引号需要翻倍转义，例如字段名 ccc" 变成 "ccc""
                val escapedName = name.replace("\"", "\"\"")
                "\"$escapedName\""
            }
            else -> "`$name`"
        }
    }

    private fun escapeSqlStringLiteral(value: String): String {
        // SQL 标准：单引号通过翻倍转义。这里不做反斜杠转义，避免受 SQL_MODE 影响。
        return value.replace("'", "''")
    }

    private fun renderDefaultValueClause(
        typeName: String,
        rawDefaultValue: String?,
        dbType: DatabaseType?
    ): String {
        val v = rawDefaultValue?.trim().orEmpty()
        if (v.isEmpty()) return ""

        val normalized = v.uppercase()
        if (normalized == "NULL") return "DEFAULT NULL"

        // 用户已输入单引号字面量：保持语义但强制安全转义
        if (v.length >= 2 && v.first() == '\'' && v.last() == '\'') {
            val inner = v.substring(1, v.length - 1)
            return "DEFAULT '${escapeSqlStringLiteral(inner)}'"
        }

        // 数字字面量
        if (v.matches(Regex("^-?\\d+(\\.\\d+)?$"))) return "DEFAULT $v"

        // 布尔字面量（兼容 MySQL/Postgres）
        if (normalized == "TRUE" || normalized == "FALSE") return "DEFAULT $normalized"
        if (v == "1" || v == "0") return "DEFAULT $v"

        // 常见安全关键字/函数（限制字符，避免把任意 SQL 片段放行）
        val dangerous = Regex("[;\\n\\r]|--|/\\*|\\*/")
        val safeExpr = Regex("^[A-Za-z_][A-Za-z0-9_]*(\\([^\\)]*\\))?$")
        if (!dangerous.containsMatchIn(v) && safeExpr.matches(v)) {
            val allow = setOf(
                "CURRENT_TIMESTAMP",
                "CURRENT_DATE",
                "CURRENT_TIME",
                "NOW()",
                "LOCALTIME",
                "LOCALTIMESTAMP",
                "UUID()"
            )
            val key = normalized
            val keyFn = normalized.replace("\\s+".toRegex(), "")
            if (key in allow || keyFn in allow) return "DEFAULT $v"
        }

        // 其他按字符串字面量处理
        return "DEFAULT '${escapeSqlStringLiteral(v)}'"
    }

    private fun requireSafeCharsetName(charset: String) {
        require(charset.isNotBlank()) { "charset 不能为空" }
        require(charset.matches(Regex("^[A-Za-z0-9_]+$"))) { "非法 charset：$charset" }
    }

    private fun buildColumnDefinition(column: ColumnDefinition, isPrimaryKey: Boolean): String {
        // 字符集子句（仅 MySQL 文本类型）- 必须紧跟在数据类型之后
        val charsetClause = if (column.charset != null && currentConfig?.type == DatabaseType.MYSQL &&
            column.typeName.uppercase() in listOf("VARCHAR", "CHAR", "TEXT", "LONGTEXT", "MEDIUMTEXT", "TINYTEXT")) {
            requireSafeCharsetName(column.charset)
            "CHARACTER SET ${column.charset}"
        } else ""

        val nullClause = if (column.isNullable) "NULL" else "NOT NULL"
        val defaultClause = renderDefaultValueClause(column.typeName, column.defaultValue, currentConfig?.type)
        val autoIncrementClause = if (column.isAutoIncrement && currentConfig?.type == DatabaseType.MYSQL) "AUTO_INCREMENT" else ""
        val commentClause = if (column.comment != null && currentConfig?.type == DatabaseType.MYSQL) {
            "COMMENT '${escapeSqlStringLiteral(column.comment)}'"
        } else ""
        val pkClause = if (isPrimaryKey && !column.isAutoIncrement) "PRIMARY KEY" else ""

        return "${quoteIdentifier(column.name)} ${column.typeName} $charsetClause $nullClause $defaultClause $autoIncrementClause $pkClause $commentClause".trim()
            .replace("  +".toRegex(), " ")
    }

    // ==================== 数据库管理 ====================

    override suspend fun listDatabases(): Result<List<String>> = withContext(Dispatchers.IO) {
            val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            val dbType = context.config.type
            when (dbType) {
                DatabaseType.MYSQL -> {
                    // MySQL: SHOW DATABASES
                    withStatement(conn) { statement ->
                        statement.executeQuery("SHOW DATABASES").useAndClose { resultSet ->
                            val databases = mutableListOf<String>()
                            while (resultSet.next()) {
                                val dbName = resultSet.getString(1)
                                // 排除系统数据库
                                if (dbName !in listOf("information_schema", "mysql", "performance_schema", "sys")) {
                                    databases.add(dbName)
                                }
                            }
                            Result.success(databases)
                        }
                    }
                }
                DatabaseType.POSTGRESQL -> {
                    // PostgreSQL: SELECT datname FROM pg_database WHERE datistemplate = false
                    withStatement(conn) { statement ->
                        statement.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY datname")
                            .useAndClose { resultSet ->
                                val databases = mutableListOf<String>()
                                while (resultSet.next()) {
                                    databases.add(resultSet.getString("datname"))
                                }
                                Result.success(databases)
                            }
                    }
                }
                else -> Result.failure(IllegalArgumentException("不支持的数据库类型：$dbType"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun switchDatabase(database: String): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = context.connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            val dbType = context.config.type
            when (dbType) {
                DatabaseType.MYSQL -> {
                    // MySQL: USE database_name
                    withStatement(conn) { it.execute("USE ${quoteIdentifier(database)}") }
                    // 更新当前 database 状态
                    context.currentDatabase = database
                    Result.success(Unit)
                }
                DatabaseType.POSTGRESQL -> {
                    // PostgreSQL 需要重新建立连接（不同数据库）
                    val newConfig = context.config.copy(database = database)

                    // 断开当前连接
                    try { conn.close() } catch (_: Exception) {}

                    // 建立新连接并更新 context
                    val jdbcUrl = "jdbc:postgresql://${newConfig.host}:${newConfig.port}/${newConfig.database}"
                    try {
                        Class.forName("org.postgresql.Driver")
                        context.connection = DriverManager.getConnection(jdbcUrl, newConfig.username, newConfig.password)
                        context.currentDatabase = database
                        Result.success(Unit)
                    } catch (e: Exception) {
                        Result.failure(Exception("切换到数据库 $database 失败：${e.message}"))
                    }
                }
                else -> Result.failure(IllegalArgumentException("不支持的数据库类型：$dbType"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentDatabase(): Result<String> = withContext(Dispatchers.IO) {
        val conn = connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))
        try {
            val dbType = currentConfig?.type
            when (dbType) {
                DatabaseType.MYSQL -> {
                    // MySQL: SELECT DATABASE()
                    withStatement(conn) { statement ->
                        statement.executeQuery("SELECT DATABASE() as current_db").useAndClose { resultSet ->
                            if (resultSet.next()) {
                                Result.success(resultSet.getString("current_db") ?: currentConfig?.database ?: "")
                            } else {
                                Result.success(currentConfig?.database ?: "")
                            }
                        }
                    }
                }
                DatabaseType.POSTGRESQL -> {
                    // PostgreSQL: SELECT current_database()
                    withStatement(conn) { statement ->
                        statement.executeQuery("SELECT current_database() as current_db").useAndClose { resultSet ->
                            if (resultSet.next()) {
                                Result.success(resultSet.getString("current_db") ?: currentConfig?.database ?: "")
                            } else {
                                Result.success(currentConfig?.database ?: "")
                            }
                        }
                    }
                }
                else -> Result.success(currentConfig?.database ?: "")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDatabase(name: String, charset: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))

        // 验证数据库名称
        if (name.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("数据库名称不能为空"))
        }

        if (!name.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))) {
            return@withContext Result.failure(IllegalArgumentException("数据库名称只能包含字母、数字和下划线，且必须以字母或下划线开头"))
        }

        try {
            val dbType = currentConfig?.type
            when (dbType) {
                DatabaseType.MYSQL -> {
                    // MySQL: CREATE DATABASE IF NOT EXISTS `name` [DEFAULT CHARACTER SET charset]
                    val charsetClause = if (charset != null) {
                        requireSafeCharsetName(charset)
                        " DEFAULT CHARACTER SET ${charset}"
                    } else {
                        ""
                    }
                    withStatement(conn) { it.execute("CREATE DATABASE IF NOT EXISTS ${quoteIdentifier(name)}$charsetClause") }
                    Result.success(Unit)
                }
                DatabaseType.POSTGRESQL -> {
                    // PostgreSQL: CREATE DATABASE "name" [ENCODING 'charset'] [TEMPLATE template0]
                    // 使用 template0 模板可以绕过 locale 限制
                    val encodingClause = if (charset != null) {
                        requireSafeCharsetName(charset)
                        " ENCODING '${escapeSqlStringLiteral(charset)}' TEMPLATE template0 LC_COLLATE 'C' LC_CTYPE 'C'"
                    } else {
                        ""
                    }
                    withStatement(conn) { it.execute("CREATE DATABASE ${quoteIdentifier(name)}$encodingClause") }
                    Result.success(Unit)
                }
                else -> Result.failure(IllegalArgumentException("不支持的数据库类型：$dbType"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("创建数据库失败：${e.message}"))
        }
    }

    override suspend fun dropDatabase(name: String): Result<Unit> = withContext(Dispatchers.IO) {
        val conn = connection
            ?: return@withContext Result.failure(IllegalStateException("未连接到数据库"))

        // 验证数据库名称
        if (name.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("数据库名称不能为空"))
        }

        if (!name.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))) {
            return@withContext Result.failure(IllegalArgumentException("数据库名称只能包含字母、数字和下划线，且必须以字母或下划线开头"))
        }

        try {
            val dbType = currentConfig?.type
            when (dbType) {
                DatabaseType.MYSQL -> {
                    // MySQL: DROP DATABASE `name`
                    withStatement(conn) { it.execute("DROP DATABASE ${quoteIdentifier(name)}") }
                    Result.success(Unit)
                }
                DatabaseType.POSTGRESQL -> {
                    // PostgreSQL: DROP DATABASE "name"
                    // 注意：PostgreSQL 的 DROP DATABASE 不能在事务块中执行
                    withStatement(conn) { it.execute("DROP DATABASE ${quoteIdentifier(name)}") }
                    Result.success(Unit)
                }
                else -> Result.failure(IllegalArgumentException("不支持的数据库类型：$dbType"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("删除数据库失败：${e.message}"))
        }
    }

}
