package com.mindyapps.asleep.ui.dialogs.factors

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mindyapps.asleep.data.db.SlimboDatabase
import com.mindyapps.asleep.data.model.Factor
import com.mindyapps.asleep.data.repository.SlimboRepository

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
