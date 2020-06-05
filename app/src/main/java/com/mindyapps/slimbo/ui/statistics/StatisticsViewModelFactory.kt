package com.mindyapps.slimbo.ui.statistics

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mindyapps.slimbo.data.repository.SlimboRepository

class StatisticsViewModelFactory(
    private val slimboRepository: SlimboRepository,
    private val application: Application
): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  StatisticsViewModel(slimboRepository, application) as T
    }
}