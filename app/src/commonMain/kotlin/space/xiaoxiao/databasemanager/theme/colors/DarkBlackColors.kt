package space.xiaoxiao.databasemanager.theme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 暗黑主题 - 双色调设计
 *
 * 设计灵感：岩石与钢铁
 * - 背景：深灰到浅灰的渐变层次
 * - 主色：明亮蓝灰作为视觉焦点
 *
 * 配色方案：
 * - 亮色主题 "Slate Rock"：深灰背景 + 蓝灰主色
 * - 暗色主题 "Pure AMOLED"：纯黑背景 + 明亮蓝灰
 */
object DarkBlackColors : AppColorScheme {
    override val id = "dark_black"

    // ==================== 亮色主题 "Slate Rock" ====================
    private val LightBackground = Color(0xFFE8EAED)        // 浅灰
    private val LightOnBackground = Color(0xFF1A1C22)      // 深灰黑

    private val LightSurface = Color(0xFFF1F3F5)           // 浅灰表面
    private val LightOnSurface = Color(0xFF1F2128)         // 深灰

    private val LightSurfaceVariant = Color(0xFFD3D7DC)    // 灰变体
    private val LightOnSurfaceVariant = Color(0xFF3A3D45)  // 中灰

    // 主色调：深蓝灰
    private val LightPrimary = Color(0xFF475569)           // Slate 600
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFFCBD5E1)
    private val LightOnPrimaryContainer = Color(0xFF1E293B)

    // 辅助色：中蓝灰
    private val LightSecondary = Color(0xFF64748B)         // Slate 500
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFFE2E8F0)
    private val LightOnSecondaryContainer = Color(0xFF334155)

    // 第三色：靛蓝色点缀
    private val LightTertiary = Color(0xFF818CF8)          // Indigo 400
    private val LightOnTertiary = Color(0xFFFFFFFF)
    private val LightTertiaryContainer = Color(0xFFE0E7FF)
    private val LightOnTertiaryContainer = Color(0xFF3730A3)

    // Surface Container 系列 - 灰色层级
    private val LightSurfaceContainerLowest = Color(0xFFF5F6F8)
    private val LightSurfaceContainerLow = Color(0xFFEDEFF2)
    private val LightSurfaceContainer = Color(0xFFE8EAED)
    private val LightSurfaceContainerHigh = Color(0xFFD3D7DC)
    private val LightSurfaceContainerHighest = Color(0xFFBEC3CC)

    private val LightError = Color(0xFFDC2626)
    private val LightOnError = Color(0xFFFFFFFF)
    private val LightErrorContainer = Color(0xFFFEE2E2)
    private val LightOnErrorContainer = Color(0xFF7F1D1D)

    private val LightOutline = Color(0xFF8B92A3)
    private val LightOutlineVariant = Color(0xFFBEC3CC)

    // ==================== 暗色主题 "Pure AMOLED" ====================
    private val DarkBackground = Color(0xFF050505)         // 近黑
    private val DarkOnBackground = Color(0xFFE2E4E8)       // 浅灰白

    private val DarkSurface = Color(0xFF121214)            // 深灰表面
    private val DarkOnSurface = Color(0xFFD0D2D8)          // 浅灰文字

    private val DarkSurfaceVariant = Color(0xFF1E1F25)     // 灰变体
    private val DarkOnSurfaceVariant = Color(0xFFC8CAD5)   // 中浅灰

    // 主色调：明亮蓝灰
    private val DarkPrimary = Color(0xFF94A3B8)            // Slate 400
    private val DarkOnPrimary = Color(0xFF1E293B)
    private val DarkPrimaryContainer = Color(0xFF475569)
    private val DarkOnPrimaryContainer = Color(0xFFF1F5F9)

    // 辅助色
    private val DarkSecondary = Color(0xFFA8B2C7)          // Slate 400 变体
    private val DarkOnSecondary = Color(0xFF1E293B)
    private val DarkSecondaryContainer = Color(0xFF334155)
    private val DarkOnSecondaryContainer = Color(0xFFE2E8F0)

    // 第三色
    private val DarkTertiary = Color(0xFFA5B4FC)           // Indigo 300
    private val DarkOnTertiary = Color(0xFF312E81)
    private val DarkTertiaryContainer = Color(0xFF4338CA)
    private val DarkOnTertiaryContainer = Color(0xFFE0E7FF)

    // Surface Container 系列 - 深灰层级
    private val DarkSurfaceContainerLowest = Color(0xFF000000)
    private val DarkSurfaceContainerLow = Color(0xFF0D0E12)
    private val DarkSurfaceContainer = Color(0xFF121214)
    private val DarkSurfaceContainerHigh = Color(0xFF1E1F25)
    private val DarkSurfaceContainerHighest = Color(0xFF2A2C33)

    private val DarkError = Color(0xFFF87171)
    private val DarkOnError = Color(0xFF7F1D1D)
    private val DarkErrorContainer = Color(0xFF991B1B)
    private val DarkOnErrorContainer = Color(0xFFFECACA)

    private val DarkOutline = Color(0xFF3A3C45)
    private val DarkOutlineVariant = Color(0xFF1E1F25)

    override val lightScheme: ColorScheme = lightColorScheme(
        primary = LightPrimary,
        onPrimary = LightOnPrimary,
        primaryContainer = LightPrimaryContainer,
        onPrimaryContainer = LightOnPrimaryContainer,
        secondary = LightSecondary,
        onSecondary = LightOnSecondary,
        secondaryContainer = LightSecondaryContainer,
        onSecondaryContainer = LightOnSecondaryContainer,
        tertiary = LightTertiary,
        onTertiary = LightOnTertiary,
        tertiaryContainer = LightTertiaryContainer,
        onTertiaryContainer = LightOnTertiaryContainer,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightOnSurfaceVariant,
        surfaceContainerLowest = LightSurfaceContainerLowest,
        surfaceContainerLow = LightSurfaceContainerLow,
        surfaceContainer = LightSurfaceContainer,
        surfaceContainerHigh = LightSurfaceContainerHigh,
        surfaceContainerHighest = LightSurfaceContainerHighest,
        error = LightError,
        onError = LightOnError,
        errorContainer = LightErrorContainer,
        onErrorContainer = LightOnErrorContainer,
        outline = LightOutline,
        outlineVariant = LightOutlineVariant
    )

    override val darkScheme: ColorScheme = darkColorScheme(
        primary = DarkPrimary,
        onPrimary = DarkOnPrimary,
        primaryContainer = DarkPrimaryContainer,
        onPrimaryContainer = DarkOnPrimaryContainer,
        secondary = DarkSecondary,
        onSecondary = DarkOnSecondary,
        secondaryContainer = DarkSecondaryContainer,
        onSecondaryContainer = DarkOnSecondaryContainer,
        tertiary = DarkTertiary,
        onTertiary = DarkOnTertiary,
        tertiaryContainer = DarkTertiaryContainer,
        onTertiaryContainer = DarkOnTertiaryContainer,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurfaceVariant,
        surfaceContainerLowest = DarkSurfaceContainerLowest,
        surfaceContainerLow = DarkSurfaceContainerLow,
        surfaceContainer = DarkSurfaceContainer,
        surfaceContainerHigh = DarkSurfaceContainerHigh,
        surfaceContainerHighest = DarkSurfaceContainerHighest,
        error = DarkError,
        onError = DarkOnError,
        errorContainer = DarkErrorContainer,
        onErrorContainer = DarkOnErrorContainer,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant
    )
}
