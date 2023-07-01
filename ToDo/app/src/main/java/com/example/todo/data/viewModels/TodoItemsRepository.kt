package com.example.todo.data.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.todo.Constants
import com.example.todo.data.database.AppDatabase
import com.example.todo.data.extensions.asDatabaseModel
import com.example.todo.data.extensions.asDomainModel
import com.example.todo.model.DatabaseRevision
import com.example.todo.model.TodoItem
import com.example.todo.network.*
import com.example.todo.network.models.ServerResponse
import com.example.todo.network.models.TodoItemListRequest
import com.example.todo.network.models.TodoItemRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TodoItemsRepository(
    private val database: AppDatabase,
) {
    private val status = "ok"
    val todoItemsList: LiveData<List<TodoItem>> = database.todoAppDao().getAllItems().asLiveData()
    suspend fun refreshData() {
        withContext(Dispatchers.IO) {
            val request = TodoApi.retrofitService.getServerResponse()
            val localRevision = database.todoAppDao().getRevision().value
            if (localRevision > request.revision) {
                updateServerFromDatabase(request)
            } else {
                updateDatabaseFromServer(request)
            }
            synchronizeRevisions()
        }
    }

    suspend fun updateItemToService(item: TodoItem) =
        withContext(Dispatchers.IO) {
            val request = TodoItemRequest(status, item.asDomainModel())
            val response = TodoApi.retrofitService.getServerResponse()
            TodoApi.retrofitService.updateItem(item.id, response.revision, request)
        }

    suspend fun insertItemToServer(item: TodoItem) =
        withContext(Dispatchers.IO) {
            val request = TodoItemRequest(status, item.asDomainModel())
            val response = TodoApi.retrofitService.getServerResponse()
            TodoApi.retrofitService.addItem( response.revision, request)
            synchronizeRevisions()
        }
    suspend fun insertItemToDatabase(item: TodoItem) {
        increaseRevisions()
        withContext(Dispatchers.IO) { database.todoAppDao().insertItem(item) }
    }

    suspend fun updateItemToDatabase(item: TodoItem) {
        increaseRevisions()
        withContext(Dispatchers.IO) { database.todoAppDao().updateItem(item)  }
    }
    suspend fun deleteItemFromDatabase(item: TodoItem) {
        increaseRevisions()
        withContext(Dispatchers.IO) { database.todoAppDao().deleteItem(item) }
    }


    suspend fun deleteItemFromService(item: TodoItem) =
        withContext(Dispatchers.IO) {
            val response = TodoApi.retrofitService.getServerResponse()
            TodoApi.retrofitService.deleteItem(item.id, response.revision)
        }


    private suspend fun synchronizeRevisions() {
        withContext(Dispatchers.IO) {
            val response = TodoApi.retrofitService.getServerResponse()
            database.todoAppDao().updateRevision(DatabaseRevision(Constants.REVISION_ID, response.revision))
        }
    }
    private suspend fun increaseRevisions() {
        withContext(Dispatchers.IO) {
            val currentRevision  = database.todoAppDao().getRevision().value
            database.todoAppDao().updateRevision(DatabaseRevision(Constants.REVISION_ID, currentRevision+1))
        }
    }

    private suspend fun updateServerFromDatabase(request: ServerResponse) {
        val items = todoItemsList.value
        if (!items.isNullOrEmpty()) {
            val currentRequest = TodoItemListRequest(status, items.map { it.asDomainModel() })
            TodoApi.retrofitService.patchItemList(request.revision, currentRequest)
        }
    }


    private suspend fun updateDatabaseFromServer(request: ServerResponse) {
        val itemsFromServer = request.list.asDatabaseModel()
        val itemsFromDatabase = todoItemsList.value
        if (itemsFromDatabase.isNullOrEmpty()) {
            database.todoAppDao().insertAll(itemsFromServer)
        } else {
            mergeData(itemsFromDatabase, itemsFromServer)
        }
    }

    private suspend fun TodoItemsRepository.mergeData(
        itemsFromDatabase: List<TodoItem>,
        itemsFromServer: List<TodoItem>
    ) {
        for (item in itemsFromDatabase) {
            if (itemsFromServer.find { it.id == item.id } == null) {
                deleteItemFromDatabase(item)
            }
        }
        for (item in itemsFromServer) {
            if (itemsFromDatabase.find { it.id == item.id } != null) {
                updateItemToDatabase(item)
            } else {
                insertItemToDatabase(item)
            }
        }
    }
}



