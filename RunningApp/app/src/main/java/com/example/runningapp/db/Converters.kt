package com.example.runningapp.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

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
}