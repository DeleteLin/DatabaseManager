package space.xiaoxiao.databasemanager.theme

import androidx.compose.ui.unit.dp

/**
 * 间距系统 - 基于 4dp 网格
 *
 * 使用场景：
 * - spaceLg (16dp)：页面内边距、卡片内边距
 * - spaceMd (12dp)：列表项间距、卡片内边距
 * - spaceSm (8dp)：图标与文字间距
 * - spaceXl (24dp)：区块间距
 * - spaceXxl (32dp)：大区块间距
 */
object AppSpacing {
    val spaceNone = 0.dp
    val spaceXxs = 2.dp   // 极小间距（图标与文字间）
    val spaceXs = 4.dp    // 元素内部间距
    val spaceSm = 8.dp    // 紧凑元素间距、图标与文字间距
    val spaceMd = 12.dp   // 标准元素间距、列表项间距
    val spaceLg = 16.dp   // 页面内边距、卡片内边距
    val spaceXl = 24.dp   // 区块间距
    val spaceXxl = 32.dp  // 大区块间距
}