package com.example.todo.model

import android.content.res.Resources
import com.example.todo.R

enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}

fun TaskPriority.toPriorityString(resources: Resources): String {
    val priorityTypeList = resources.getStringArray(R.array.task_priority_type)

    return when (this) {
        TaskPriority.MEDIUM -> priorityTypeList[0]
        TaskPriority.LOW -> priorityTypeList[1]
        else -> priorityTypeList[2]
    }
}