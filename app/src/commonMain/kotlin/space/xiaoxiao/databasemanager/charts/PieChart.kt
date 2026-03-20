package space.xiaoxiao.databasemanager.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * 饼图组件
 */
@Composable
fun PieChart(
    chartData: ChartData,
    modifier: Modifier = Modifier,
    showLegend: Boolean = true,
    showPercentages: Boolean = true
) {
    if (chartData.labels.isEmpty() || chartData.values.isEmpty()) return

    val slices = chartData.toPieSlices()
    if (slices.isEmpty()) return

    val textColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        if (chartData.title.isNotEmpty()) {
            Text(
                text = chartData.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 饼图
            Box(
                modifier = Modifier
                    .weight(1f)
                    .size(220.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasSize = minOf(size.width, size.height)
                    val radius = canvasSize / 2 * 0.85f
                    val center = Offset(size.width / 2, size.height / 2)

                    var startAngle = -90f

                    slices.forEach { slice ->
                        val sweepAngle = slice.percentage * 3.6f

                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )

                        drawArc(
                            color = Color.White,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = 2f)
                        )

                        startAngle += sweepAngle
                    }
                }
            }

            // 图例
            if (showLegend) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    slices.forEach { slice ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(slice.color, shape = androidx.compose.foundation.shape.CircleShape)
                            )
                            Column {
                                Text(
                                    text = if (slice.label.length > 10) slice.label.take(10) + ".." else slice.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor
                                )
                                if (showPercentages) {
                                    Text(
                                        text = "${formatPercentage(slice.percentage)} (${formatValue(slice.value)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textColor.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatPercentage(percentage: Float): String {
    return String.format("%.1f%%", percentage)
}

private fun formatValue(value: Double): String {
    return when {
        value >= 1000000 -> String.format("%.1fM", value / 1000000)
        value >= 1000 -> String.format("%.1fK", value / 1000)
        value == value.toLong().toDouble() -> value.toLong().toString()
        else -> String.format("%.1f", value)
    }
}