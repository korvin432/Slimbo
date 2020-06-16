package com.mindyapps.asleep.ui.statistics

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.asleep.data.db.SlimboDatabase
import com.mindyapps.asleep.data.model.Recording
import com.mindyapps.asleep.data.repository.SlimboRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.*


class StatisticsViewModel(
    val slimboRepository: SlimboRepository,
    val application: Application
) : ViewModel() {

    private val oneDayInMills = 86400000
    var recordings = MediatorLiveData<List<Recording>>()
    val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()


    init {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        recordings.addSource(
            slimboRepository.getRecordingsBetween(
                slimboDao,
                System.currentTimeMillis(),
                calendar.timeInMillis
            )
        ) {
            recordings.value = it
        }
    }

    fun setRecordings(days: Int) {
        CoroutineScope(Main).launch {
            when (days) {
                7 -> {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.clear(Calendar.MINUTE)
                    calendar.clear(Calendar.SECOND)
                    calendar.clear(Calendar.MILLISECOND)
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

                    recordings.addSource(
                        slimboRepository.getRecordingsBetween(
                            slimboDao,
                            System.currentTimeMillis(),
                            calendar.timeInMillis
                        )
                    ) { recordings.value = it }
                }
                30 -> {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.MONTH, -1)
                    recordings.addSource(
                        slimboRepository.getRecordingsBetween(
                            slimboDao,
                            System.currentTimeMillis(),
                            System.currentTimeMillis() - 2592000000
                        )
                    ) { recordings.value = it }
                }
                365 -> {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.YEAR, -1)
                    recordings.addSource(
                        slimboRepository.getRecordingsBetween(
                            slimboDao,
                            System.currentTimeMillis(),
                            System.currentTimeMillis() - 2592000000 * 12
                        )
                    ) { recordings.value = it }
                }
            }

        }

    }

}
