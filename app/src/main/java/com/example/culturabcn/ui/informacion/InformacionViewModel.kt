package com.example.culturabcn.ui.informacion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InformacionViewModel: ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is informacion Fragment"
    }
    val text: LiveData<String> = _text

}