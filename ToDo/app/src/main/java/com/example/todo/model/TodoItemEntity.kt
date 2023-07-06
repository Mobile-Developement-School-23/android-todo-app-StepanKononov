package com.example.todo.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.List

@Entity(tableName = "todo_items")
data class TodoItemEntity(
    @PrimaryKey @NonNull @ColumnInfo(name = "id")
    val id: String,
    @NonNull @ColumnInfo(name = "text")
    var text: String,
    @NonNull @ColumnInfo(name = "priority")
    var priority: TaskPriority = TaskPriority.MEDIUM,
    @ColumnInfo(name = "deadline")
    var deadline: Date? = null,
    @ColumnInfo(name = "is_complete")
    var isComplete: Boolean = false,
    @NonNull @ColumnInfo(name = "creation_date")
    val creationDate: Date,
    @ColumnInfo(name = "modified_date")
    var modifiedDate: Date? = null
)

fun TodoItem.asEntity() = TodoItemEntity(
    id = id,
    text = text,
    priority = priority,
    deadline = deadline,
    isComplete = isComplete,
    creationDate = creationDate,
    modifiedDate = modifiedDate
)

fun TodoItemEntity.asExternalModel() = TodoItem(
    id = id,
    text = text,
    priority = priority,
    deadline = deadline,
    isComplete = isComplete,
    creationDate = creationDate,
    modifiedDate = modifiedDate
)

fun List<TodoItemEntity>.asExternalModels() = map(TodoItemEntity::asExternalModel)