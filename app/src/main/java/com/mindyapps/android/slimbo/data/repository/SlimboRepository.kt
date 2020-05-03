package com.mindyapps.android.slimbo.data.repository

import androidx.lifecycle.LiveData
import com.mindyapps.android.slimbo.data.db.SlimboDao
import com.mindyapps.android.slimbo.data.model.Factor

interface SlimboRepository {
    fun getFactors(slimboDao: SlimboDao): LiveData<List<Factor>>
}