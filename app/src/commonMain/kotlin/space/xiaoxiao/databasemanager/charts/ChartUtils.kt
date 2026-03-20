package space.xiaoxiao.databasemanager.charts

import androidx.compose.ui.graphics.Color
import space.xiaoxiao.databasemanager.core.Column
import space.xiaoxiao.databasemanager.core.QueryResult
import space.xiaoxiao.databasemanager.core.Row
import kotlin.math.roundToInt

/**
 * 将查询结果转换为图表数据
 */
fun QueryResult.toChartData(
    labelColumn: Int = 0,
    valueColumn: Int = 1,
    title: String = ""
): Result<ChartData> {
    return try {
        if (rows.isEmpty()) {
            return Result.failure(Exception("Query result is empty"))
        }

        if (labelColumn < 0 || labelColumn >= columns.size ||
            valueColumn < 0 || valueColumn >= columns.size) {
            return Result.failure(Exception("Invalid column index"))
        }

        val labels = mutableListOf<String>()
        val values = mutableListOf<Double>()

        rows.forEach { row ->
            val label = row.values.getOrNull(labelColumn)?.toString() ?: ""
            val valueStr = row.values.getOrNull(valueColumn)?.toString() ?: "0"
            val value = valueStr.toDoubleOrNull() ?: 0.0

            labels.add(label)
            values.add(value)
        }

        Result.success(ChartData(
            labels = labels,
            values = values,
            title = title
        ))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * 将图表数据转换为饼图扇形列表
 */
fun ChartData.toPieSlices(): List<PieSlice> {
    val total = values.sum()
    if (total == 0.0) return emptyList()

    // 预设颜色列表
    val colors = listOf(
        Color(0xFF2196F3), // 蓝色
        Color(0xFF4CAF50), // 绿色
        Color(0xFFFF9800), // 橙色
        Color(0xFFE91E63), // 粉色
        Color(0xFF9C27B0), // 紫色
        Color(0xFF00BCD4), // 青色
        Color(0xFFFFEB3B), // 黄色
        Color(0xFF795548), // 棕色
        Color(0xFF607D8B), // 灰蓝色
        Color(0xFF009688)  // 蓝绿色
    )

    return labels.mapIndexed { index, label ->
        val value = values.getOrElse(index) { 0.0 }
        PieSlice(
            label = label,
            value = value,
            percentage = ((value / total) * 100).toFloat(),
            color = colors[index % colors.size]
        )
    }
}

/**
 * 获取安全的数值（用于显示）
 */
fun Any?.toDisplayString(): String {
    return when (this) {
        null -> ""
        is Number -> {
            if (this is Double || this is Float) {
                val rounded = (this.toDouble() * 100).roundToInt() / 100.0
                if (rounded == rounded.toLong().toDouble()) {
                    rounded.toLong().toString()
                } else {
                    rounded.toString()
                }
            } else {
                this.toString()
            }
        }
        else -> this.toString()
    }
}