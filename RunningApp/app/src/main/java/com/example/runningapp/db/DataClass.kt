package com.example.runningapp.db

import androidx.room.ColumnInfo
import org.jetbrains.annotations.NotNull
import java.sql.Timestamp
import java.time.Year

data class CountByYear(
    @ColumnInfo(name = "year")
    val year: Int,
    @ColumnInfo(name = "count")
    val count: Int
    )