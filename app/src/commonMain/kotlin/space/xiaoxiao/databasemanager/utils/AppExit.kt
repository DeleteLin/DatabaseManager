package space.xiaoxiao.databasemanager.utils

/**
 * 跨平台退出应用
 *
 * - Android: 需要在 MainActivity 中调用 init(activity)
 * - Desktop: 直接 exitProcess
 */
expect object AppExit {
    fun exitApp()
}

