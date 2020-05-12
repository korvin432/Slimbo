package com.mindyapps.android.slimbo.internal

import android.content.SharedPreferences


class AlarmStore(preferences: SharedPreferences) {

    var useAlarm: Boolean by PreferencesDelegate(preferences, USE_ALARM, false)
    var alarmTime: String by PreferencesDelegate(preferences, ALARM_TIME, "00:00")
    var repeatDays: String by PreferencesDelegate(preferences, REPEAT_DAYS, "")
    var alarmSound: String by PreferencesDelegate(preferences, ALARM_SOUND, "")

    companion object {
        private const val USE_ALARM = "use_alarm"
        private const val ALARM_TIME = "alarm_time"
        private const val REPEAT_DAYS = "repeat_days"
        private const val ALARM_SOUND = "alarm_sound"
    }
}