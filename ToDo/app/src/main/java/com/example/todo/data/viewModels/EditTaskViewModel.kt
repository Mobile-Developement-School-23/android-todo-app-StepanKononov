package com.example.todo.data.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.todo.model.TaskPriority
import com.example.todo.model.TodoItem
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class EditTaskViewModel @Inject constructor(
    application: Application,
    private val itemsRepository: TodoItemsRepository
) : AndroidViewModel(application) {

    private val _currentItem = MutableLiveData<TodoItem>()

    val currentItem: LiveData<TodoItem>
        get() = _currentItem

    fun addTodoItem(todoItem: TodoItem) = insertItem(todoItem)
    fun removeItem(todoItem: TodoItem) = deleteItem(todoItem)
    fun updateTodoItem(todoItem: TodoItem) = updateItem(todoItem)
    fun retrieveItem(id: String): LiveData<TodoItem?> =
        itemsRepository.retrieveItem(id)

    fun createNewTask(id: String) {
        if ( _currentItem.value == null)
            _currentItem.value = TodoItem(id, "", TaskPriority.MEDIUM, creationDate = Calendar.getInstance().time)
    }
    fun setTask(todoItem: TodoItem){
        _currentItem.value = todoItem
    }

    fun setText(text: String){
        _currentItem.value?.text = text
    }

    fun setDeadline(date: Date?){
        _currentItem.value?.deadline = date
    }

    fun setPriority(priorityIndex: Int){
        _currentItem.value?.priority = getTaskPriority(priorityIndex)
    }

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
    private fun getTaskPriority(selectedPriorityIndex: Int): TaskPriority {
        return when (selectedPriorityIndex) {
            0 -> TaskPriority.MEDIUM
            1 -> TaskPriority.LOW
            else -> TaskPriority.HIGH
        }
    }

}




