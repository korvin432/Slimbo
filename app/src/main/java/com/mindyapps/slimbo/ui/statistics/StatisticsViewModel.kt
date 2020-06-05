package com.mindyapps.slimbo.ui.statistics

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.slimbo.data.db.SlimboDatabase
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepository
import java.util.*


class StatisticsViewModel(
    val slimboRepository: SlimboRepository,
    val application: Application
) : ViewModel() {

    var recordings: LiveData<List<Recording>>

    init {
        val oneDayInMills = 86400000
        val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()
        recordings = slimboRepository.getRecordingsBetween(
            slimboDao,
            System.currentTimeMillis(),
            System.currentTimeMillis() - oneDayInMills * 7
        )
    }

}
