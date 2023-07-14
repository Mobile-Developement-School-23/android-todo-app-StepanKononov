package com.example.todo.data.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.TodoItemsRepository
import com.example.todo.data.model.TaskPriority
import com.example.todo.data.model.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


class EditTaskViewModel @Inject constructor(
    private val itemsRepository: TodoItemsRepository,
) : ViewModel() {

    private var _currentItem = createNewTask()

    private val _itemText = MutableStateFlow(_currentItem.text)
    val itemText: StateFlow<String> = _itemText.asStateFlow()

    private val _itemPriority = MutableStateFlow(_currentItem.priority)
    val itemPriority: StateFlow<TaskPriority> = _itemPriority.asStateFlow()

    private val _itemDeadline = MutableStateFlow(_currentItem.deadline)
    val itemDeadline: StateFlow<Date?> = _itemDeadline.asStateFlow()

    private var _isNewItem = MutableStateFlow(true)
    val isNewItem = _isNewItem.asStateFlow()

    fun itemNotNew() {
        _isNewItem.value = false
    }

    fun removeItem() = deleteItem(_currentItem)
    fun retrieveItem(id: String): LiveData<TodoItem?> =
        itemsRepository.retrieveItem(id)

    fun saveOrUpdateTask() {
        _currentItem.text = _itemText.value
        _currentItem.priority = _itemPriority.value
        _currentItem.deadline = _itemDeadline.value
        if (_isNewItem.value)
            insertItem(_currentItem)
        else
            updateItem(_currentItem)
    }

    private fun createNewTask(): TodoItem =
        TodoItem(UUID.randomUUID().toString(), "", TaskPriority.MEDIUM, creationDate = Calendar.getInstance().time)


    fun setTask(todoItem: TodoItem) {
        _currentItem = todoItem
        _itemText.value = todoItem.text
        _itemPriority.value = todoItem.priority
        _itemDeadline.value = todoItem.deadline
    }

    fun setText(text: String) {
        _itemText.value = text
        _currentItem.text = text
    }

    fun setDeadline(date: Date?) {
        _itemDeadline.value = date
        _currentItem.deadline = date
    }

    fun setPriority(priorityIndex: Int) {
        _itemPriority.value = getTaskPriority(priorityIndex)
        _currentItem.priority = getTaskPriority(priorityIndex)
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




