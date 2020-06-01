package com.mindyapps.slimbo.ui.relax

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.slimbo.data.db.SlimboDatabase
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.repository.SlimboRepository

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