package com.example.runningapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.runningapp.db.Run

class SharedViewModel:ViewModel() {
    private val _position=MutableLiveData<Int>(0)
    val position: LiveData<Int> = _position

    private val _selectedRun=MutableLiveData<Run>(Run())
    val selectedRun: LiveData<Run> = _selectedRun

    fun setPosition(position: Int){
        _position.value=position
    }

    fun setSelectedRun(selectedRun:Run){
        _selectedRun.value=selectedRun
    }
}