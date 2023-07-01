package com.example.todo.model

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revision")
data class DatabaseRevision(
    @PrimaryKey @NonNull @ColumnInfo(name = "id")
    val id: Int,
    @NonNull @ColumnInfo(name = "value")
    var value: Int,
)
