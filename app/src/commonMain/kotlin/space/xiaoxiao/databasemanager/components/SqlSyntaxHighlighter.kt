package space.xiaoxiao.databasemanager.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * SQL 语法高亮器
 * 支持 SQL 关键词、字符串、注释、数字等的高亮显示
 */
object SqlSyntaxHighlighter {

    // SQL 关键词列表
    val keywords = setOf(
        // DML
        "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "VALUES", "SET", "INTO",
        // DDL
        "CREATE", "DROP", "ALTER", "TABLE", "INDEX", "VIEW", "DATABASE", "SCHEMA",
        "TRUNCATE", "RENAME", "ADD", "COLUMN", "CONSTRAINT",
        // DCL
        "GRANT", "REVOKE",
        // JOIN
        "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "FULL", "CROSS", "ON", "USING",
        // 条件
        "AND", "OR", "NOT", "IN", "LIKE", "BETWEEN", "EXISTS", "IS", "NULL",
        "CASE", "WHEN", "THEN", "ELSE", "END",
        // 排序和分组
        "ORDER", "BY", "GROUP", "HAVING", "LIMIT", "OFFSET", "ASC", "DESC",
        // 聚合函数
        "COUNT", "SUM", "AVG", "MIN", "MAX", "DISTINCT",
        // 约束
        "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "UNIQUE", "CHECK", "DEFAULT",
        // 数据类型
        "INT", "INTEGER", "BIGINT", "SMALLINT", "TINYINT",
        "VARCHAR", "CHAR", "TEXT", "LONGTEXT", "MEDIUMTEXT",
        "DECIMAL", "NUMERIC", "FLOAT", "DOUBLE", "REAL",
        "DATE", "DATETIME", "TIMESTAMP", "TIME", "YEAR",
        "BOOLEAN", "BOOL", "BLOB", "JSON",
        // 其他
        "AS", "FROM", "TO", "WITH", "RECURSIVE", "UNION", "ALL", "INTERSECT", "EXCEPT",
        "AUTO_INCREMENT", "AUTOINCREMENT", "IDENTITY",
        "ENGINE", "CHARSET", "COLLATE", "COMMENT",
        "CASCADE", "RESTRICT", "NO", "ACTION",
        "TRUE", "FALSE", "UNKNOWN"
    )

    // MySQL 特定函数
    val functions = setOf(
        "IF", "IFNULL", "NULLIF", "COALESCE", "CONCAT", "CONCAT_WS",
        "SUBSTRING", "SUBSTR", "LENGTH", "CHAR_LENGTH", "UPPER", "LOWER", "TRIM", "LTRIM", "RTRIM",
        "REPLACE", "REVERSE", "LEFT", "RIGHT", "LPAD", "RPAD", "INSTR", "LOCATE", "POSITION",
        "NOW", "CURDATE", "CURTIME", "DATE_FORMAT", "STR_TO_DATE", "DATEDIFF", "DATE_ADD", "DATE_SUB",
        "YEAR", "MONTH", "DAY", "HOUR", "MINUTE", "SECOND", "WEEK", "QUARTER",
        "ABS", "CEIL", "CEILING", "FLOOR", "ROUND", "TRUNCATE", "MOD", "POW", "POWER", "SQRT",
        "RAND", "SIGN", "PI",
        "CAST", "CONVERT", "BINARY",
        "GROUP_CONCAT", "FIND_IN_SET", "FIELD", "ELT",
        "ROW_NUMBER", "RANK", "DENSE_RANK", "LAG", "LEAD", "FIRST_VALUE", "LAST_VALUE",
        "UUID", "MD5", "SHA1", "SHA2", "AES_ENCRYPT", "AES_DECRYPT"
    )

    /**
     * 对 SQL 代码进行语法高亮
     * @param code SQL 代码字符串
     * @param colors 颜色配置
     * @return 带有样式注释的字符串
     */
    fun highlight(code: String, colors: CodeEditorColors): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            var currentIndex = 0
            val chars = code.toCharArray()

