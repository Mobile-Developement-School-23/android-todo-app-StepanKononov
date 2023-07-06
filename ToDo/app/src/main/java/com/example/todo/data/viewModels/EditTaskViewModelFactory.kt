package com.example.todo.data.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class EditTaskViewModelFactory @Inject constructor(
    modelProvider: Provider<EditTaskViewModel>
) : ViewModelProvider.Factory {

    private val providers = mapOf<Class<*>, Provider<out ViewModel>>(
        EditTaskViewModel::class.java to modelProvider
    )
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return providers[modelClass]!!.get() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


