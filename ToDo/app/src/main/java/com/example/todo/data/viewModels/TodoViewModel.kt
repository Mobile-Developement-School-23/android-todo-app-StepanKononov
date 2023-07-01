package com.example.todo.data.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.todo.Constants
import com.example.todo.data.database.AppDatabase.Companion.getDatabase
import com.example.todo.data.database.TodoAppDao
import com.example.todo.model.TodoItem
import com.example.todo.workers.SynchronizeWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.TimeUnit

class TodoViewModel(
    private val todoAppDao: TodoAppDao,
    application: Application,
    private val itemsRepository: TodoItemsRepository = TodoItemsRepository(getDatabase(application)),
    private val workManager: WorkManager = WorkManager.getInstance(application)
) : AndroidViewModel(application) {

    init {
        refreshDataFromRepository()
    }
    private val workRequest =
        PeriodicWorkRequestBuilder<SynchronizeWorker>(Constants.SYNCHRONIZE_INTERVAL_HOURS, TimeUnit.HOURS)
            .build()

    private var todoItems: LiveData<List<TodoItem>> = itemsRepository.todoItemsList
    private var _eventNetworkError = MutableLiveData(false)
    private var _isNetworkErrorShown = MutableLiveData(false)

    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown

    fun retrieveItem(id: String): LiveData<TodoItem> = todoAppDao.getItemById(id).asLiveData()
    fun updateTodoItem(todoItem: TodoItem) = updateItem(todoItem)
    fun getAllItems(): Flow<List<TodoItem>> = todoItems.asFlow()
    fun addTodoItem(todoItem: TodoItem) = insertItem(todoItem)
    fun removeItem(todoItem: TodoItem) = deleteItem(todoItem)
    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }
    fun getCompleteItemsCount(): LiveData<Int> {
        return getAllItems().map { items ->
            items.count { it.isComplete }
        }.asLiveData()
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

    private fun deleteItem(item: TodoItem) {
        viewModelScope.launch {
            itemsRepository.deleteItemFromDatabase(item)
            try {
                itemsRepository.deleteItemFromService(item)
                onSuccessResponse()
            } catch (e: Exception) {
                onUnsuccessfulResponse()
                Log.v("deleteItem", e.message.toString())
            }
        }
    }

    private fun insertItem(item: TodoItem) {
        viewModelScope.launch {
            itemsRepository.insertItemToDatabase(item)
            try {
                itemsRepository.insertItemToServer(item)
                onSuccessResponse()
            } catch (e: Exception) {
                onUnsuccessfulResponse()
                Log.v("insertItem", e.message.toString())
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

