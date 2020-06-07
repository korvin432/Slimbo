package com.mindyapps.slimbo.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mindyapps.slimbo.data.db.SlimboDao
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.Recording

class SlimboRepositoryImpl : SlimboRepository {

    override fun getFactors(slimboDao: SlimboDao): LiveData<List<Factor>> {
        return slimboDao.getFactors()
    }

    override fun getMusic(slimboDao: SlimboDao): LiveData<List<Music>> {
        return slimboDao.getAllMusic()
    }

    override fun getRelaxMusic(slimboDao: SlimboDao): LiveData<List<Music>> {
        return slimboDao.getRelaxMusic()
    }

    override fun getAlarms(slimboDao: SlimboDao): LiveData<List<Music>> {
        return slimboDao.getAlarms()
    }

    override fun insertRecording(slimboDao: SlimboDao, recording: Recording): Long {
        return slimboDao.insertRecording(recording)
    }

    override fun updateRecording(slimboDao: SlimboDao, recId: Int, newRating: Int) {
        return slimboDao.updateRecording(recId, newRating)
    }

    override fun getRecording(slimboDao: SlimboDao, recId: Int): LiveData<Recording> {
        return slimboDao.getRecording(recId)
    }

    override fun getAllRecordings(slimboDao: SlimboDao): LiveData<List<Recording>> {
        return slimboDao.getAllRecordings()
    }

    override fun getRecordingsBetween(
        slimboDao: SlimboDao, startTime: Long, endTime: Long
    ): LiveData<List<Recording>> {
        return slimboDao.getRecordingsBetween(startTime, endTime)
    }
}