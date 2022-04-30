package com.example.runningapp.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream
import java.sql.Date
import java.sql.Timestamp
import java.util.*

class Converters {
    //ByteArray->Bitmap
    @TypeConverter
    fun ByteArraytoBitmap(bytes: ByteArray):Bitmap{
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    //Bitmap->ByteArray->Save in DataBase
    @TypeConverter
    fun ByteArrayfromBitmap(bmp:Bitmap):ByteArray{
        val outputStream=ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream) //Picture->ByteArray
        return outputStream.toByteArray()
    }

    //Timestamp(Long)->Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Timestamp? {
        Log.d("converter","toDate")
        return value?.let { Timestamp(it) }
    }

    //Date->TimeStamp
    @TypeConverter
    fun dateToTimestamp(date: Timestamp?): Long? {
        Log.d("converter","toLong")
        return date?.time?.toLong()
    }

}