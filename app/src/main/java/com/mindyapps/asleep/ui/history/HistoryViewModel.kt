package com.mindyapps.asleep.ui.history

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.asleep.data.db.SlimboDatabase
import com.mindyapps.asleep.data.model.Recording
import com.mindyapps.asleep.data.repository.SlimboRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(
    val slimboRepository: SlimboRepository,
    val application: Application
) : ViewModel() {

    var allRecordings = MediatorLiveData<List<Recording>>()
    val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()

    init {
        allRecordings.addSource(slimboRepository.getAllRecordings(slimboDao)){
            allRecordings.value = it
        }
    }

    fun searchAll(){
        CoroutineScope(Dispatchers.Main).launch {
            allRecordings.addSource(slimboRepository.getAllRecordings(slimboDao)){
                allRecordings.value = it
            }
        }
    }

    fun searchRecordings(from: Long, to: Long){
        CoroutineScope(Dispatchers.Main).launch {
            allRecordings.addSource(
                slimboRepository.getRecordingsBetween(
                    slimboDao,
                    to,
                    from
                )
            ) { allRecordings.value = it }
        }
    }
}