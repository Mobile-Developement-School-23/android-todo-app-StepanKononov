package com.example.todo.data.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.todo.data.TodoItemsRepository
import com.example.todo.data.model.TodoItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class TaskListViewModel @Inject constructor(
    application: Application,
    private val itemsRepository: TodoItemsRepository,
    private val workManager: WorkManager,
    private val workRequest: PeriodicWorkRequest,
) : AndroidViewModel(application) {

    init {
        refreshDataFromRepository()
    }

    private var todoItems: LiveData<List<TodoItem>> = itemsRepository.todoItemsList
    private var _eventNetworkError = MutableLiveData(false)
    private var _isNetworkErrorShown = MutableLiveData(false)
    private var _isDoneTaskHide = MutableStateFlow(false)

    val isDoneTaskHide : StateFlow<Boolean>  = _isDoneTaskHide.asStateFlow()

    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown


    fun updateTodoItem(todoItem: TodoItem) = updateItem(todoItem)
    fun getAllItems(): StateFlow<List<TodoItem>> = todoItems.asFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

    fun showAllTasks() {
        _isDoneTaskHide.value = false
    }

    fun hideDoneTasks() {
        _isDoneTaskHide.value = true
    }

    fun getCompleteItemsCount(): StateFlow<Int> {
        return getAllItems().map { items ->
            items.count { it.isComplete }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0
        )
    }


    fun refreshDataFromRepository() {
        viewModelScope.launch {
            try {
                itemsRepository.refreshData()
                onSuccessResponse()

            } catch (networkError: IOException) {
                onUnsuccessfulResponse()
            }
            workManager.enqueue(workRequest)
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


    private fun onSuccessResponse() {
        _eventNetworkError.value = false
        _isNetworkErrorShown.value = false
    }

    private fun onUnsuccessfulResponse() {
        _eventNetworkError.value = true
    }
}


