package com.example.todo.data.viewModels

data class SnackbarState(
    val deleteText: String,
    val cancelText: String,
    val duration: Long,
    var remainingTime: Long,
)