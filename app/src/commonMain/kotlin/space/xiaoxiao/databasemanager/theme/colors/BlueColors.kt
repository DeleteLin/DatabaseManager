package space.xiaoxiao.databasemanager.theme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 蓝色主题 - 双色调设计
 *
 * 设计灵感：海洋深处
 * - 背景：深蓝到浅蓝的渐变层次
 * - 主色：明亮蔚蓝作为视觉焦点
 *
 * 配色方案：
 * - 亮色主题 "Ocean Mist"：浅蓝灰背景 + 蔚蓝主色
 * - 暗色主题 "Deep Ocean"：深海蓝背景 + 亮蓝主色
 */
object BlueColors : AppColorScheme {
    override val id = "blue"

    // ==================== 亮色主题 "Ocean Mist" ====================
    private val LightBackground = Color(0xFFF0F4F8)        // 极浅蓝灰
    private val LightOnBackground = Color(0xFF1A2938)      // 深蓝黑

    private val LightSurface = Color(0xFFFFFFFF)
    private val LightOnSurface = Color(0xFF1F3347)         // 深蓝

    private val LightSurfaceVariant = Color(0xFFE0E8EF)    // 浅蓝灰
    private val LightOnSurfaceVariant = Color(0xFF3A4858)  // 中蓝灰

    // 主色调：饱和蔚蓝
    private val LightPrimary = Color(0xFF3B82F6)           // Blue 500
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFF93C5FD)
    private val LightOnPrimaryContainer = Color(0xFF1E3A8A)

    // 辅助色：天蓝色
    private val LightSecondary = Color(0xFF0EA5E9)         // Sky 500
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFFBAE6FD)
    private val LightOnSecondaryContainer = Color(0xFF0C4A6E)

    // 第三色：蓝紫色点缀
    private val LightTertiary = Color(0xFF8B5CF6)          // Violet 500
    private val LightOnTertiary = Color(0xFFFFFFFF)
    private val LightTertiaryContainer = Color(0xFFDDD6FE)
    private val LightOnTertiaryContainer = Color(0xFF5B21B6)

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

    // ==================== 暗色主题 "Deep Ocean" ====================
    private val DarkBackground = Color(0xFF0C1929)         // 深海蓝
    private val DarkOnBackground = Color(0xFFD8E2EE)       // 浅蓝白

    private val DarkSurface = Color(0xFF1A2B42)            // 深蓝表面
    private val DarkOnSurface = Color(0xFFC8D5E5)          // 浅蓝文字

    private val DarkSurfaceVariant = Color(0xFF2A3D57)     // 蓝灰变体
    private val DarkOnSurfaceVariant = Color(0xFFB8C8D9)   // 中浅蓝

    // 主色调：明亮蔚蓝
    private val DarkPrimary = Color(0xFF60A5FA)            // Blue 400
    private val DarkOnPrimary = Color(0xFF1E3A8A)
    private val DarkPrimaryContainer = Color(0xFF1D4ED8)
    private val DarkOnPrimaryContainer = Color(0xFFDBEAFE)

    // 辅助色
    private val DarkSecondary = Color(0xFF38BDF8)          // Sky 400
    private val DarkOnSecondary = Color(0xFF0C4A6E)
    private val DarkSecondaryContainer = Color(0xFF0369A1)
    private val DarkOnSecondaryContainer = Color(0xFFE0F2FE)

    // 第三色
    private val DarkTertiary = Color(0xFFA78BFA)           // Violet 400
    private val DarkOnTertiary = Color(0xFF5B21B6)
    private val DarkTertiaryContainer = Color(0xFF6D28D9)
    private val DarkOnTertiaryContainer = Color(0xFFEDE9FE)

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
