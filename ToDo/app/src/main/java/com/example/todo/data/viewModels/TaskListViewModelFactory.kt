package com.example.todo.data.viewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todo.data.database.TodoAppDao

class TaskListViewModelFactory(
    private val todoAppDao: TodoAppDao,
    private val app: Application,
    private val repository: TodoItemsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel( app, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

