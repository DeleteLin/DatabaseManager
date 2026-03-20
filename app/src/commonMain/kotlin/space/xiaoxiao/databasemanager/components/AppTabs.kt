package space.xiaoxiao.databasemanager.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AppPillTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = false,
    tabHeight: Dp = 48.dp,
    horizontalPadding: Dp = 6.dp,
    pillHorizontalPadding: Dp = 10.dp,
    textMaxWidth: Dp = 160.dp,
    addTabIcon: ImageVector? = null,
    addTabContentDescription: String? = null,
    onAddTab: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val tabShape = RoundedCornerShape(6.dp)

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        BoxWithConstraints {
            val rowModifier = if (isScrollable) {
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            } else {
                Modifier.fillMaxWidth()
            }

            Row(
                modifier = rowModifier.padding(horizontal = horizontalPadding, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = index == selectedIndex
                    Surface(
                        modifier = Modifier
                            .height(tabHeight)
                            .clip(tabShape)
                            .clickable { onSelectIndex(index) },
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = tabShape
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = pillHorizontalPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = textMaxWidth)
                            )
                        }
                    }
                    if (index != tabs.lastIndex) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                if (onAddTab != null && addTabIcon != null) {
                    if (tabs.isNotEmpty()) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                    }
                    Surface(
                        modifier = Modifier
                            .height(tabHeight)
                            .clip(tabShape)
                            .clickable { onAddTab() },
                        color = MaterialTheme.colorScheme.surface,
                        shape = tabShape
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = pillHorizontalPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = addTabIcon,
                                contentDescription = addTabContentDescription,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

