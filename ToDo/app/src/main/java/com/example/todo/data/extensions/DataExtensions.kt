package com.example.todo.data.extensions

import com.example.todo.model.TaskPriority
import com.example.todo.model.TodoItem
import com.example.todo.model.TodoItemEntity
import com.example.todo.network.models.TodoItemResponse
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
        TaskPriority.LOW -> TodoItemResponse.Importance.low
        TaskPriority.MEDIUM -> TodoItemResponse.Importance.basic
        TaskPriority.HIGH -> TodoItemResponse.Importance.important
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

