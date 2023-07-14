package com.example.todo.di.components

import android.app.Application
import android.content.Context
import com.example.todo.data.database.AppDatabase
import com.example.todo.di.module.DatabaseModule
import com.example.todo.di.module.PeriodicWorkRequestModule
import com.example.todo.di.module.WorkManagerModule
import com.example.todo.network.TodoApi
import com.example.todo.ui.NotificationUtils
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        DatabaseModule::class,
        WorkManagerModule::class,
        PeriodicWorkRequestModule::class,
        AppSubcomponents::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context, @BindsInstance app: Application): AppComponent
    }

    fun dataBase(): AppDatabase
    fun todoApi(): TodoApi
    fun inject(notificationUtils: NotificationUtils)
    fun fragmentComponent(): FragmentComponent.Factory
    fun workerComponent(): WorkerComponent.Factory
}

