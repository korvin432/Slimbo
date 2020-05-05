package com.mindyapps.android.slimbo.ui.dialogs.music_select

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mindyapps.android.slimbo.data.repository.SlimboRepository

class SelectMusicViewModelFactory(
    private val slimboRepository: SlimboRepository,
    private val application: Application
): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return  SelectMusicViewModel(slimboRepository, application) as T
    }
}