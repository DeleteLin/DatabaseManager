package space.xiaoxiao.databasemanager.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Android 平台软键盘工具实现
 */
actual object KeyboardUtils {
    /**
     * 收起软键盘
     * Desktop 环境无需操作
     */
    actual fun hideKeyboard() {
        // Android 平台通过 View 扩展方法收起键盘
        // 此函数需要在有焦点 View 的上下文中调用
        // 在 Compose 中，可以通过 LocalView.current 获取当前焦点视图
        // 由于平台限制，此处不实现具体逻辑，调用方需要在 UI 上下文中处理
    }

    /**
     * 收起软键盘 (扩展函数)
     * @param view 当前焦点视图
     */
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
