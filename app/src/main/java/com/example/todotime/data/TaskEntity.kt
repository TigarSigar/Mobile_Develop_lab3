package com.example.todotime.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val isCompleted: Boolean = false,
    val tag: String = "intel",
    val hasTimer: Boolean = false,
    val timeSpentSeconds: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
