package com.example.todo.data.database

import androidx.room.*
import com.example.todo.model.DatabaseRevision
import com.example.todo.model.TodoItem
import kotlinx.coroutines.flow.Flow


@Dao
interface TodoAppDao {
    @Query("select * from todo_items")
    fun getAllItems(): Flow<List<TodoItem>>

    @Query("SELECT * from todo_items WHERE id = :id")
    fun getItemById(id: String): Flow<TodoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: TodoItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<TodoItem>)

    @Update
    suspend fun updateItem(item: TodoItem)

    @Query("SELECT * from revision WHERE id = :id")
    fun getRevision(id: Int = 1): DatabaseRevision

    @Update
    fun updateRevision(revision: DatabaseRevision)

    @Delete
    suspend fun deleteItem(item: TodoItem)
}