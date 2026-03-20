package space.xiaoxiao.databasemanager.theme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 橙彩主题 - 双色调设计
 *
 * 设计灵感：海上日落
 * - 背景：深蓝灰到浅蓝灰的渐变层次
 * - 主色：鲜明橙色作为视觉焦点（冷色背景 + 暖色主色）
 *
 * 配色方案：
 * - 亮色主题 "Pacific Mist"：浅蓝灰背景 + 橙色主色
 * - 暗色主题 "Pacific Night"：深蓝灰背景 + 柔和橙色
 */
object SlateOrangeColors : AppColorScheme {
    override val id = "slate_orange"

    // ==================== 亮色主题 "Pacific Mist" ====================
    private val LightBackground = Color(0xFFF0F4F8)        // 极浅蓝灰
    private val LightOnBackground = Color(0xFF1A2938)      // 深蓝黑

    private val LightSurface = Color(0xFFFFFFFF)
    private val LightOnSurface = Color(0xFF1F3347)         // 深蓝

    private val LightSurfaceVariant = Color(0xFFE0E8EF)    // 浅蓝灰
    private val LightOnSurfaceVariant = Color(0xFF3A4858)  // 中蓝灰

    // 主色调：饱和橙色（暖色对比）
    private val LightPrimary = Color(0xFFEA580C)           // Orange 600
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFFFED7AA)
    private val LightOnPrimaryContainer = Color(0xFF7C2D12)

    // 辅助色：深橙色
    private val LightSecondary = Color(0xFFC2410C)         // Orange 700
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFFFFEDD5)
    private val LightOnSecondaryContainer = Color(0xFF431407)

    // 第三色：蓝绿色（冷暖平衡）
    private val LightTertiary = Color(0xFF0D9488)          // Teal 600
    private val LightOnTertiary = Color(0xFFFFFFFF)
    private val LightTertiaryContainer = Color(0xFF99F6E4)
    private val LightOnTertiaryContainer = Color(0xFF042F2E)

    // Surface Container 系列 - 蓝灰层级
    private val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
    private val LightSurfaceContainerLow = Color(0xFFF8FAFC)
    private val LightSurfaceContainer = Color(0xFFF0F4F8)
    private val LightSurfaceContainerHigh = Color(0xFFE0E8EF)
    private val LightSurfaceContainerHighest = Color(0xFFD0DBE5)

    private val LightError = Color(0xFFDC2626)
    private val LightOnError = Color(0xFFFFFFFF)
    private val LightErrorContainer = Color(0xFFFEE2E2)
    private val LightOnErrorContainer = Color(0xFF7F1D1D)

    private val LightOutline = Color(0xFF8B9FB3)
    private val LightOutlineVariant = Color(0xFFD0DBE5)

    // ==================== 暗色主题 "Pacific Night" ====================
    private val DarkBackground = Color(0xFF0C1929)         // 深蓝黑
    private val DarkOnBackground = Color(0xFFD8E2EE)       // 浅蓝白

    private val DarkSurface = Color(0xFF1A2B42)            // 深蓝表面
    private val DarkOnSurface = Color(0xFFC8D5E5)          // 浅蓝文字

    private val DarkSurfaceVariant = Color(0xFF2A3D57)     // 蓝灰变体
    private val DarkOnSurfaceVariant = Color(0xFFB8C8D9)   // 中浅蓝

    // 主色调：柔和橙色
    private val DarkPrimary = Color(0xFFFDBA74)            // Orange 300
    private val DarkOnPrimary = Color(0xFF7C2D12)
    private val DarkPrimaryContainer = Color(0xFF9A3412)
    private val DarkOnPrimaryContainer = Color(0xFFFED7AA)

    // 辅助色
    private val DarkSecondary = Color(0xFFFDBA74)          // Orange 300
    private val DarkOnSecondary = Color(0xFF7C2D12)
    private val DarkSecondaryContainer = Color(0xFF431407)
    private val DarkOnSecondaryContainer = Color(0xFFFFEDD5)

    // 第三色
    private val DarkTertiary = Color(0xFF2DD4BF)           // Teal 400
    private val DarkOnTertiary = Color(0xFF042F2E)
    private val DarkTertiaryContainer = Color(0xFF115E59)
    private val DarkOnTertiaryContainer = Color(0xFF99F6E4)

    // Surface Container 系列 - 深蓝层级
    private val DarkSurfaceContainerLowest = Color(0xFF08111C)
    private val DarkSurfaceContainerLow = Color(0xFF121F33)
    private val DarkSurfaceContainer = Color(0xFF1A2B42)
    private val DarkSurfaceContainerHigh = Color(0xFF2A3D57)
    private val DarkSurfaceContainerHighest = Color(0xFF364C6B)

    private val DarkError = Color(0xFFF87171)
    private val DarkOnError = Color(0xFF7F1D1D)
    private val DarkErrorContainer = Color(0xFF991B1B)
    private val DarkOnErrorContainer = Color(0xFFFECACA)

    private val DarkOutline = Color(0xFF3A4C63)
    private val DarkOutlineVariant = Color(0xFF2A3D57)

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
