package com.example.todo.data.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.todo.model.TodoItem
import kotlinx.coroutines.launch

class EditTaskViewModel(
    application: Application,
    private val itemsRepository: TodoItemsRepository
) : AndroidViewModel(application) {



    fun addTodoItem(todoItem: TodoItem) = insertItem(todoItem)
    fun removeItem(todoItem: TodoItem) = deleteItem(todoItem)

    fun updateTodoItem(todoItem: TodoItem) = updateItem(todoItem)
    fun retrieveItem(id: String): LiveData<TodoItem?> =
        itemsRepository.retrieveItem(id)

    private fun deleteItem(item: TodoItem) {
        viewModelScope.launch {
            itemsRepository.deleteItemFromDatabase(item)
            try {
                itemsRepository.deleteItemFromService(item)
            } catch (e: Exception) {
                Log.v("deleteItem", e.message.toString())
            }
        }
    }

    private fun insertItem(item: TodoItem) {
        viewModelScope.launch {
            itemsRepository.insertItemToDatabase(item)
            try {
                itemsRepository.insertItemToServer(item)
            } catch (e: Exception) {
                Log.v("insertItem", e.message.toString())
            }
        }
    }

    private fun updateItem(item: TodoItem) {
        viewModelScope.launch {
            itemsRepository.updateItemToDatabase(item)
            try {
                itemsRepository.updateItemToService(item)
            } catch (e: Exception) {
                Log.v("updateItem", e.message.toString())
            }
        }
    }
}




