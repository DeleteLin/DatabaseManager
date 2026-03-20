package space.xiaoxiao.databasemanager.theme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 粉色主题 - 双色调设计
 *
 * 设计灵感：玫瑰花园
 * - 背景：深粉到浅粉的渐变层次
 * - 主色：鲜明玫瑰粉作为视觉焦点
 *
 * 配色方案：
 * - 亮色主题 "Rose Mist"：浅粉灰背景 + 玫瑰粉主色
 * - 暗色主题 "Rose Night"：深粉背景 + 樱花粉主色
 */
object PinkColors : AppColorScheme {
    override val id = "pink"

    // ==================== 亮色主题 "Rose Mist" ====================
    private val LightBackground = Color(0xFFF8F3F5)        // 极浅粉灰
    private val LightOnBackground = Color(0xFF2F1F26)      // 深粉黑

    private val LightSurface = Color(0xFFFFFFFF)
    private val LightOnSurface = Color(0xFF382530)         // 深粉

    private val LightSurfaceVariant = Color(0xFFEBE3E7)    // 浅粉灰
    private val LightOnSurfaceVariant = Color(0xFF523D48)  // 中粉灰

    // 主色调：饱和玫瑰粉
    private val LightPrimary = Color(0xFFEC4899)           // Pink 500
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFFF9A8D8)
    private val LightOnPrimaryContainer = Color(0xFF9D174D)

    // 辅助色：玫瑰红
    private val LightSecondary = Color(0xFFF43F5E)         // Rose 500
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFFFECDD5)
    private val LightOnSecondaryContainer = Color(0xFF881337)

    // 第三色：橙色点缀
    private val LightTertiary = Color(0xFFF97316)          // Orange 500
    private val LightOnTertiary = Color(0xFFFFFFFF)
    private val LightTertiaryContainer = Color(0xFFFFEDD5)
    private val LightOnTertiaryContainer = Color(0xFF9A3412)

    // Surface Container 系列 - 粉灰层级
    private val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
    private val LightSurfaceContainerLow = Color(0xFFFAF8F9)
    private val LightSurfaceContainer = Color(0xFFF8F3F5)
    private val LightSurfaceContainerHigh = Color(0xFFEBE3E7)
    private val LightSurfaceContainerHighest = Color(0xFFDED3D8)

    private val LightError = Color(0xFFDC2626)
    private val LightOnError = Color(0xFFFFFFFF)
    private val LightErrorContainer = Color(0xFFFEE2E2)
    private val LightOnErrorContainer = Color(0xFF7F1D1D)

    private val LightOutline = Color(0xFFB8A3AC)
    private val LightOutlineVariant = Color(0xFFDED3D8)

    // ==================== 暗色主题 "Rose Night" ====================
    private val DarkBackground = Color(0xFF1F0F16)         // 深粉黑
    private val DarkOnBackground = Color(0xFFE8E2E5)       // 浅粉白

    private val DarkSurface = Color(0xFF382230)            // 深粉表面
    private val DarkOnSurface = Color(0xFFD8D0D5)          // 浅粉文字

    private val DarkSurfaceVariant = Color(0xFF4F3342)     // 粉灰变体
    private val DarkOnSurfaceVariant = Color(0xFFC8BCC5)   // 中浅粉

    // 主色调：柔和樱花粉
    private val DarkPrimary = Color(0xFFF472B6)            // Pink 400
    private val DarkOnPrimary = Color(0xFF9D174D)
    private val DarkPrimaryContainer = Color(0xFFDB2777)
    private val DarkOnPrimaryContainer = Color(0xFFFDF2F8)

    // 辅助色
    private val DarkSecondary = Color(0xFFFB7185)          // Rose 400
    private val DarkOnSecondary = Color(0xFF881337)
    private val DarkSecondaryContainer = Color(0xFFBE123C)
    private val DarkOnSecondaryContainer = Color(0xFFFEE2E2)

    // 第三色
    private val DarkTertiary = Color(0xFFFB923C)           // Orange 400
    private val DarkOnTertiary = Color(0xFF9A3412)
    private val DarkTertiaryContainer = Color(0xFFEA580C)
    private val DarkOnTertiaryContainer = Color(0xFFFFEDD5)

    // Surface Container 系列 - 深粉层级
    private val DarkSurfaceContainerLowest = Color(0xFF140910)
    private val DarkSurfaceContainerLow = Color(0xFF251320)
    private val DarkSurfaceContainer = Color(0xFF382230)
    private val DarkSurfaceContainerHigh = Color(0xFF4F3342)
    private val DarkSurfaceContainerHighest = Color(0xFF664555)

    private val DarkError = Color(0xFFF87171)
    private val DarkOnError = Color(0xFF7F1D1D)
    private val DarkErrorContainer = Color(0xFF991B1B)
    private val DarkOnErrorContainer = Color(0xFFFECACA)

    private val DarkOutline = Color(0xFF5C4250)
    private val DarkOutlineVariant = Color(0xFF4F3342)

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
