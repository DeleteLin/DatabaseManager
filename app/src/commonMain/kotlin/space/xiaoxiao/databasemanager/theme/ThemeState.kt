package space.xiaoxiao.databasemanager.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 主题状态管理
 * 管理颜色主题
 */
class ThemeState {
    // 颜色主题
    private val _colorTheme = MutableStateFlow(ColorTheme.GREEN)
    val colorTheme: StateFlow<ColorTheme> = _colorTheme.asStateFlow()

    // 是否为暗色模式（便捷属性，从颜色主题派生）
    val darkTheme: StateFlow<Boolean>
        get() = MutableStateFlow(_colorTheme.value.isDark).asStateFlow()

    // 是否已加载配置
    var isLoaded: Boolean = false
        private set

    /**
     * 从配置加载主题设置
     * @param colorThemeId 颜色主题ID
     */
    fun loadFromConfig(colorThemeId: String?) {
        _colorTheme.value = ColorTheme.fromId(colorThemeId)
        isLoaded = true
    }

    /**
     * 设置颜色主题
     */
    fun setColorTheme(theme: ColorTheme) {
        _colorTheme.value = theme
    }

    /**
     * 获取颜色主题配置ID
     */
    fun toConfigColorTheme(): String {
        return _colorTheme.value.id
    }
}