package com.example.todo.data.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.TodoItemsRepository
import com.example.todo.data.model.TaskPriority
import com.example.todo.data.model.TodoItem
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class SnackbarState(
    val deleteText: String,
    val cancelText: String,
    val duration: Long,
    var remainingTime: Long,
)

class EditTaskViewModel @Inject constructor(
    private val itemsRepository: TodoItemsRepository,
) : ViewModel() {
    private var _snackbarState: SnackbarState? = null
    private val _currentItem = MutableLiveData<TodoItem>()
    private var _isNewItem = true
    val snackbarState get() = _snackbarState
    val currentItem get() = _currentItem
    val isNewItem get() = _isNewItem

    fun showDeleteButton() = !isNewItem && snackbarState == null

    fun itemNotNew() {
        _isNewItem = false
    }

    fun removeItem(todoItem: TodoItem) = deleteItem(todoItem)
    fun retrieveItem(id: String): LiveData<TodoItem?> =
        itemsRepository.retrieveItem(id)

    fun saveOrUpdateTask(item: TodoItem) {
        if (_isNewItem)
            insertItem(item)
        else
            updateItem(item)
    }

    fun createNewTask(id: String) {
        if (_currentItem.value == null)
            _currentItem.value = TodoItem(id, "", TaskPriority.MEDIUM, creationDate = Calendar.getInstance().time)
    }

    fun setTask(todoItem: TodoItem) {
        _currentItem.value = todoItem
    }


    fun setText(text: String) {
        _currentItem.value?.text = text
    }

    fun setDeadline(date: Date?) {
        _currentItem.value?.deadline = date
    }

    fun setPriority(priorityIndex: Int) {
        _currentItem.value?.priority = getTaskPriority(priorityIndex)
    }


    fun setSnackbarState(state: SnackbarState) {
        _snackbarState = state
    }

    fun setRemainingTimeState(time: Long) {
        _snackbarState?.remainingTime = time
    }

    fun clearSnackbarState() {
        _snackbarState = null
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




