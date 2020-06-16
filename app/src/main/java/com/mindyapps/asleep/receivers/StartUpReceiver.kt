package com.mindyapps.asleep.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
import androidx.preference.PreferenceManager
import com.mindyapps.asleep.preferences.AlarmStore
import java.util.*


class StartUpReceiver : BroadcastReceiver() {

    private lateinit var alarmStore: AlarmStore

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("qwwe", "onReceive")
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            setAlarm(context)
        }
    }

    private fun setAlarm(context: Context) {
        Log.d("qwwe", "setAlarm")
        try {
            alarmStore = AlarmStore(
                PreferenceManager
                    .getDefaultSharedPreferences(context.applicationContext)
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val selectedDays = alarmStore.repeatDays.replace(" ", "")
            val selectedTime = alarmStore.alarmTime
            val intList = convertFromString(selectedDays) { it.toInt() }
            val AM_PM = if (selectedTime.substringBefore(":").toInt() < 12) {
                "AM"
            } else {
                "PM"
            }

            intList.forEach {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, it + 1)
                if (DateFormat.is24HourFormat(context)) {
                    calendar.set(Calendar.HOUR_OF_DAY, selectedTime.substringBefore(":").toInt())
                } else {
                    calendar.set(Calendar.HOUR, selectedTime.substringBefore(":").toInt())
                    if (AM_PM == "AM") {
                        calendar.set(Calendar.AM_PM, 0)
                    } else {
                        calendar.set(Calendar.AM_PM, 1)
                    }
                }
                val min = selectedTime.substringBefore(" ")
                calendar.set(Calendar.MINUTE, min.substringAfter(":").toInt())
                if (Calendar.getInstance().timeInMillis > calendar.timeInMillis) {
                    calendar.add(Calendar.DATE, 7)
                }
                val intent = Intent(context, AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, it + 1, intent, 0)
                Log.d(
                    "qwwe",
                    "set alarm on ${DateFormat.format(
                        "dd/MM/yyyy hh:mm:ss",
                        calendar.timeInMillis
                    )}"
                )
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    24 * 7 * 60 * 60 * 1000,
                    pendingIntent
                )
            }
        } catch (ex: Exception) {
            Log.d("qwwe", "error: ${ex.printStackTrace()} \n ${ex.message} \n $ex")
        }

    }

    private inline fun <T : Any> convertFromString(
        myString: String,
        transformation: (String) -> T
    ) = myString.trim().split(",").map(transformation).toList()
}