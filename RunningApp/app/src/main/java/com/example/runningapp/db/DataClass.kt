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

data class SumByYear(
    @ColumnInfo(name = "year")
    val year: Int,
    @ColumnInfo(name = "distance")
    val distance: Int
)

data class SumByMonth(
    @ColumnInfo(name = "month")
    val month: Int,
    @ColumnInfo(name = "distance")
    val distance: Int
)

data class SumByMonthDate(
    @ColumnInfo(name = "month")
    val month: Int,
    @ColumnInfo(name = "distance")
    val distance: Int,
    @ColumnInfo(name = "olddate")
    val olddate: String,
    @ColumnInfo(name = "newdate")
    val newdate: String,
)