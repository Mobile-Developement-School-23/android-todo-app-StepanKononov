package com.example.todo.data.viewModels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todo.data.viewModels.TaskListViewModel
import javax.inject.Inject
import javax.inject.Provider

class TaskListViewModelFactory @Inject constructor(
    modelProvider: Provider<TaskListViewModel>
) : ViewModelProvider.Factory {

    private val providers = mapOf<Class<*>, Provider<out ViewModel>>(
        TaskListViewModel::class.java to modelProvider
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return providers[modelClass]!!.get() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

