package com.example.todo.network.models

import com.google.gson.annotations.SerializedName

data class ServerResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("list")
    val list: List<TodoItemResponse>,
    @SerializedName("revision")
    val revision: Int
)