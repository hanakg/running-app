package com.example.runningapp.db

import androidx.lifecycle.LiveData
import androidx.room.*

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


    //----------------------------------------------------------------------

}