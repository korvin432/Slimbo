package com.mindyapps.android.slimbo.ui.dialogs.alarm

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.android.slimbo.data.db.SlimboDatabase
import com.mindyapps.android.slimbo.data.model.Music
import com.mindyapps.android.slimbo.data.repository.SlimboRepository

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
