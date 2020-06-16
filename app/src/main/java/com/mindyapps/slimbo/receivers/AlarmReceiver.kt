package com.mindyapps.slimbo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.mindyapps.slimbo.RecorderService
import com.mindyapps.slimbo.internal.NotificationUtils
import com.mindyapps.slimbo.preferences.AlarmStore
import com.mindyapps.slimbo.ui.sleeping.SleepingActivity


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (AlarmStore(PreferenceManager.getDefaultSharedPreferences(context.applicationContext)).useAlarm) {
            val notificationUtils = NotificationUtils(context)
            val notification = notificationUtils.getNotificationBuilder().build()
            notificationUtils.getManager().notify(150, notification)

            val stopIntent = Intent(context, RecorderService::class.java)
            stopIntent.action = RecorderService.STOP_ACTION
            stopIntent.putExtra(SleepingActivity.FROM_ALARM, true)
            context.startService(stopIntent)
        }
    }
}