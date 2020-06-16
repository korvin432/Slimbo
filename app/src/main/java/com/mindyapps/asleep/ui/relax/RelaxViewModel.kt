package com.mindyapps.asleep.ui.relax

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.asleep.data.db.SlimboDatabase
import com.mindyapps.asleep.data.model.Music
import com.mindyapps.asleep.data.repository.SlimboRepository

class RelaxViewModel(
    val slimboRepository: SlimboRepository,
    val application: Application
) : ViewModel() {

    var allMusic: LiveData<List<Music>>

    init {
        val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()
        allMusic = slimboRepository.getRelaxMusic(slimboDao)
    }
}