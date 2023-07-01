package com.example.todo.network.models

import com.google.gson.annotations.SerializedName

data class TodoItemRequest(
    @SerializedName("status")
    val status: String,
    @SerializedName("element")
    val element: TodoItemResponse
)