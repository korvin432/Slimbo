package com.mindyapps.slimbo.ui.history

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.slimbo.data.db.SlimboDatabase
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepository

class HistoryViewModel(
    val slimboRepository: SlimboRepository,
    val application: Application
) : ViewModel() {

    var allRecordings: LiveData<List<Recording>>

    init {
        val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()
        allRecordings = slimboRepository.getAllRecordings(slimboDao)
    }
}