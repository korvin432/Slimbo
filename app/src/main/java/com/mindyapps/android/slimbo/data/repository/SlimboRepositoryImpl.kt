package com.mindyapps.android.slimbo.data.repository

import androidx.lifecycle.LiveData
import com.mindyapps.android.slimbo.data.db.SlimboDao
import com.mindyapps.android.slimbo.data.model.Factor

class SlimboRepositoryImpl : SlimboRepository {

    override fun getFactors(slimboDao: SlimboDao): LiveData<List<Factor>> {
        return slimboDao.getFactors()
    }
}