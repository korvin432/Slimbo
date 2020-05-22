package com.mindyapps.android.slimbo.preferences

import android.content.SharedPreferences


class SleepingStore(preferences: SharedPreferences) {

    var isWorking: Boolean by PreferencesDelegate(preferences,IS_WORKING,false)
    var showTip: Boolean by PreferencesDelegate(preferences,SHOW_TIP,true)
    var minimalTimeReached: Boolean by PreferencesDelegate(preferences,MINIMUM_REACHED,false)

    companion object {
        private const val IS_WORKING = "is_working"
        private const val SHOW_TIP = "show_tip"
        private const val MINIMUM_REACHED = "min_reached"
    }
}