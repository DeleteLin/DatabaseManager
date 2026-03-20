package space.xiaoxiao.databasemanager.utils

/**
 * JVM 桌面平台软键盘工具实现
 * Desktop 环境无需处理软键盘
 */
actual object KeyboardUtils {
    /**
     * 收起软键盘
     * Desktop 环境无需操作
     */
    actual fun hideKeyboard() {
        // Desktop 环境无需收起软键盘
    }
}
