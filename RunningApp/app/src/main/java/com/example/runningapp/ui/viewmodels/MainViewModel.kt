package com.example.runningapp.ui.viewmodels


import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapp.db.Run
import com.example.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
):ViewModel(){
    private val runsSortedByDate=mainRepository.getAllRunsByDate()
    private val runsSortedByDistance=mainRepository.getAllRunsByDistance()
    private val runsSortedByBurnedCalories=mainRepository.getAllRunsByBurnedCalories()
    private val runsSortedByTimeInMillisec=mainRepository.getAllRunsByTimeMillisec()
    private val runsSortedByAvgSpeed=mainRepository.getAllRunsByAvgSpeed()

    val runs=MediatorLiveData<List<Run>>()

    fun insertRun(run: Run)=viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}