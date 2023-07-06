package com.example.todo.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todo.TodoApplication
import com.example.todo.data.database.AppDatabase
import com.example.todo.data.viewModels.TodoItemsRepository
import javax.inject.Inject


private const val TAG = "SynchronizeWorker"

class SynchronizeWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {
    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var repository: TodoItemsRepository
    override suspend fun doWork(): Result {
        injectDependencies()

        return try {
            repository.refreshData()
            Result.success()
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error synchronize data")
            Result.failure()
        }
    }

    private fun injectDependencies() {
        val component = (applicationContext as TodoApplication).appComponent
        component.inject(this)
    }

}