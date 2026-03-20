package space.xiaoxiao.databasemanager.theme

import space.xiaoxiao.databasemanager.theme.colors.AppColorScheme
import space.xiaoxiao.databasemanager.theme.colors.BlueColors
import space.xiaoxiao.databasemanager.theme.colors.DarkBlackColors
import space.xiaoxiao.databasemanager.theme.colors.GreenColors
import space.xiaoxiao.databasemanager.theme.colors.PinkColors
import space.xiaoxiao.databasemanager.theme.colors.PurpleColors
import space.xiaoxiao.databasemanager.theme.colors.SlateOrangeColors

/**
 * 颜色主题枚举
 * 每个主题都是独立的配色方案
 *
 * 添加新主题步骤：
 * 1. 在 colors/ 目录创建颜色文件，实现 AppColorScheme 接口
 * 2. 在此枚举中添加新主题
 */
enum class ColorTheme(
    val id: String,
    val colorScheme: AppColorScheme,
    val isDark: Boolean = false
) {
    GREEN("green", GreenColors, isDark = false),
    BLUE("blue", BlueColors, isDark = false),
    PURPLE("purple", PurpleColors, isDark = false),
    PINK("pink", PinkColors, isDark = false),
    SLATE_ORANGE("slate_orange", SlateOrangeColors, isDark = false),
    DARK_BLACK("dark_black", DarkBlackColors, isDark = true);

    companion object {
        /**
         * 根据 ID 获取颜色主题，找不到时返回默认绿色主题
         */
        fun fromId(id: String?): ColorTheme {
            return entries.find { it.id == id } ?: GREEN
        }
    }
}