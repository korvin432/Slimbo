package com.mindyapps.slimbo.data.repository

import androidx.lifecycle.LiveData
import com.mindyapps.slimbo.data.db.SlimboDao
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Music

class SlimboRepositoryImpl : SlimboRepository {

    override fun getFactors(slimboDao: SlimboDao): LiveData<List<Factor>> {
        return slimboDao.getFactors()
    }

    override fun getMusic(slimboDao: SlimboDao): LiveData<List<Music>> {
        return slimboDao.getAllMusic()
    }

    override fun getAlarms(slimboDao: SlimboDao): LiveData<List<Music>> {
        return slimboDao.getAlarms()
    }
}