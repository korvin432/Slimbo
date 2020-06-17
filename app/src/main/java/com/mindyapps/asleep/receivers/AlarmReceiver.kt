package com.mindyapps.asleep.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.preference.PreferenceManager
import com.mindyapps.asleep.RecorderService
import com.mindyapps.asleep.internal.NotificationUtils
import com.mindyapps.asleep.preferences.AlarmStore
import com.mindyapps.asleep.preferences.SleepingStore
import com.mindyapps.asleep.ui.AlarmActivity
import com.mindyapps.asleep.ui.sleeping.SleepingActivity


class AlarmReceiver : BroadcastReceiver() {

    private lateinit var sleepingStore: SleepingStore

    override fun onReceive(context: Context, intent: Intent) {
        if (AlarmStore(PreferenceManager.getDefaultSharedPreferences(context.applicationContext)).useAlarm) {
            val notificationUtils = NotificationUtils(context)
            val notification = notificationUtils.getNotificationBuilder().build()
            notificationUtils.getManager().notify(150, notification)

            sleepingStore = SleepingStore(
                PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
            )

            if (sleepingStore.isWorking) {
                val stopIntent = Intent(context, RecorderService::class.java)
                stopIntent.action = RecorderService.STOP_ACTION
                stopIntent.putExtra(SleepingActivity.FROM_ALARM, true)
                context.startService(stopIntent)
            } else {
                Log.d("qwwe", "not working")
                val i = Intent()
                i.setClassName(
                    "com.mindyapps.asleep",
                    "com.mindyapps.asleep.ui.AlarmActivity"
                )
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(i)
            }
        }
    }
}