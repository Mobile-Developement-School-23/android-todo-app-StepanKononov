package com.example.todo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todo.data.DateConverter
import com.example.todo.data.model.DatabaseRevision
import com.example.todo.data.model.TodoItemEntity
import javax.inject.Singleton

@Singleton
@Database(entities = [TodoItemEntity::class, DatabaseRevision::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoAppDao(): TodoAppDao
}