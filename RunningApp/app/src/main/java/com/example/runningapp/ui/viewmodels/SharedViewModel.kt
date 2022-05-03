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

    private val _newRun=MutableLiveData<Run>(Run())
    val newRun: LiveData<Run> = _newRun

    private val _challenge=MutableLiveData<Boolean>(false)
    val challenge: LiveData<Boolean> = _challenge

    private val _goalTime=MutableLiveData<Long>(0L)
    val goalTime: LiveData<Long> = _goalTime

    private val _goalDistance=MutableLiveData<Int>(0)
    val goalDistance: LiveData<Int> = _goalDistance

    fun setPosition(position: Int){
        _position.value=position
    }

    fun setSelectedRun(selectedRun:Run){
        _selectedRun.value=selectedRun
    }

    fun setNewRun(newRun:Run){
        _newRun.value=newRun
    }

    fun setChallenge(challenge: Boolean){
        _challenge.value=challenge
    }

    fun setGoalTime(goalTime: Long){
        _goalTime.value=goalTime
    }

    fun  setGoalDistance(goalDistance: Int){
        _goalDistance.value=goalDistance
    }
}