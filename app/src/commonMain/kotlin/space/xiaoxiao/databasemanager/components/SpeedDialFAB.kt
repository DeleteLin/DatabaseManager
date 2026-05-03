package space.xiaoxiao.databasemanager.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import space.xiaoxiao.databasemanager.theme.AppSpacing
import space.xiaoxiao.databasemanager.theme.ElevationTokens
import space.xiaoxiao.databasemanager.theme.SemanticSizes

/**
 * Speed Dial 操作项
 *
 * @param icon  迷你 FAB 图标
 * @param label 操作标签文字
 * @param onClick 点击回调
 */
data class SpeedDialAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

/**
 * 可展开悬浮按钮组件 (Material3 Speed Dial)
 *
 * 主 FAB 点击后展开 2-4 个迷你 FAB，带半透明遮罩层和动画效果。
 * 可用于页面内放置，通过 overlay 覆盖全屏。
 *
 * 使用方式：
 * ```kotlin
 * var expanded by remember { mutableStateOf(false) }
 * SpeedDialFAB(
 *     actions = listOf(
 *         SpeedDialAction(Icons.Filled.Edit, "编辑") { ... },
 *         SpeedDialAction(Icons.Filled.Delete, "删除") { ... }
 *     ),
 *     isExpanded = expanded,
 *     onToggle = { expanded = !expanded }
 * )
 * ```
 *
 * @param mainIcon   主 FAB 图标，默认 [Icons.Filled.Add]
 * @param actions    操作项列表（建议 2-4 个）
 * @param isExpanded 是否展开
 * @param onToggle   展开/收起回调
 * @param modifier   修饰符
 */
@Composable
fun SpeedDialFAB(
    mainIcon: ImageVector = Icons.Filled.Add,
    actions: List<SpeedDialAction>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "fabRotation"
    )

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(durationMillis = 150)),
            exit = fadeOut(animationSpec = tween(durationMillis = 150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = AppSpacing.spaceLg,
                    bottom = AppSpacing.spaceLg
                ),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.spaceSm)
        ) {
            actions.reversed().forEachIndexed { index, action ->
                val delayMs = index * 60
                ActionItem(
                    action = action,
                    visible = isExpanded,
                    index = delayMs
                )
            }

            FloatingActionButton(
                onClick = onToggle,
                modifier = Modifier.size(SemanticSizes.fabSize),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = mainIcon,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun ActionItem(
    action: SpeedDialAction,
    visible: Boolean,
    index: Int
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 180, delayMillis = index)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 180, delayMillis = index),
                    initialOffsetY = { it }
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 120)) +
                slideOutVertically(
                    animationSpec = tween(durationMillis = 120),
                    targetOffsetY = { it }
                )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = ElevationTokens.elevationLevel2
            ) {
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        horizontal = AppSpacing.spaceSm,
                        vertical = AppSpacing.spaceXs
                    )
                )
            }

            Spacer(modifier = Modifier.width(AppSpacing.spaceSm))

            SmallFloatingActionButton(
                onClick = action.onClick,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.label
                )
            }
        }
    }
}
