package com.example.runningapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runningapp.db.RunningDatabase
import com.example.runningapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningapp.other.Constants.KEY_MOVEMENT
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.example.runningapp.other.Constants.RUNNING_DATABASE_NAME
import com.example.runningapp.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.DefineComponent
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.managers.ActivityComponentManager
import dagger.hilt.android.internal.managers.ApplicationComponentManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    //Hogyan tudja a Dagger-Hilt létrehozni az adatbázist
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app:Context
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    //Hogyan tudja a Dagger-Hilt létrehozni a RunDao objectet(adatbázis műveletek)
    @Provides
    fun providePunDao(db: RunningDatabase)=db.getRunDao()

    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context)=
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    //Dagger-Hilt - név tárolására létrehozás
    @Provides
    fun provideName(sharedPref:SharedPreferences)=sharedPref.getString(KEY_NAME, "")?:""

    //Dagger-Hilt - súly tárolására létrehozás
    @Provides
    fun provideWeight(sharedPref:SharedPreferences)=sharedPref.getFloat(KEY_WEIGHT, 80f)

    //Dagger-Hilt - mozgás típusának tárolására létrehozás
    @Provides
    fun provideMovement(sharedPref:SharedPreferences)=sharedPref.getString(KEY_MOVEMENT, "Futás")

    //Dagger-Hilt - első futása-e az alkalmazásnak tárolására létrehozás
    @Provides
    fun provideFirstTimeToggle(sharedPref:SharedPreferences)=sharedPref.getBoolean(
        KEY_FIRST_TIME_TOGGLE, true)
}