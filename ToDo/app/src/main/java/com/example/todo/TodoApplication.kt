package com.example.todo

import android.app.Application
import com.example.todo.data.database.AppDatabase
import com.example.todo.data.viewModels.TodoItemsRepository

class TodoApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    //Todo переделать на di
    val itemsRepository: TodoItemsRepository by lazy { TodoItemsRepository(database) }

    companion object {

        @Volatile
        private var instance: TodoApplication? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: TodoApplication().also { instance = it }
            }
    }
}