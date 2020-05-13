package com.mindyapps.android.slimbo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mindyapps.android.slimbo.internal.NotificationUtils


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationUtils = NotificationUtils(context)
        val notification = notificationUtils.getNotificationBuilder().build()
        notificationUtils.getManager().notify(150, notification)

        val i = Intent()
        i.setClassName(
            "com.mindyapps.android.slimbo",
            "com.mindyapps.android.slimbo.ui.AlarmActivity"
        )
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(i)
    }
}