package com.mindyapps.android.slimbo.ui.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SleepViewModelFactory: ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  SleepViewModel() as T
    }
}