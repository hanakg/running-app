package com.example.runningapp.di

import android.content.Context
import androidx.room.Room
import com.example.runningapp.db.RunningDatabase
import com.example.runningapp.other.Constants.RUNNING_DATABASE_NAME
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

    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app:Context
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Provides
    fun providePunDao(db: RunningDatabase)=db.getRunDao()
}