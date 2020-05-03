package com.mindyapps.android.slimbo.ui.factors

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mindyapps.android.slimbo.data.repository.SlimboRepository

class FactorsViewModelFactory(
    private val slimboRepository: SlimboRepository,
    private val application: Application
): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  FactorsViewModel(slimboRepository, application) as T
    }
}