            while (currentIndex < chars.size) {
                when {
                    // 处理单行注释 (-- 或 #)
                    isSingleLineCommentStart(chars, currentIndex) -> {
                        val endIndex = findSingleLineCommentEnd(chars, currentIndex)
                        appendWithStyle(code.substring(currentIndex, endIndex), colors.commentStyle)
                        currentIndex = endIndex
                    }

                    // 处理多行注释 (/* */)
                    isMultiLineCommentStart(chars, currentIndex) -> {
                        val endIndex = findMultiLineCommentEnd(chars, currentIndex)
                        appendWithStyle(code.substring(currentIndex, endIndex), colors.commentStyle)
                        currentIndex = endIndex
                    }

                    // 处理字符串字面量
                    chars[currentIndex] == '\'' || chars[currentIndex] == '"' -> {
                        val endIndex = findStringEnd(chars, currentIndex)
                        appendWithStyle(code.substring(currentIndex, endIndex), colors.stringStyle)
                        currentIndex = endIndex
                    }

                    // 处理反引号标识符
                    chars[currentIndex] == '`' -> {
                        val endIndex = findBacktickEnd(chars, currentIndex)
                        appendWithStyle(code.substring(currentIndex, endIndex), colors.identifierStyle)
                        currentIndex = endIndex
                    }

                    // 处理数字
                    isDigit(chars[currentIndex]) || (chars[currentIndex] == '-' && currentIndex + 1 < chars.size && isDigit(chars[currentIndex + 1])) -> {
                        val endIndex = findNumberEnd(chars, currentIndex)
                        appendWithStyle(code.substring(currentIndex, endIndex), colors.numberStyle)
                        currentIndex = endIndex
                    }

                    // 处理关键词和标识符
                    isIdentifierStart(chars[currentIndex]) -> {
                        val endIndex = findIdentifierEnd(chars, currentIndex)
                        val word = code.substring(currentIndex, endIndex)
                        val upperWord = word.uppercase()

                        when {
                            keywords.contains(upperWord) -> {
                                appendWithStyle(word, colors.keywordStyle)
                            }
                            functions.contains(upperWord) -> {
                                appendWithStyle(word, colors.functionStyle)
                            }
                            else -> {
                                append(word)
                            }
                        }
                        currentIndex = endIndex
                    }

                    // 处理运算符和分隔符
                    isOperator(chars[currentIndex]) -> {
                        appendWithStyle(chars[currentIndex].toString(), colors.operatorStyle)
                        currentIndex++
                    }

                    // 其他字符
                    else -> {
                        append(chars[currentIndex])
                        currentIndex++
                    }
                }
            }
        }
    }

    private fun appendWithStyle(text: String, style: SpanStyle, builder: AnnotatedString.Builder) {
        builder.withStyle(style) {
            append(text)
        }
    }

    private fun AnnotatedString.Builder.appendWithStyle(text: String, style: SpanStyle) {
        withStyle(style) {
            append(text)
        }
    }

    private fun isSingleLineCommentStart(chars: CharArray, index: Int): Boolean {
        if (index >= chars.size) return false
        // MySQL 风格 -- 注释 (后面需要空格) 或 # 注释
        if (chars[index] == '#') return true
        if (chars[index] == '-' && index + 1 < chars.size && chars[index + 1] == '-') {
            // 确认后面是空格或行尾
            return index + 2 >= chars.size || chars[index + 2].isWhitespace()
        }
        return false
    }

    private fun findSingleLineCommentEnd(chars: CharArray, startIndex: Int): Int {
        var index = startIndex
        while (index < chars.size && chars[index] != '\n') {
            index++
        }
        return index
    }

    private fun isMultiLineCommentStart(chars: CharArray, index: Int): Boolean {
        return index + 1 < chars.size && chars[index] == '/' && chars[index + 1] == '*'
    }

    private fun findMultiLineCommentEnd(chars: CharArray, startIndex: Int): Int {
        var index = startIndex + 2
        while (index + 1 < chars.size) {
            if (chars[index] == '*' && chars[index + 1] == '/') {
                return index + 2
            }
            index++
        }
        return chars.size
    }

    private fun findStringEnd(chars: CharArray, startIndex: Int): Int {
        val quote = chars[startIndex]
        var index = startIndex + 1
        while (index < chars.size) {
            if (chars[index] == quote) {
                // 检查是否是转义的引号 (两个连续的引号)
                if (index + 1 < chars.size && chars[index + 1] == quote) {
                    index += 2
                    continue
                }
                return index + 1
            }
            index++
        }
        return chars.size
    }

    private fun findBacktickEnd(chars: CharArray, startIndex: Int): Int {
        var index = startIndex + 1
        while (index < chars.size) {
            if (chars[index] == '`') {
                return index + 1
            }
            index++
        }
        return chars.size
    }

    private fun findNumberEnd(chars: CharArray, startIndex: Int): Int {
        var index = startIndex
        var hasDecimal = false
        var hasExponent = false

        // 处理负号
        if (chars[index] == '-') index++

        while (index < chars.size) {
            when {
                isDigit(chars[index]) -> index++
                chars[index] == '.' && !hasDecimal -> {
                    hasDecimal = true
                    index++
                }
                (chars[index] == 'e' || chars[index] == 'E') && !hasExponent -> {
                    hasExponent = true
                    index++
                    // 科学计数法符号后的正负号
                    if (index < chars.size && (chars[index] == '+' || chars[index] == '-')) {
                        index++
                    }
                }
                else -> break
            }
        }
        return index
    }

    private fun findIdentifierEnd(chars: CharArray, startIndex: Int): Int {
        var index = startIndex
        while (index < chars.size && isIdentifierPart(chars[index])) {
            index++
        }
        return index
    }

    private fun isIdentifierStart(c: Char): Boolean {
        return c.isLetter() || c == '_'
    }

    private fun isIdentifierPart(c: Char): Boolean {
        return c.isLetterOrDigit() || c == '_'
    }

    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isOperator(c: Char): Boolean {
        return c in setOf('=', '<', '>', '!', '+', '-', '*', '/', '%', '&', '|', '^', '~', '(', ')', ',', ';', '.', ':')
    }
}

