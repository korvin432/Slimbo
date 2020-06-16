package com.mindyapps.asleep.ui.settings.alarm

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.asleep.data.db.SlimboDatabase
import com.mindyapps.asleep.data.model.Music
import com.mindyapps.asleep.data.repository.SlimboRepository

class AlarmViewModel(
    slimboRepository: SlimboRepository,
    application: Application
) : ViewModel() {

    val allAlarms: LiveData<List<Music>>

    init {
        val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()
        allAlarms = slimboRepository.getAlarms(slimboDao)
    }
}