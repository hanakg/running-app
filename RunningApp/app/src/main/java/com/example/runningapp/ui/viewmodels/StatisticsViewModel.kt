package com.example.runningapp.ui.viewmodels


import androidx.lifecycle.ViewModel
import com.example.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
):ViewModel(){
    val totalTimeRun=mainRepository.getTotalTimeMillisec()
    val totalDistance=mainRepository.getTotalDistance()
    val totalBurnedCalories=mainRepository.getTotalBurnedCalories()
    val totalAvgSpeed=mainRepository.getTotalAvgSpeed()
    val totalMaxSpeed=mainRepository.getTotalMaxSpeed()
    val totalMinSpeed=mainRepository.getTotalMinSpeed()
    val runsCountByYear=mainRepository.getRunsCountByYear()
    val getTimeStampt=mainRepository.getTimeStampt()

    val runsSortedByDate=mainRepository.getAllRunsByDate()
}