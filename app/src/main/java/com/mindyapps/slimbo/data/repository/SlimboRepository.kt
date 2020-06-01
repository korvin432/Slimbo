package com.mindyapps.slimbo.data.repository

import androidx.lifecycle.LiveData
import com.mindyapps.slimbo.data.db.SlimboDao
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.Recording

interface SlimboRepository {
    fun getFactors(slimboDao: SlimboDao): LiveData<List<Factor>>
    fun getMusic(slimboDao: SlimboDao): LiveData<List<Music>>
    fun getRelaxMusic(slimboDao: SlimboDao): LiveData<List<Music>>
    fun getAlarms(slimboDao: SlimboDao): LiveData<List<Music>>
    fun insertRecording(slimboDao: SlimboDao, recording: Recording): Long
    fun updateRecording(slimboDao: SlimboDao, recId: Int, newRating: Int)
    fun getRecording(slimboDao: SlimboDao, recId: Int): LiveData<Recording>
    fun getAllRecordings(slimboDao: SlimboDao): LiveData<List<Recording>>
}