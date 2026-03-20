package space.xiaoxiao.databasemanager.utils

import android.app.Activity

actual object AppExit {
    private var activityRef: Activity? = null

    fun init(activity: Activity) {
        activityRef = activity
    }

    actual fun exitApp() {
        val activity = activityRef ?: return
        activity.finishAffinity()
    }
}

