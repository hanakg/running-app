package com.example.runningapp.repositories

import com.example.runningapp.db.Run
import com.example.runningapp.db.RunDao
import javax.inject.Inject

//Adatbázis funkciók/műveletek (a RunDao műveletek meghívása)
class MainRepository @Inject constructor(
    val runDao: RunDao
){
    suspend fun insertRun(run:Run)=runDao.insertRun(run)
    suspend fun deleteRun(run:Run)=runDao.deleteRun(run)
    fun getAllRunsByDate()=runDao.getAllRunsByDate()
    fun getAllRunsByAvgSpeed()=runDao.getAllRunsByAvgSpeed()
    fun getAllRunsByDistance()=runDao.getAllRunsByDistance()
    fun getAllRunsByTimeMillisec()=runDao.getAllRunsByTimeMillisec()
    fun getAllRunsByBurnedCalories()=runDao.getAllRunsByBurnedCalories()
    fun getTotalDistance()=runDao.getTotalDistance()
    fun getTotalTimeMillisec()=runDao.getTotalTimeMillisec()
    fun getTotalBurnedCalories()=runDao.getTotalBurnedCalories()
    fun getTotalAvgSpeed()=runDao.getTotalAvgSpeed()
    fun getTotalMaxSpeed()=runDao.getTotalMaxSpeed()
    fun getTotalMinSpeed()=runDao.getTotalMinSpeed()
    fun getRunsCountByYear()=runDao.getRunsCountByYear()
    fun getDistanceSumByYear()=runDao.getDistanceSumByYear()
    fun getDistanceSumByMonth(actualMonth: Int, actualYear: Int)=runDao.getDistanceSumByActualMonth(actualMonth, actualYear)
    fun getDistanceSumLastThreeMonths(actualTimestamp: Long)=runDao.getSumDistanceLastThreeMonths(/*actualTimestamp*/)
    fun getLastRunTimeStamp()=runDao.getLastRunTimeStamp()

    fun getTimeStampt()=runDao.getTimeStampt()
}