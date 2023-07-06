package com.example.todo.di

import android.content.Context
import androidx.room.Room
import com.example.todo.data.database.AppDatabase
import dagger.Module
import dagger.Provides

@Module
class DatabaseModule {
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        )
            .createFromAsset("database/todo_app")
            .build()
    }
}