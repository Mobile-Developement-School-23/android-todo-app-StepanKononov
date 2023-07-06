package com.example.todo.di

import android.app.Application
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todo.Constants
import com.example.todo.workers.SynchronizeWorker
import dagger.Module
import dagger.Provides
import java.util.concurrent.TimeUnit

@Module
class WorkManagerModule {
    @Provides
    fun provideWorkManager(application: Application): WorkManager {
        return WorkManager.getInstance(application)
    }
}

@Module
class PeriodicWorkRequestModule {
    @Provides
    fun provideWorkRequest(): PeriodicWorkRequest {
        return PeriodicWorkRequestBuilder<SynchronizeWorker>(
            Constants.SYNCHRONIZE_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .build()
    }
}