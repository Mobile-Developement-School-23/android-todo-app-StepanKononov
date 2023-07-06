package com.example.todo.data.database

import androidx.room.*
import com.example.todo.model.DatabaseRevision
import com.example.todo.model.TodoItemEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface TodoAppDao {
    @Query("select * from todo_items")
    fun getAllItems(): Flow<List<TodoItemEntity>>

    @Query("SELECT * from todo_items WHERE id = :id")
    fun getItemById(id: String): Flow<TodoItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: TodoItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<TodoItemEntity>)

    @Update
    suspend fun updateItem(item: TodoItemEntity)

    @Delete
    suspend fun deleteItem(item: TodoItemEntity)

    @Query("SELECT * from revision WHERE id = :id")
    fun getRevision(id: Int = 1): DatabaseRevision

    @Update
    fun updateRevision(revision: DatabaseRevision)
}