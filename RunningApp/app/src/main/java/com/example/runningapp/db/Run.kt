package com.example.runningapp.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "running_table")
class Run(
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeed:Float=0f,
    var distance:Int=0,
    var timeMillisec:Long=0L,
    var burnedCalories: Int=0
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}