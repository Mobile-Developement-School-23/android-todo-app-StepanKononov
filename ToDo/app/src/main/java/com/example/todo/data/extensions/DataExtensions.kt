package com.example.todo.data.extensions

import com.example.todo.Constants
import com.example.todo.data.model.TaskPriority
import com.example.todo.data.model.TodoItem
import com.example.todo.data.model.TodoItemEntity
import com.example.todo.network.models.TodoItemResponse
import java.text.SimpleDateFormat
import java.util.*

fun List<TodoItemResponse>.asDatabaseModel(): List<TodoItemEntity> {
    return map { todoItemResponse ->
        TodoItemEntity(
            id = todoItemResponse.id,
            text = todoItemResponse.text,
            priority = when (todoItemResponse.importance) {
                TodoItemResponse.Importance.low -> TaskPriority.LOW
                TodoItemResponse.Importance.basic -> TaskPriority.MEDIUM
                TodoItemResponse.Importance.important -> TaskPriority.HIGH
            },
            deadline = todoItemResponse.deadline?.let { Date(it) },
            isComplete = todoItemResponse.done,
            creationDate = Date(todoItemResponse.createdAt),
            modifiedDate = todoItemResponse.changedAt?.let { Date(it) }
        )
    }
}

fun TodoItem.asDomainModel(): TodoItemResponse {
    val importance = when (priority) {
        com.example.todo.data.model.TaskPriority.LOW -> TodoItemResponse.Importance.low
        com.example.todo.data.model.TaskPriority.MEDIUM -> TodoItemResponse.Importance.basic
        com.example.todo.data.model.TaskPriority.HIGH -> TodoItemResponse.Importance.important
    }

    return TodoItemResponse(
        id = id,
        text = text,
        importance = importance,
        deadline = deadline?.time,
        done = isComplete,
        color = "#FFFFFF",
        createdAt = creationDate.time,
        changedAt = modifiedDate?.time ?: creationDate.time,
        lastUpdateBy = "cf1"
    )
}

fun Date.convertToStringWithFormat(): String {
    val dateFormat = SimpleDateFormat(Constants.DATA_PATTERN, Locale.getDefault())
    return dateFormat.format(this)
}
