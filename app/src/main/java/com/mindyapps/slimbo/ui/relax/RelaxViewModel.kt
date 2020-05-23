package com.mindyapps.slimbo.ui.relax

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RelaxViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Relax Fragment"
    }
    val text: LiveData<String> = _text
}