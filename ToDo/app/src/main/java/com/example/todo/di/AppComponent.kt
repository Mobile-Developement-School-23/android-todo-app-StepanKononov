package com.example.todo.di

import android.app.Application
import android.content.Context
import com.example.todo.data.database.AppDatabase
import com.example.todo.data.viewModels.TaskListViewModelFactory
import com.example.todo.network.TodoApi
import com.example.todo.ui.EditTaskFragment
import com.example.todo.ui.TaskListFragment
import com.example.todo.workers.SynchronizeWorker
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, WorkManagerModule::class, PeriodicWorkRequestModule::class])
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context, @BindsInstance app: Application): AppComponent
    }

    fun taskListViewModelFactory(): TaskListViewModelFactory
    fun dataBase(): AppDatabase

    fun todoApi(): TodoApi
    fun inject(fragment: TaskListFragment)
    fun inject(fragment: EditTaskFragment)
    fun inject(work: SynchronizeWorker)
}