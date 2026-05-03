package space.xiaoxiao.databasemanager.theme

import androidx.compose.ui.unit.dp

/**
 * 响应式窗口尺寸断点
 *
 * 参照 Material 3 窗口尺寸类规范：
 * - [CompactWidth] ≤ 360dp：手机竖屏
 * - [MediumWidth] ≤ 600dp：平板竖屏 / 折叠屏展开
 */
object WindowSizeClass {
    /** 紧凑宽度断点（手机竖屏） */
    val CompactWidth = 360.dp

    /** 中等宽度断点（平板竖屏 / 折叠屏展开） */
    val MediumWidth = 600.dp
}

/**
 * 语义化尺寸令牌
 *
 * 统一常用组件的标准尺寸，避免魔术数字散布各处。
 */
object SemanticSizes {
    /** 顶栏高度 */
    val topBarHeight = 56.dp

    /** 浮动操作按钮尺寸 */
    val fabSize = 56.dp

    /** 最小可触摸目标尺寸（无障碍基础要求） */
    val minTouchTarget = 48.dp
}

/**
 * 阴影/海拔层级
 *
 * 定义三层海拔，覆盖从无阴影到明显浮动的常见需求。
 */
object ElevationTokens {
    /** 无海拔 — 平铺于表面 */
    val elevationNone = 0.dp

    /** 第 1 层 — 轻微浮起（如卡片、按钮悬停） */
    val elevationLevel1 = 1.dp

    /** 第 2 层 — 明显浮起（如对话框、导航栏） */
    val elevationLevel2 = 3.dp
}
