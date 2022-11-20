package com.example.runningapp.db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import java.sql.Timestamp
import java.util.*

@Dao
interface RunDao {

    // Rendezés valamai szerint - lekérdezés
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsByDate(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeed DESC")
    fun getAllRunsByAvgSpeed(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distance DESC")
    fun getAllRunsByDistance(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeMillisec DESC")
    fun getAllRunsByTimeMillisec(): LiveData<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY burnedCalories DESC")
    fun getAllRunsByBurnedCalories(): LiveData<List<Run>>
    //----------------------------------------------------------------------

    // Statisztikák
    @Query("SELECT SUM(timeMillisec) FROM running_table")
    fun getTotalTimeMillisec():LiveData<Long>

    @Query("SELECT SUM(distance) FROM running_table")
    fun getTotalDistance():LiveData<Int>

    @Query("SELECT SUM(burnedCalories) FROM running_table")
    fun getTotalBurnedCalories():LiveData<Int>

    @Query("SELECT AVG(avgSpeed) FROM running_table")
    fun getTotalAvgSpeed():LiveData<Float>

    @Query("SELECT MAX(maxSpeed) FROM running_table")
    fun getTotalMaxSpeed():LiveData<Float>

    @Query("SELECT MIN(minSpeed) FROM running_table")
    fun getTotalMinSpeed():LiveData<Float>

    @Query("SELECT strftime('%Y',datetime(timestamp/1000, 'unixepoch', 'localtime')) as year, COUNT(*) as count FROM running_table GROUP BY strftime('%Y',datetime(timestamp/1000, 'unixepoch', 'localtime'))")
    fun getRunsCountByYear():LiveData<List<CountByYear>>

    @Query("SELECT strftime('%Y',datetime(timestamp/1000, 'unixepoch', 'localtime')) as year, SUM(distance) as distance FROM running_table GROUP BY strftime('%Y',datetime(timestamp/1000, 'unixepoch', 'localtime'))")
    fun getDistanceSumByYear():LiveData<List<SumByYear>>

    //strftime('%m',datetime(timestamp/1000, 'unixepoch', 'localtime'))
    @Query("SELECT CAST(strftime('%m',datetime(timestamp/1000, 'unixepoch', 'localtime')) AS int) as month, SUM(distance) as distance FROM running_table WHERE month == :actualMonth AND CAST(strftime('%Y',datetime(timestamp/1000, 'unixepoch', 'localtime')) AS int) == :actualYear")
    fun getDistanceSumByActualMonth(actualMonth: Int, actualYear: Int):LiveData<SumByMonth>

    //CAST(strftime('%Y - %m - %d',datetime(timestamp/1000, 'unixepoch', 'localtime'))as string)
    //WHERE Date('2022-10-02')  BETWEEN DATE('2022-11-15', '-3month') AND DATE('2022-11-15') GROUP BY month

    //DATE(strftime('%Y-%m-%d',datetime(timestamp/1000, 'unixepoch', 'localtime')), '-3 month')

    //@Query("SELECT DATE('now', '-3 month') as month FROM running_table")
    // @Query("SELECT CAST(strftime('%m',datetime(timestamp/1000, 'unixepoch', 'localtime')) AS int) as month, SUM(distance) as distance FROM running_table WHERE (month == :actualMonth-2 OR month == :actualMonth-1 OR month == :actualMonth) AND CAST(strftime('%Y',datetime(timestamp/1000, 'unixepoch', 'localtime')) AS int) == :actualYear GROUP BY month")
    @Query("SELECT CAST(strftime('%m',datetime(timestamp/1000, 'unixepoch', 'localtime')) AS int) as month, SUM(distance) as distance, DATE('now', '-3 month') as olddate, DATE('now') as newdate   FROM running_table WHERE strftime('%Y-%m-%d',datetime(timestamp/1000, 'unixepoch', 'localtime')) BETWEEN DATE('now', '-3 month') AND DATE('now') GROUP BY month")
    fun getSumDistanceLastThreeMonths(/*actualTimestamp: Long*/):LiveData<List<SumByMonthDate>>

    @Query("SELECT timestamp as value FROM running_table ORDER BY timestamp DESC LIMIT 1")
    fun getLastRunTimeStamp():LiveData<Timestamp>

    @Query("SELECT timestamp as value FROM running_table ")
    fun getTimeStampt():LiveData<List<Timestamp>>
    //----------------------------------------------------------------------

}