package com.mindyapps.slimbo.ui.history

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.slimbo.data.db.SlimboDatabase
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepository
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