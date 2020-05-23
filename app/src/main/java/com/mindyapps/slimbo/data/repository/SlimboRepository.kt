package com.mindyapps.slimbo.data.repository

import androidx.lifecycle.LiveData
import com.mindyapps.slimbo.data.db.SlimboDao
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Music

interface SlimboRepository {
    fun getFactors(slimboDao: SlimboDao): LiveData<List<Factor>>
    fun getMusic(slimboDao: SlimboDao): LiveData<List<Music>>
    fun getAlarms(slimboDao: SlimboDao): LiveData<List<Music>>
}