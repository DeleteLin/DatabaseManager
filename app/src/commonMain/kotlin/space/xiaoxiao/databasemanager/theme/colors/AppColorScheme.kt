package space.xiaoxiao.databasemanager.theme.colors

import androidx.compose.material3.ColorScheme

/**
 * 颜色主题接口
 * 实现此接口以定义新的颜色主题
 *
 * 添加新主题步骤：
 * 1. 在 theme/colors/ 目录下创建新文件，如 GreenColors.kt
 * 2. 实现 AppColorScheme 接口
 * 3. 在 ColorTheme 枚举中添加新主题
 *
 * 示例：
 * ```kotlin
 * object GreenColors : AppColorScheme {
 *     override val id = "green"
 *     override val lightScheme = lightColorScheme(...)
 *     override val darkScheme = darkColorScheme(...)
 * }
 * ```
 */
interface AppColorScheme {
    /**
     * 主题唯一标识符，用于配置存储
     */
    val id: String

    /**
     * 亮色模式配色方案
     */
    val lightScheme: ColorScheme

    /**
     * 暗色模式配色方案
     */
    val darkScheme: ColorScheme
}