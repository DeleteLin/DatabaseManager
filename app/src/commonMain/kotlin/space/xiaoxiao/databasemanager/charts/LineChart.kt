package space.xiaoxiao.databasemanager.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 折线图组件
 */
@Composable
fun LineChart(
    chartData: ChartData,
    modifier: Modifier = Modifier,
    lineColor: Color = chartData.color,
    showPoints: Boolean = true,
    showArea: Boolean = true,
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
                val paddingBottom = 0f

                val maxValue = chartData.values.maxOrNull() ?: 1.0
                val minValue = chartData.values.minOrNull() ?: 0.0
                val valueRange = if (maxValue == minValue) 1.0 else maxValue - minValue

                val dataCount = chartData.values.size
                val xStep = if (dataCount > 1) chartWidth / (dataCount - 1) else chartWidth

                // 绘制Y轴刻度线
                val yStep = valueRange / 4
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

                // 计算数据点位置
                val points = chartData.values.mapIndexed { index, value ->
                    val x = xStep * index
                    val normalizedValue = (value - minValue) / valueRange
                    val y = chartHeight - (chartHeight * normalizedValue).toFloat()
                    Offset(x, y)
                }

                // 绘制填充区域
                if (showArea && points.isNotEmpty()) {
                    val areaPath = Path().apply {
                        moveTo(points.first().x, chartHeight)
                        points.forEach { point ->
                            lineTo(point.x, point.y)
                        }
                        lineTo(points.last().x, chartHeight)
                        close()
                    }
                    drawPath(
                        path = areaPath,
                        color = lineColor.copy(alpha = 0.2f)
                    )
                }

                // 绘制折线
                if (points.size >= 2) {
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = lineColor,
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }

                // 绘制数据点
                if (showPoints) {
                    points.forEach { point ->
                        drawCircle(
                            color = lineColor,
                            radius = 6f,
                            center = point
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3f,
                            center = point
                        )
                    }
                }
            }

            // Y轴标签（使用 Compose Text 组件）
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val maxValue = chartData.values.maxOrNull() ?: 1.0
                val minValue = chartData.values.minOrNull() ?: 0.0
                val valueRange = if (maxValue == minValue) 1.0 else maxValue - minValue

                for (i in 4 downTo 0) {
                    val value = minValue + valueRange * i / 4
                    Text(
                        text = formatValue(value),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor
                    )
                }
            }

            // X轴标签
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 50.dp, end = 20.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dataCount = chartData.labels.size
                chartData.labels.forEachIndexed { index, label ->
                    if (index % ((dataCount / 5).coerceAtLeast(1)) == 0 || index == dataCount - 1) {
                        Text(
                            text = if (label.length > 6) label.take(6) + ".." else label,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor
                        )
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                }
            }

            // 数值标签
            if (showValues) {
                val maxValue = chartData.values.maxOrNull() ?: 1.0
                val minValue = chartData.values.minOrNull() ?: 0.0
                val valueRange = if (maxValue == minValue) 1.0 else maxValue - minValue

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(start = 50.dp, top = 20.dp, end = 20.dp, bottom = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    chartData.values.forEachIndexed { index, value ->
                        val normalizedValue = (value - minValue) / valueRange
                        Box(
                            modifier = Modifier
                        ) {
                            Text(
                                text = formatValue(value),
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 8.sp
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