package com.mindyapps.slimbo.ui.settings.snore

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.slimbo.data.db.SlimboDatabase
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.repository.SlimboRepository

class AntiSnoreViewModel(
    slimboRepository: SlimboRepository,
    application: Application
) : ViewModel() {

    val allAlarms: LiveData<List<Music>>

    init {
        val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()
        allAlarms = slimboRepository.getAlarms(slimboDao)
    }
}
