package space.xiaoxiao.databasemanager.features

import space.xiaoxiao.databasemanager.core.QueryResult
import space.xiaoxiao.databasemanager.core.Column
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * 导出格式枚举
 */
enum class ExportFormat {
    CSV,
    JSON,
    SQL_INSERT,
    SQL_UPDATE;

    val fileExtension: String
        get() = when (this) {
            CSV -> "csv"
            JSON -> "json"
            SQL_INSERT -> "sql"
            SQL_UPDATE -> "sql"
        }

    val mimeType: String
        get() = when (this) {
            CSV -> "text/csv"
            JSON -> "application/json"
            SQL_INSERT, SQL_UPDATE -> "application/sql"
        }
}

/**
 * 统一数据导出器接口
 */
interface DataExporter {
    val format: ExportFormat
    fun export(result: QueryResult): ByteArray
}

/**
 * CSV 导出器
 */
object CsvExporter : DataExporter {
    override val format = ExportFormat.CSV

    override fun export(result: QueryResult): ByteArray {
        return buildCsvString(result).toByteArray(Charsets.UTF_8)
    }
}

/**
 * JSON 导出器
 */
object JsonExporter : DataExporter {
    override val format = ExportFormat.JSON

    override fun export(result: QueryResult): ByteArray {
        val jsonList = result.rows.map { row ->
            result.columns.mapIndexed { index, column ->
                column.name to (row.values[index]?.toString() ?: null)
            }.toMap()
        }
        return Json { prettyPrint = true }.encodeToString(jsonList).toByteArray(Charsets.UTF_8)
    }
}

/**
 * SQL INSERT 语句导出器
 */
object SqlInsertExporter : DataExporter {
    override val format = ExportFormat.SQL_INSERT

    override fun export(result: QueryResult): ByteArray {
        if (result.rows.isEmpty()) return "".toByteArray()

        val tableName = "export_table" // 默认表名
        val columns = result.columns.map { it.name }
        val sb = StringBuilder()

        result.rows.forEach { row ->
            val values = row.values.mapIndexed { index, value ->
                formatSqlValue(value, result.columns[index])
            }
            sb.append("INSERT INTO $tableName (${columns.joinToString(", ")}) VALUES (${values.joinToString(", ")});\n")
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
    }
}

/**
 * SQL UPDATE 语句导出器
 */
object SqlUpdateExporter : DataExporter {
    override val format = ExportFormat.SQL_UPDATE

    override fun export(result: QueryResult): ByteArray {
        if (result.rows.isEmpty()) return "".toByteArray()

        val tableName = "export_table" // 默认表名
        val sb = StringBuilder()

        result.rows.forEach { row ->
            val setClause = result.columns.mapIndexed { index, column ->
                val value = formatSqlValue(row.values[index], column)
                "${column.name} = $value"
            }.joinToString(", ")

            sb.append("UPDATE $tableName SET $setClause;\n")
        }

        return sb.toString().toByteArray(Charsets.UTF_8)
    }
}

/**
 * 格式化 SQL 值
 */
private fun formatSqlValue(value: Any?, column: Column): String {
    return when (value) {
        null -> "NULL"
        is String -> "'${value.replace("'", "''")}'"
        is Number -> value.toString()
        is Boolean -> if (value) "1" else "0"
        else -> "'${value.toString().replace("'", "''")}'"
    }
}

/**
 * 导出工具对象
 */
object DataExportUtils {
    /**
     * 获取导出器
     */
    fun getExporter(format: ExportFormat): DataExporter = when (format) {
        ExportFormat.CSV -> CsvExporter
        ExportFormat.JSON -> JsonExporter
        ExportFormat.SQL_INSERT -> SqlInsertExporter
        ExportFormat.SQL_UPDATE -> SqlUpdateExporter
    }

    /**
     * 导出数据
     */
    fun export(result: QueryResult, format: ExportFormat): ByteArray {
        return getExporter(format).export(result)
    }

    /**
     * 获取导出文件名
     */
    fun getExportFileName(baseName: String, format: ExportFormat): String {
        return "${baseName}.${format.fileExtension}"
    }
}