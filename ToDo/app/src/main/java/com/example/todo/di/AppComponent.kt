package com.example.todo.di

import android.app.Application
import android.content.Context
import com.example.todo.data.database.AppDatabase
import com.example.todo.network.TodoApi
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import javax.inject.Scope
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
    fun fragmentComponent(): FragmentComponent.Factory
    fun workerComponent(): WorkerComponent.Factory
}

@Scope
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
annotation class FragmentScope
@Scope
@MustBeDocumented
@Retention(value = AnnotationRetention.RUNTIME)
annotation class ActivityScope
@Module(subcomponents = [FragmentComponent::class, WorkerComponent::class])
class AppSubcomponents

