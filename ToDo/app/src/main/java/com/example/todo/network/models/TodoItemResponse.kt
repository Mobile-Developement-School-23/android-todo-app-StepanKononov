package com.example.todo.network.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TodoItemResponse(
    @SerializedName("id")
    var id: String,

    @SerializedName("text")
    var text: String,

    @SerializedName("importance")
    var importance: Importance,

    @SerializedName("deadline")
    var deadline: Long? = null,

    @SerializedName("done")
    var done: Boolean,

    @SerializedName("color")
    var color: String?,

    @SerializedName("created_at")
    var createdAt: Long,

    @SerializedName("changed_at")
    var changedAt: Long? = null,

    @SerializedName("last_updated_by")
    var lastUpdateBy: String?

) : Serializable {
    enum class Importance {
        low, basic, important
    }
}