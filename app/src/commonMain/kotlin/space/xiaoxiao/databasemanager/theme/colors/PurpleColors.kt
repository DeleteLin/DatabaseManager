package space.xiaoxiao.databasemanager.theme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 紫色主题 - 双色调设计
 *
 * 设计灵感：薰衣草田与黄昏
 * - 背景：深紫到浅紫的渐变层次
 * - 主色：鲜明紫罗兰作为视觉焦点
 *
 * 配色方案：
 * - 亮色主题 "Lavender Mist"：浅紫灰背景 + 紫罗兰主色
 * - 暗色主题 "Purple Night"：深紫背景 + 薰衣紫主色
 */
object PurpleColors : AppColorScheme {
    override val id = "purple"

    // ==================== 亮色主题 "Lavender Mist" ====================
    private val LightBackground = Color(0xFFF5F3F8)        // 极浅紫灰
    private val LightOnBackground = Color(0xFF291F33)      // 深紫黑

    private val LightSurface = Color(0xFFFFFFFF)
    private val LightOnSurface = Color(0xFF2F2538)         // 深紫

    private val LightSurfaceVariant = Color(0xFFE8E3ED)    // 浅紫灰
    private val LightOnSurfaceVariant = Color(0xFF483D58)  // 中紫灰

    // 主色调：饱和紫罗兰
    private val LightPrimary = Color(0xFF8B5CF6)           // Violet 500
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFFC4B5FD)
    private val LightOnPrimaryContainer = Color(0xFF5B21B6)

    // 辅助色：紫色
    private val LightSecondary = Color(0xFFA78BFA)         // Violet 400
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFFEDE9FE)
    private val LightOnSecondaryContainer = Color(0xFF6D28D9)

    // 第三色：粉红色点缀
    private val LightTertiary = Color(0xFFEC4899)          // Pink 500
    private val LightOnTertiary = Color(0xFFFFFFFF)
    private val LightTertiaryContainer = Color(0xFFFBD5E1)
    private val LightOnTertiaryContainer = Color(0xFF9D174D)

    // Surface Container 系列 - 紫灰层级
    private val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
    private val LightSurfaceContainerLow = Color(0xFFF9F8FB)
    private val LightSurfaceContainer = Color(0xFFF5F3F8)
    private val LightSurfaceContainerHigh = Color(0xFFE8E3ED)
    private val LightSurfaceContainerHighest = Color(0xFFDBD4E2)

    private val LightError = Color(0xFFDC2626)
    private val LightOnError = Color(0xFFFFFFFF)
    private val LightErrorContainer = Color(0xFFFEE2E2)
    private val LightOnErrorContainer = Color(0xFF7F1D1D)

    private val LightOutline = Color(0xFFA398B3)
    private val LightOutlineVariant = Color(0xFFDBD4E2)

    // ==================== 暗色主题 "Purple Night" ====================
    private val DarkBackground = Color(0xFF140F1F)         // 深紫黑
    private val DarkOnBackground = Color(0xFFE8E2F0)       // 浅紫白

    private val DarkSurface = Color(0xFF2B2238)            // 深紫表面
    private val DarkOnSurface = Color(0xFFD8D0E5)          // 浅紫文字

    private val DarkSurfaceVariant = Color(0xFF3D3152)     // 紫灰变体
    private val DarkOnSurfaceVariant = Color(0xFFC8BEDC)   // 中浅紫

    // 主色调：柔和薰衣紫
    private val DarkPrimary = Color(0xFFA78BFA)            // Violet 400
    private val DarkOnPrimary = Color(0xFF5B21B6)
    private val DarkPrimaryContainer = Color(0xFF6D28D9)
    private val DarkOnPrimaryContainer = Color(0xFFEDE9FE)

    // 辅助色
    private val DarkSecondary = Color(0xFFC4B5FD)          // Violet 300
    private val DarkOnSecondary = Color(0xFF6D28D9)
    private val DarkSecondaryContainer = Color(0xFF5B21B6)
    private val DarkOnSecondaryContainer = Color(0xFFDDD6FE)

    // 第三色
    private val DarkTertiary = Color(0xFFF472B6)           // Pink 400
    private val DarkOnTertiary = Color(0xFF9D174D)
    private val DarkTertiaryContainer = Color(0xFFDB2777)
    private val DarkOnTertiaryContainer = Color(0xFFFCE7F3)

    // Surface Container 系列 - 深紫层级
    private val DarkSurfaceContainerLowest = Color(0xFF0F0B18)
    private val DarkSurfaceContainerLow = Color(0xFF1C152B)
    private val DarkSurfaceContainer = Color(0xFF2B2238)
    private val DarkSurfaceContainerHigh = Color(0xFF3D3152)
    private val DarkSurfaceContainerHighest = Color(0xFF504166)

    private val DarkError = Color(0xFFF87171)
    private val DarkOnError = Color(0xFF7F1D1D)
    private val DarkErrorContainer = Color(0xFF991B1B)
    private val DarkOnErrorContainer = Color(0xFFFECACA)

    private val DarkOutline = Color(0xFF4A3D5C)
    private val DarkOutlineVariant = Color(0xFF3D3152)

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
