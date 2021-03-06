package com.mindyapps.asleep.ui.recording

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mindyapps.asleep.data.repository.SlimboRepository

class RecordingViewModelFactory(
    private val application: Application,
    val slimboRepository: SlimboRepository
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecordingViewModel(slimboRepository, application) as T
    }
}