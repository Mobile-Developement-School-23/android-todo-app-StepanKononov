package com.example.todo.model

import java.util.*

data class TodoItem(
    val id: String,
    var text: String,
    var priority: TaskPriority = TaskPriority.MEDIUM,
    var deadline: Date? = null,
    var isComplete: Boolean = false,
    val creationDate: Date,
    var modifiedDate: Date? = null
)