package com.mindyapps.slimbo.ui.statistics

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.slimbo.data.db.SlimboDatabase
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class StatisticsViewModel(
    val slimboRepository: SlimboRepository,
    val application: Application
) : ViewModel() {

    private val oneDayInMills = 86400000
    var recordings = MediatorLiveData<List<Recording>>()
    val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()


    init {
        recordings.addSource(
            slimboRepository.getRecordingsBetween(
                slimboDao,
                System.currentTimeMillis(),
                System.currentTimeMillis() - oneDayInMills * 7
            )
        ) {
            recordings.value = it
        }
    }

    fun setRecordings(days: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            when (days) {
                7 -> {
                    Log.d("qwwe", "setting 7")
                    recordings.addSource(
                        slimboRepository.getRecordingsBetween(
                            slimboDao,
                            System.currentTimeMillis(),
                            System.currentTimeMillis() - oneDayInMills * 7
                        )
                    ) {
                        recordings.value = it
                    }
                }
            }

        }

    }

}
