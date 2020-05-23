package com.mindyapps.slimbo.ui.dialogs.music_select

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.slimbo.data.db.SlimboDatabase
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.repository.SlimboRepository

class SelectMusicViewModel(
    val slimboRepository: SlimboRepository,
    val application: Application
) : ViewModel() {

    var allMusic: LiveData<List<Music>>

    init {
        val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()
        allMusic = slimboRepository.getMusic(slimboDao)
    }

    fun updateMusic(){
        val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()
        allMusic = slimboRepository.getMusic(slimboDao)
    }
}
