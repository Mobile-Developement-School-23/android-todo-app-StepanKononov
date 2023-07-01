package com.example.todo.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todo.TodoApplication
import com.example.todo.data.viewModels.TodoItemsRepository


private const val TAG = "SynchronizeWorker"

class SynchronizeWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            val repo = TodoItemsRepository(TodoApplication.getInstance().database)
            repo.refreshData()
            Result.success()
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error synchronize data")
            Result.failure()
        }
    }

}