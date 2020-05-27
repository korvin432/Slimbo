package com.mindyapps.slimbo.ui.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mindyapps.slimbo.data.repository.SlimboRepository

class HistoryViewModelFactory(
    private val slimboRepository: SlimboRepository,
    private val application: Application
): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  HistoryViewModel(slimboRepository, application) as T
    }
}