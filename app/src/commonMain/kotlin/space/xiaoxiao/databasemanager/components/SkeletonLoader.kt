package space.xiaoxiao.databasemanager.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/**
 * 闪烁效果修饰符
 */
@Composable
fun Modifier.shimmer(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    return this.then(
        Modifier.background(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        )
    )
}

/**
 * 骨架屏单元格
 */
@Composable
fun SkeletonCell(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .padding(4.dp)
            .clip(RoundedCornerShape(4.dp))
            .shimmer()
    )
}

/**
 * 表格骨架屏加载器
 */
@Composable
fun TableSkeletonLoader(
    rowCount: Int = 5,
    columnCount: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 表头骨架
        Row {
            repeat(columnCount) {
                SkeletonCell(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 数据行骨架
        repeat(rowCount) {
            Row {
                repeat(columnCount) { col ->
                    SkeletonCell(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 列表项骨架屏
 */
@Composable
fun ListItemSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        // 图标占位
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmer()
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            // 标题占位
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 副标题占位
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }
    }
}

/**
 * 卡片骨架屏
 */
@Composable
fun CardSkeleton(
    modifier: Modifier = Modifier,
    lineCount: Int = 3
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        repeat(lineCount) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (index == lineCount - 1) 0.5f else 1f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
            if (index < lineCount - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * 数据库连接卡片骨架屏
 */
@Composable
fun DatabaseConnectionCardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 图标占位
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .shimmer()
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                // 连接名称占位
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmer()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 类型 Badge 占位
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmer()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 连接信息占位
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmer()
                )
            }
        }
    }
}