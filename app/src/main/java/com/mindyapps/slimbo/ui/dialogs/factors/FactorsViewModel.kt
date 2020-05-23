package com.mindyapps.slimbo.ui.dialogs.factors

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.slimbo.data.db.SlimboDatabase
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.repository.SlimboRepository

class FactorsViewModel(
    slimboRepository: SlimboRepository,
    application: Application
) : ViewModel() {

    val allFactors: LiveData<List<Factor>>

    init {
        val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()
        allFactors = slimboRepository.getFactors(slimboDao)
    }
}
