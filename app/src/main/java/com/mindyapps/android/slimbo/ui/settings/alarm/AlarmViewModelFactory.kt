package com.mindyapps.android.slimbo.ui.settings.alarm

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mindyapps.android.slimbo.data.repository.SlimboRepository

class AlarmViewModelFactory(
    private val slimboRepository: SlimboRepository,
    private val application: Application
): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  AlarmViewModel(
            slimboRepository,
            application
        ) as T
    }
}