package com.example.todo.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "todo_items")
data class TodoItem(
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
