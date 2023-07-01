package com.example.todo.network.models

import com.google.gson.annotations.SerializedName

data class TodoItemListRequest(
    @SerializedName("status")
    val status: String,
    @SerializedName("list")
    val list: List<TodoItemResponse>
)