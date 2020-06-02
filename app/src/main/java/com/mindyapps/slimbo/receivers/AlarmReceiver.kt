package com.mindyapps.slimbo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.mindyapps.slimbo.internal.NotificationUtils
import com.mindyapps.slimbo.preferences.AlarmStore


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (AlarmStore(PreferenceManager.getDefaultSharedPreferences(context.applicationContext)).useAlarm) {
            val notificationUtils = NotificationUtils(context)
            val notification = notificationUtils.getNotificationBuilder().build()
            notificationUtils.getManager().notify(150, notification)

            val i = Intent()
            i.setClassName(
                "com.mindyapps.slimbo",
                "com.mindyapps.slimbo.ui.AlarmActivity"
            )
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }
    }
}