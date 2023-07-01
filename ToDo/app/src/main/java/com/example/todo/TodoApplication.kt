package com.example.todo

import android.app.Application
import com.example.todo.data.database.AppDatabase

class TodoApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    companion object {

        @Volatile
        private var instance: TodoApplication? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: TodoApplication().also { instance = it }
            }
    }
}