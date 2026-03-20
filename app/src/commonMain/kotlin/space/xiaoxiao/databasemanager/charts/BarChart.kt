package space.xiaoxiao.databasemanager.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 柱状图组件
 */
@Composable
fun BarChart(
    chartData: ChartData,
    modifier: Modifier = Modifier,
    barColor: Color = chartData.color,
    showValues: Boolean = true
) {
    if (chartData.labels.isEmpty() || chartData.values.isEmpty()) return

    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        if (chartData.title.isNotEmpty()) {
            Text(
                text = chartData.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 50.dp, top = 20.dp, end = 20.dp, bottom = 40.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height

                val maxValue = chartData.values.maxOrNull() ?: 1.0
                val barCount = chartData.labels.size
                val barSpacing = 8f
                val totalSpacing = barSpacing * (barCount + 1)
                val barWidth = (chartWidth - totalSpacing) / barCount

                // 绘制Y轴刻度线
                val yStep = maxValue / 4
                for (i in 0..4) {
                    val y = chartHeight - (chartHeight * i / 4)

                    // 绘制水平网格线
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                // 绘制柱状图
                chartData.values.forEachIndexed { index, value ->
                    val barHeight = (value / maxValue * chartHeight).toFloat()
                    val x = barSpacing + (barWidth + barSpacing) * index
                    val y = chartHeight - barHeight

                    // 绘制柱体
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }

            // Y轴标签
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val maxValue = chartData.values.maxOrNull() ?: 1.0
                for (i in 4 downTo 0) {
                    val value = maxValue * i / 4
                    Text(
                        text = formatValue(value),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor
                    )
                }
            }

            // X轴标签和数值标签
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 50.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val maxValue = chartData.values.maxOrNull() ?: 1.0

                    chartData.labels.forEachIndexed { index, label ->
                        val value = chartData.values.getOrElse(index) { 0.0 }
                        val barHeight = if (maxValue > 0) (value / maxValue * 100).toInt() else 0

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (showValues && barHeight > 10) {
                                Text(
                                    text = formatValue(value),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.7f),
                                    fontSize = androidx.compose.ui.unit.TextUnit.Unspecified
                                )
                            }
                            Text(
                                text = if (label.length > 6) label.take(6) + ".." else label,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatValue(value: Double): String {
    return when {
        value >= 1000000 -> String.format("%.1fM", value / 1000000)
        value >= 1000 -> String.format("%.1fK", value / 1000)
        value == value.toLong().toDouble() -> value.toLong().toString()
        else -> String.format("%.1f", value)
    }
}