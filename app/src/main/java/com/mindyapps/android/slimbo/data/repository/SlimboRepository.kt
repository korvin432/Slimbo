package com.mindyapps.android.slimbo.data.repository

import androidx.lifecycle.LiveData
import com.mindyapps.android.slimbo.data.db.SlimboDao
import com.mindyapps.android.slimbo.data.model.Factor
import com.mindyapps.android.slimbo.data.model.Music

interface SlimboRepository {
    fun getFactors(slimboDao: SlimboDao): LiveData<List<Factor>>
    fun getMusic(slimboDao: SlimboDao): LiveData<List<Music>>
}