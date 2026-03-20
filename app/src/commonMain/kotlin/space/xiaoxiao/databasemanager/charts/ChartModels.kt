package space.xiaoxiao.databasemanager.charts

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 图表类型
 */
enum class ChartType {
    BAR,    // 柱状图
    PIE,    // 饼图
    LINE    // 折线图
}

/**
 * 图表宽度枚举
 * @param span 在网格中占据的列数（基于 2 列网格）
 */
enum class ChartWidth(val span: Int, val displayNameKey: String) {
    MEDIUM(1, "chart_size_medium"),    // 半宽 (1列)
    FULL(2, "chart_size_full")         // 全宽 (2列)
}

/**
 * 图表面板 (Tab)
 */
data class ChartPanel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val charts: List<ChartConfig> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 图表配置
 */
data class ChartConfig(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val chartType: ChartType = ChartType.BAR,
    val databaseId: String,
    val sqlQuery: String,
    val labelColumnIndex: Int = 0,
    val valueColumnIndex: Int = 1,
    val color: Color = Color(0xFF2196F3),
    val width: ChartWidth = ChartWidth.FULL,
    val position: Int = 0
)

/**
 * 图表运行时数据 (用于 UI 显示)
 */
data class ChartData(
    val labels: List<String>,        // X轴标签
    val values: List<Double>,        // 数据值
    val title: String = "",          // 图表标题
    val color: Color = Color(0xFF2196F3) // 主颜色
)

/**
 * 图表运行时状态 (包含配置和数据)
 */
data class ChartState(
    val config: ChartConfig,
    val data: ChartData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 饼图扇形数据
 */
data class PieSlice(
    val label: String,
    val value: Double,
    val percentage: Float,
    val color: Color
)

/**
 * 图表错误类型
 */
sealed class ChartError {
    object NoData : ChartError()
    object NoDatabase : ChartError()
    data class QueryError(val message: String) : ChartError()
    data class InvalidData(val message: String) : ChartError()
}

/**
 * 可序列化的图表面板配置
 */
@Serializable
data class SerializableChartPanel(
    val id: String,
    val name: String,
    val charts: List<SerializableChartConfig>,
    val createdAt: Long
) {
    companion object {
        fun fromChartPanel(panel: ChartPanel): SerializableChartPanel {
            return SerializableChartPanel(
                id = panel.id,
                name = panel.name,
                charts = panel.charts.map { SerializableChartConfig.fromChartConfig(it) },
                createdAt = panel.createdAt
            )
        }

        fun toChartPanel(panel: SerializableChartPanel): ChartPanel {
            return ChartPanel(
                id = panel.id,
                name = panel.name,
                charts = panel.charts.map { SerializableChartConfig.toChartConfig(it) },
                createdAt = panel.createdAt
            )
        }
    }
}

/**
 * 可序列化的图表配置
 */
@Serializable
data class SerializableChartConfig(
    val id: String,
    val title: String,
    val chartType: String,
    val databaseId: String,
    val sqlQuery: String,
    val labelColumnIndex: Int,
    val valueColumnIndex: Int,
    val colorValue: ULong,
    val width: String,
    val position: Int
) {
    companion object {
        fun fromChartConfig(config: ChartConfig): SerializableChartConfig {
            return SerializableChartConfig(
                id = config.id,
                title = config.title,
                chartType = config.chartType.name,
                databaseId = config.databaseId,
                sqlQuery = config.sqlQuery,
                labelColumnIndex = config.labelColumnIndex,
                valueColumnIndex = config.valueColumnIndex,
                colorValue = config.color.value,
                width = config.width.name,
                position = config.position
            )
        }

        fun toChartConfig(config: SerializableChartConfig): ChartConfig {
            return ChartConfig(
                id = config.id,
                title = config.title,
                chartType = ChartType.valueOf(config.chartType),
                databaseId = config.databaseId,
                sqlQuery = config.sqlQuery,
                labelColumnIndex = config.labelColumnIndex,
                valueColumnIndex = config.valueColumnIndex,
                color = Color(config.colorValue),
                width = ChartWidth.valueOf(config.width),
                position = config.position
            )
        }
    }
}