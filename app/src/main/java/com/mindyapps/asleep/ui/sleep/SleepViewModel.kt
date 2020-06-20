package com.mindyapps.asleep.ui.sleep

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.asleep.data.db.SlimboDatabase
import com.mindyapps.asleep.data.model.Recording
import com.mindyapps.asleep.data.repository.SlimboRepository

class SleepViewModel(
    val slimboRepository: SlimboRepository,
    val application: Application
) : ViewModel() {

    var recordingsCount = MediatorLiveData<Int>()
    val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()

    init {
        recordingsCount.addSource(slimboRepository.getRecordingsCount(slimboDao)){
            recordingsCount.value = it
        }
    }

}