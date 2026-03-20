package space.xiaoxiao.databasemanager.utils

import kotlin.system.exitProcess

actual object AppExit {
    actual fun exitApp() {
        exitProcess(0)
    }
}

