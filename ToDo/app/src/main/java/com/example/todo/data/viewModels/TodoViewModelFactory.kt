package com.example.todo.data.viewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todo.data.database.TodoAppDao

class TodoViewModelFactory(
    private val todoAppDao: TodoAppDao,
    val app: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(todoAppDao, app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}