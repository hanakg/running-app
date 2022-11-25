package com.example.runningapp.ui.viewmodels


import androidx.lifecycle.ViewModel
import com.example.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
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
    val distanceSumByYear=mainRepository.getDistanceSumByYear()

    val calendar = Calendar.getInstance()

    val distanceSumByMonth=mainRepository.getDistanceSumByMonth((calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.YEAR))

    val currentTimestamp = System.currentTimeMillis()

    val distanceSumLastThreeMonths=mainRepository.getDistanceSumLastThreeMonths(currentTimestamp)

    val lastRunTimeStamp=mainRepository.getLastRunTimeStamp()

    val avgDistance=mainRepository.getAvgDistance()

    val getTimeStampt=mainRepository.getTimeStampt()

    val runsSortedByDate=mainRepository.getAllRunsByDate()
}