package com.example.culturabcn.ui.contacto

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactoViewModel: ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is contacto Fragment"
    }
    val text: LiveData<String> = _text

}