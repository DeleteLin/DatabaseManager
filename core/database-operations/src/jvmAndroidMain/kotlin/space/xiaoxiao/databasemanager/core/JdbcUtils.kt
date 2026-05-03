package space.xiaoxiao.databasemanager.core

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

// ===================================================================
// JDBC 通用工具函数 — 由所有 Jdbc 处理类共享
// ===================================================================

internal inline fun <C : AutoCloseable, R> C.useAndClose(block: (C) -> R): R {
    try {
        return block(this)
    } finally {
        try {
            close()
        } catch (_: Exception) {
        }
    }
}

internal inline fun <R> withStatement(conn: Connection, block: (Statement) -> R): R =
    conn.createStatement().useAndClose(block)

internal inline fun <R> withPreparedStatement(
    conn: Connection,
    sql: String,
    binder: (PreparedStatement) -> Unit = {},
    block: (PreparedStatement) -> R
): R = conn.prepareStatement(sql).useAndClose { ps ->
    binder(ps)
    block(ps)
}

// ===================================================================
// SQL 标识符与字符串工具
// ===================================================================

internal fun quoteIdentifier(name: String, dbType: DatabaseType?): String {
    return when (dbType) {
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

internal fun escapeSqlStringLiteral(value: String): String {
    // SQL 标准：单引号通过翻倍转义。这里不做反斜杠转义，避免受 SQL_MODE 影响。
    return value.replace("'", "''")
}

internal fun requireSafeCharsetName(charset: String) {
    require(charset.isNotBlank()) { "charset 不能为空" }
    require(charset.matches(Regex("^[A-Za-z0-9_]+$"))) { "非法 charset：$charset" }
}

// ===================================================================
// SQL 片段构建
// ===================================================================

internal fun renderDefaultValueClause(
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

internal fun buildColumnDefinition(
    column: ColumnDefinition,
    isPrimaryKey: Boolean,
    dbType: DatabaseType?
): String {
    // 字符集子句（仅 MySQL 文本类型）- 必须紧跟在数据类型之后
    val charsetClause = if (column.charset != null && dbType == DatabaseType.MYSQL &&
        column.typeName.uppercase() in listOf("VARCHAR", "CHAR", "TEXT", "LONGTEXT", "MEDIUMTEXT", "TINYTEXT")
    ) {
        requireSafeCharsetName(column.charset)
        "CHARACTER SET ${column.charset}"
    } else ""

    val nullClause = if (column.isNullable) "NULL" else "NOT NULL"
    val defaultClause = renderDefaultValueClause(column.typeName, column.defaultValue, dbType)
    val autoIncrementClause = if (column.isAutoIncrement && dbType == DatabaseType.MYSQL) "AUTO_INCREMENT" else ""
    val commentClause = if (column.comment != null && dbType == DatabaseType.MYSQL) {
        "COMMENT '${escapeSqlStringLiteral(column.comment)}'"
    } else ""
    val pkClause = if (isPrimaryKey && !column.isAutoIncrement) "PRIMARY KEY" else ""

    return "${quoteIdentifier(column.name, dbType)} ${column.typeName} $charsetClause $nullClause $defaultClause $autoIncrementClause $pkClause $commentClause".trim()
        .replace("  +".toRegex(), " ")
}