/**
 * 代码编辑器颜色配置
 */
data class CodeEditorColors(
    val keywordStyle: SpanStyle,
    val stringStyle: SpanStyle,
    val commentStyle: SpanStyle,
    val numberStyle: SpanStyle,
    val functionStyle: SpanStyle,
    val operatorStyle: SpanStyle,
    val identifierStyle: SpanStyle,
    val textStyle: SpanStyle,
    val lineNumberStyle: SpanStyle,
    val lineNumberBackground: Color
) {
    companion object {
        /**
         * 从 Material 主题颜色创建浅色主题配置
         */
        fun light(
            onSurface: Color,
            onSurfaceVariant: Color,
            primary: Color,
            secondary: Color,
            tertiary: Color,
            outline: Color
        ): CodeEditorColors {
            return CodeEditorColors(
                keywordStyle = SpanStyle(color = Color(0xFF0000FF)), // 蓝色关键词
                stringStyle = SpanStyle(color = Color(0xFF008000)), // 绿色字符串
                commentStyle = SpanStyle(color = Color(0xFF808080)), // 灰色注释
                numberStyle = SpanStyle(color = Color(0xFF800000)), // 深红色数字
                functionStyle = SpanStyle(color = Color(0xFF8B4513)), // 棕色函数
                operatorStyle = SpanStyle(color = Color(0xFF666666)), // 灰色运算符
                identifierStyle = SpanStyle(color = Color(0xFF008080)), // 青色标识符
                textStyle = SpanStyle(color = onSurface),
                lineNumberStyle = SpanStyle(color = onSurfaceVariant.copy(alpha = 0.6f)),
                lineNumberBackground = outline.copy(alpha = 0.1f)
            )
        }

        /**
         * 从 Material 主题颜色创建深色主题配置
         */
        fun dark(
            onSurface: Color,
            onSurfaceVariant: Color,
            primary: Color,
            secondary: Color,
            tertiary: Color,
            outline: Color
        ): CodeEditorColors {
            return CodeEditorColors(
                keywordStyle = SpanStyle(color = Color(0xFF569CD6)), // 蓝色关键词
                stringStyle = SpanStyle(color = Color(0xFFCE9178)), // 橙色字符串
                commentStyle = SpanStyle(color = Color(0xFF6A9955)), // 绿色注释
                numberStyle = SpanStyle(color = Color(0xFFB5CEA8)), // 浅绿色数字
                functionStyle = SpanStyle(color = Color(0xFFDCDCAA)), // 黄色函数
                operatorStyle = SpanStyle(color = Color(0xFFD4D4D4)), // 浅灰色运算符
                identifierStyle = SpanStyle(color = Color(0xFF4EC9B0)), // 青色标识符
                textStyle = SpanStyle(color = onSurface),
                lineNumberStyle = SpanStyle(color = onSurfaceVariant.copy(alpha = 0.5f)),
                lineNumberBackground = outline.copy(alpha = 0.2f)
            )
        }
    }
}

/**
 * SQL 自动补全辅助对象
 */
object SqlAutoComplete {
    /**
     * 获取所有 SQL 关键词
     */
    fun getAllKeywords(): Set<String> = SqlSyntaxHighlighter.getKeywords()

    /**
     * 获取所有 SQL 函数
     */
    fun getAllFunctions(): Set<String> = SqlSyntaxHighlighter.getFunctions()

