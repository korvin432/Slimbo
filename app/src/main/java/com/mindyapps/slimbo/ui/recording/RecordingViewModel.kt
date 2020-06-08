package com.mindyapps.slimbo.ui.recording

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.slimbo.data.db.SlimboDao
import com.mindyapps.slimbo.data.db.SlimboDatabase
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecordingViewModel(val slimboRepository: SlimboRepository, application: Application) :
    ViewModel() {

    val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()

    fun updateRecording(id: Int, rating: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            slimboRepository.updateRecording(slimboDao, id, rating)
        }
    }

    fun deleteRecording(recording: Recording) {
        CoroutineScope(Dispatchers.IO).launch {
            slimboRepository.deleteRecording(slimboDao, recording)
        }
    }
}
