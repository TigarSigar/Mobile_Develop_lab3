package com.example.todotime.data.model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
data class ActiveTimer(
    var taskId: String = "",       // ID задачи или идентификатор категории TV (local:ID)
    var taskName: String = "",     // Название активной сущности
    var tag: String = "intel",
    var startTime: Long = 0L,      // System.currentTimeMillis() при старте
    @get:PropertyName("running")
    @set:PropertyName("running")
    var isRunning: Boolean = false,
    var sourceApp: String = ""     // "TDT" | "TV"
)
