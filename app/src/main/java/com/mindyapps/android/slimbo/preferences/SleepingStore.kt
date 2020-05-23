package com.mindyapps.android.slimbo.preferences

import android.content.SharedPreferences


class SleepingStore(preferences: SharedPreferences) {

    var isWorking: Boolean by PreferencesDelegate(preferences,IS_WORKING,false)
    var showTip: Boolean by PreferencesDelegate(preferences,SHOW_TIP,true)
    var minimalTimeReached: Boolean by PreferencesDelegate(preferences,MINIMUM_REACHED,false)
    var useAntiSnore: Boolean by PreferencesDelegate(preferences,USE_ANTI_SNORE,false)
    var antiSnoreSound: String by PreferencesDelegate(preferences,ANTI_SNORE_SOUND,"")
    var antiSnoreDuration: Int by PreferencesDelegate(preferences,ANTI_SNORE_DURATION,1)

    companion object {
        private const val IS_WORKING = "is_working"
        private const val SHOW_TIP = "show_tip"
        private const val MINIMUM_REACHED = "min_reached"
        private const val USE_ANTI_SNORE = "use_anti_snore"
        private const val ANTI_SNORE_SOUND = "anti_snore_sound"
        private const val ANTI_SNORE_DURATION = "anti_snore_duration"
    }
}