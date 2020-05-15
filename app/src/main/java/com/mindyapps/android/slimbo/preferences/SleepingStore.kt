package com.mindyapps.android.slimbo.preferences

import android.content.SharedPreferences


class SleepingStore(preferences: SharedPreferences) {

    var lullaby: String by PreferencesDelegate(
        preferences,
        LULLABY,
        ""
    )
    var isWorking: Boolean by PreferencesDelegate(
        preferences,
        IS_WORKING,
        false
    )


    companion object {
        private const val LULLABY = "lullaby"
        private const val IS_WORKING = "is_working"
    }
}