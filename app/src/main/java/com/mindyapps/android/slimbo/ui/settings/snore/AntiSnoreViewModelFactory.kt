package com.mindyapps.android.slimbo.ui.settings.snore

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mindyapps.android.slimbo.data.repository.SlimboRepository

class AntiSnoreViewModelFactory(
    private val slimboRepository: SlimboRepository,
    private val application: Application
): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  AntiSnoreViewModel(
            slimboRepository,
            application
        ) as T
    }
}