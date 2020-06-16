package com.mindyapps.asleep.preferences

import android.content.SharedPreferences


class AlarmStore(preferences: SharedPreferences) {

    var useAlarm: Boolean by PreferencesDelegate(
        preferences,
        USE_ALARM,
        false
    )
    var alarmTime: String by PreferencesDelegate(
        preferences,
        ALARM_TIME,
        "00:00"
    )
    var smartAlarmTime: String by PreferencesDelegate(
        preferences,
        SMART_ALARM_TIME,
        ""
    )
    var repeatDays: String by PreferencesDelegate(
        preferences,
        REPEAT_DAYS,
        ""
    )
    var alarmSound: String by PreferencesDelegate(
        preferences,
        ALARM_SOUND,
        ""
    )
    var repeatMinutes: Int by PreferencesDelegate(
        preferences,
        REPEAT_MINUTES,
        0
    )

    companion object {
        const val USE_ALARM = "use_alarm"
        const val ALARM_TIME = "alarm_time"
        const val SMART_ALARM_TIME = "smart_alarm_time"
        private const val REPEAT_DAYS = "repeat_days"
        private const val ALARM_SOUND = "alarm_sound"
        private const val REPEAT_MINUTES = "repeat_minutes"
    }
}