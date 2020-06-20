package com.mindyapps.asleep.ui.sleep

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mindyapps.asleep.data.repository.SlimboRepository
import com.mindyapps.asleep.ui.history.HistoryViewModel

class SleepViewModelFactory(
    private val slimboRepository: SlimboRepository,
    private val application: Application
): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  SleepViewModel(slimboRepository, application) as T
    }
}