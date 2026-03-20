package space.xiaoxiao.databasemanager.theme.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 绿色主题 - 双色调设计
 *
 * 设计灵感：森林秘境
 * - 背景：深青绿到浅青绿的渐变层次
 * - 主色：鲜活翠绿作为视觉焦点
 *
 * 配色方案：
 * - 亮色主题 "Mist Forest"：浅青绿背景 + 翠绿主色
 * - 暗色主题 "Deep Forest"：深墨绿背景 + 荧光绿主色
 */
object GreenColors : AppColorScheme {
    override val id = "green"

    // ==================== 亮色主题 "Mist Forest" ====================
    private val LightBackground = Color(0xFFF0F7F4)        // 极浅青绿
    private val LightOnBackground = Color(0xFF1A2E26)      // 深绿黑

    private val LightSurface = Color(0xFFFFFFFF)
    private val LightOnSurface = Color(0xFF1F3A30)         // 深绿

    private val LightSurfaceVariant = Color(0xFFE0EBE6)    // 浅青灰
    private val LightOnSurfaceVariant = Color(0xFF3A4F46)  // 中绿灰

    // 主色调：饱和翠绿
    private val LightPrimary = Color(0xFF10B981)           // Emerald 500
    private val LightOnPrimary = Color(0xFFFFFFFF)
    private val LightPrimaryContainer = Color(0xFF6EE7B7)
    private val LightOnPrimaryContainer = Color(0xFF022C22)

    // 辅助色：青绿色
    private val LightSecondary = Color(0xFF14B8A6)         // Teal 500
    private val LightOnSecondary = Color(0xFFFFFFFF)
    private val LightSecondaryContainer = Color(0xFF99F6E4)
    private val LightOnSecondaryContainer = Color(0xFF042F2E)

    // 第三色：琥珀色点缀（冷暖对比）
    private val LightTertiary = Color(0xFFF59E0B)          // Amber 500
    private val LightOnTertiary = Color(0xFFFFFFFF)
    private val LightTertiaryContainer = Color(0xFFFDE68A)
    private val LightOnTertiaryContainer = Color(0xFF78350F)

    // Surface Container 系列 - 青绿层级
    private val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
    private val LightSurfaceContainerLow = Color(0xFFF8FBF9)
    private val LightSurfaceContainer = Color(0xFFF0F7F4)
    private val LightSurfaceContainerHigh = Color(0xFFE0EBE6)
    private val LightSurfaceContainerHighest = Color(0xFFD0E0D9)

    private val LightError = Color(0xFFDC2626)
    private val LightOnError = Color(0xFFFFFFFF)
    private val LightErrorContainer = Color(0xFFFEE2E2)
    private val LightOnErrorContainer = Color(0xFF7F1D1D)

    private val LightOutline = Color(0xFF8BA899)
    private val LightOutlineVariant = Color(0xFFD0E0D9)

    // ==================== 暗色主题 "Deep Forest" ====================
    private val DarkBackground = Color(0xFF0C1F18)         // 深墨绿
    private val DarkOnBackground = Color(0xFFD8E8E0)       // 浅青白

    private val DarkSurface = Color(0xFF1A2F26)            // 深绿表面
    private val DarkOnSurface = Color(0xFFC8DCD2)          // 浅绿文字

    private val DarkSurfaceVariant = Color(0xFF2A4236)     // 绿灰变体
    private val DarkOnSurfaceVariant = Color(0xFFB8CDC2)   // 中浅绿

    // 主色调：柔和荧光绿
    private val DarkPrimary = Color(0xFF34D399)            // Emerald 400
    private val DarkOnPrimary = Color(0xFF022C22)
    private val DarkPrimaryContainer = Color(0xFF064E3B)
    private val DarkOnPrimaryContainer = Color(0xFFD1FAE5)

    // 辅助色
    private val DarkSecondary = Color(0xFF2DD4BF)          // Teal 400
    private val DarkOnSecondary = Color(0xFF042F2E)
    private val DarkSecondaryContainer = Color(0xFF115E59)
    private val DarkOnSecondaryContainer = Color(0xFF99F6E4)

    // 第三色
    private val DarkTertiary = Color(0xFFFBBF24)           // Amber 400
    private val DarkOnTertiary = Color(0xFF78350F)
    private val DarkTertiaryContainer = Color(0xFF92400E)
    private val DarkOnTertiaryContainer = Color(0xFFFDE68A)

    // Surface Container 系列 - 深绿层级
    private val DarkSurfaceContainerLowest = Color(0xFF081611)
    private val DarkSurfaceContainerLow = Color(0xFF12251D)
    private val DarkSurfaceContainer = Color(0xFF1A2F26)
    private val DarkSurfaceContainerHigh = Color(0xFF2A4236)
    private val DarkSurfaceContainerHighest = Color(0xFF365446)

    private val DarkError = Color(0xFFF87171)
    private val DarkOnError = Color(0xFF7F1D1D)
    private val DarkErrorContainer = Color(0xFF991B1B)
    private val DarkOnErrorContainer = Color(0xFFFECACA)

    private val DarkOutline = Color(0xFF3A5246)
    private val DarkOutlineVariant = Color(0xFF2A4236)

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
