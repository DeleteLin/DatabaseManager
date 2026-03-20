package space.xiaoxiao.databasemanager.core

/**
 * 极简、跨平台模板：
 * - 值参数：{{name}} -> SQL 侧渲染为 '?' 并按出现顺序收集参数
 * - 标识符：{{#id name}} -> SQL 侧渲染为引用后的标识符（不可参数化绑定）
 *
 * 约束：
 * - 不支持 if/each 等控制流（后续可扩展）
 * - 不支持嵌套占位符
 */
class StringTemplate private constructor(
    private val parts: List<Part>
) {
    sealed class Part {
        data class Text(val value: String) : Part()
        data class Value(val name: String) : Part()
        data class Identifier(val name: String) : Part()
    }

    fun renderSql(
        params: Map<String, DbParam>,
        dialect: SqlDialect
    ): DbCommand.PreparedSql {
        val sql = StringBuilder()
        val ordered = ArrayList<DbParam>()

        for (p in parts) {
            when (p) {
                is Part.Text -> sql.append(p.value)
                is Part.Value -> {
                    val v = params[p.name] ?: error("缺少参数：${p.name}")
                    sql.append('?')
                    ordered.add(v)
                }
                is Part.Identifier -> {
                    val v = params[p.name] ?: error("缺少标识符参数：${p.name}")
                    val raw = (v as? DbParam.Str)?.value
                        ?: error("标识符参数必须是字符串：${p.name}")
                    require(isSafeIdentifier(raw)) { "非法标识符：$raw" }
                    sql.append(dialect.quoteIdentifier(raw))
                }
            }
        }

        return DbCommand.PreparedSql(sql.toString(), ordered)
    }

    /**
     * 将模板渲染为 Redis argv。
     *
     * 规则：
     * - 仅允许“以空白分隔的 token”模型：每个 token 要么是纯文本，要么是一个占位符
     * - 不支持在一个 token 内混合文本与占位符（例如 key:{{id}}），需要写成独立 token
     */
    fun renderRedisArgv(params: Map<String, DbParam>): DbCommand.RedisArgv {
        val argv = ArrayList<DbArg>()

        var hasActiveToken = false
        var activeTokenIsParam = false
        var activeText = StringBuilder()
        var activeParam: DbParam? = null

        fun flushToken() {
            if (!hasActiveToken) return
            if (activeTokenIsParam) {
                argv.add(DbArg.Param(activeParam ?: error("Redis 模板参数异常")))
            } else {
                val t = activeText.toString()
                if (t.isNotBlank()) argv.add(DbArg.Literal(t))
            }
            hasActiveToken = false
            activeTokenIsParam = false
            activeText = StringBuilder()
            activeParam = null
        }

        for (p in parts) {
            when (p) {
                is Part.Text -> {
                    // 按空白切分，并保留分隔符语义（遇到空白就 flush 当前 token）
                    val s = p.value
                    for (ch in s) {
                        if (ch.isWhitespace()) {
                            flushToken()
                        } else {
                            if (!hasActiveToken) {
                                hasActiveToken = true
                                activeTokenIsParam = false
                            } else {
                                require(!activeTokenIsParam) {
                                    "Redis 模板不支持占位符与文本混合在同一 token 内"
                                }
                            }
                            activeText.append(ch)
                        }
                    }
                }
                is Part.Value -> {
                    val v = params[p.name] ?: error("缺少参数：${p.name}")
                    if (!hasActiveToken) {
                        hasActiveToken = true
                        activeTokenIsParam = true
                        activeParam = v
                    } else {
                        error("Redis 模板不支持占位符与文本混合在同一 token 内")
                    }
                }
                is Part.Identifier -> error("Redis 模板不支持 {{#id ...}}")
            }
        }
        flushToken()

        require(argv.isNotEmpty()) { "Redis 命令不能为空" }

        val commandToken = argv.first()
        val command = when (commandToken) {
            is DbArg.Literal -> commandToken.value
            is DbArg.Param -> (commandToken.value as? DbParam.Str)?.value
                ?: error("Redis command 必须是字符串")
        }
        return DbCommand.RedisArgv(command = command.uppercase(), args = argv.drop(1))
    }

    companion object {
        fun compile(template: String): StringTemplate {
            val parts = ArrayList<Part>()
            var idx = 0
            while (idx < template.length) {
                val start = template.indexOf("{{", idx)
                if (start < 0) {
                    parts.add(Part.Text(template.substring(idx)))
                    break
                }
                if (start > idx) {
                    parts.add(Part.Text(template.substring(idx, start)))
                }
                val end = template.indexOf("}}", start + 2)
                require(end >= 0) { "模板占位符缺少结束符 '}}'" }
                val raw = template.substring(start + 2, end).trim()
                require(raw.isNotEmpty()) { "模板占位符不能为空" }

                if (raw.startsWith("#id")) {
                    val name = raw.removePrefix("#id").trim()
                    require(name.isNotEmpty()) { "标识符占位符缺少参数名：{{#id name}}" }
                    parts.add(Part.Identifier(name))
                } else {
                    parts.add(Part.Value(raw))
                }
                idx = end + 2
            }

            return StringTemplate(parts)
        }

        private val identifierRegex = Regex("^[A-Za-z_][A-Za-z0-9_]*$")

        fun isSafeIdentifier(raw: String): Boolean = identifierRegex.matches(raw)
    }
}

