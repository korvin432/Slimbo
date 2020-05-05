package com.mindyapps.android.slimbo.ui.dialogs.music_select

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.android.slimbo.data.db.SlimboDatabase
import com.mindyapps.android.slimbo.data.model.Music
import com.mindyapps.android.slimbo.data.repository.SlimboRepository

class SelectMusicViewModel(
    slimboRepository: SlimboRepository,
    application: Application
) : ViewModel() {

    val allMusic: LiveData<List<Music>>

    init {
        val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()
        allMusic = slimboRepository.getMusic(slimboDao)
    }
}