    /**
     * 根据前缀获取建议列表
     */
    fun getSuggestions(prefix: String, limit: Int = 10): List<String> {
        if (prefix.isEmpty()) return emptyList()

        val upperPrefix = prefix.uppercase()
        val suggestions = mutableListOf<String>()

        // 从关键词中匹配
        suggestions.addAll(getAllKeywords().filter { it.startsWith(upperPrefix) })
        // 从函数中匹配
        suggestions.addAll(getAllFunctions().filter { it.startsWith(upperPrefix) })

        return suggestions.take(limit)
    }

    /**
     * 分析 SQL 上下文
     */
    fun analyzeContext(sql: String, cursorPosition: Int): SqlContext {
        val beforeCursor = sql.substring(0, cursorPosition)
        val words = beforeCursor.split(Regex("\\s+")).filter { it.isNotBlank() }

        if (words.isEmpty()) return SqlContext.Start

        val lastWord = words.last().uppercase()
        val secondLastWord = if (words.size >= 2) words[words.size - 2].uppercase() else null

        return when {
            lastWord == "SELECT" -> SqlContext.AfterSelect
            lastWord == "FROM" -> SqlContext.AfterFrom
            lastWord == "WHERE" -> SqlContext.AfterWhere
            lastWord == "JOIN" -> SqlContext.AfterJoin
            lastWord == "ON" -> SqlContext.AfterOn
            lastWord == "ORDER" -> SqlContext.AfterOrder
            lastWord == "GROUP" -> SqlContext.AfterGroup
            lastWord in setOf("AND", "OR") -> SqlContext.AfterCondition
            secondLastWord == "ORDER" && lastWord == "BY" -> SqlContext.AfterOrderBy
            secondLastWord == "GROUP" && lastWord == "BY" -> SqlContext.AfterGroupBy
            else -> SqlContext.Other
        }
    }
}

/**
 * SQL 上下文枚举
 */
enum class SqlContext {
    Start,
    AfterSelect,
    AfterFrom,
    AfterWhere,
    AfterJoin,
    AfterOn,
    AfterOrder,
    AfterGroup,
    AfterOrderBy,
    AfterGroupBy,
    AfterCondition,
    Other
}

/**
 * SQL 格式化工具
 */
object SqlFormatter {
    /**
     * 格式化 SQL 语句
     */
    fun format(sql: String, indent: String = "    "): String {
        var result = sql.trim()
        if (result.isEmpty()) return result

        // 移除多余空格
        result = result.replace(Regex("\\s+"), " ")

        // 主要关键字换行
        val keywords = listOf(
            "SELECT", "FROM", "WHERE", "JOIN", "LEFT JOIN", "RIGHT JOIN", "INNER JOIN",
            "ON", "GROUP BY", "HAVING", "ORDER BY", "LIMIT", "OFFSET",
            "INSERT INTO", "VALUES", "UPDATE", "SET", "DELETE FROM"
        )

        for (keyword in keywords.sortedByDescending { it.length }) {
            val pattern = Regex("(?i)\\b$keyword\\b")
            result = result.replace(pattern, "\n$keyword")
        }

        // 处理 SELECT 后的字段列表
        result = formatSelectFields(result, indent)

        // 移除开头的换行符
        result = result.trimStart()

        // 添加缩进
        val lines = result.split("\n")
        val formattedLines = lines.mapIndexed { index, line ->
            if (index == 0) line.trim()
            else indent + line.trim()
        }

        return formattedLines.joinToString("\n")
    }

    private fun formatSelectFields(sql: String, indent: String): String {
        val selectMatch = Regex("(?i)SELECT\\s+(.+?)\\s+FROM", RegexOption.DOT_MATCHES_ALL).find(sql)
        if (selectMatch != null) {
            val fields = selectMatch.groupValues[1]
            if (fields.contains(",")) {
                val formattedFields = fields.split(",")
                    .map { it.trim() }
                    .joinToString(",\n$indent")
                return sql.replace(selectMatch.groupValues[1], "\n$indent$formattedFields\n")
            }
        }
        return sql
    }

    /**
     * 压缩 SQL（移除多余空白）
     */
    fun compact(sql: String): String {
        return sql.trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("\\s*,\\s*"), ", ")
            .replace(Regex("\\s*\\(\\s*"), " (")
            .replace(Regex("\\s*\\)\\s*"), ") ")
    }
}

// 扩展 SqlSyntaxHighlighter 以暴露关键词和函数
private fun SqlSyntaxHighlighter.getKeywords(): Set<String> = keywords
private fun SqlSyntaxHighlighter.getFunctions(): Set<String> = functions