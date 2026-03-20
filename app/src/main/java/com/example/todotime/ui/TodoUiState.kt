package com.example.todotime.ui

import com.example.todotime.data.TaskEntity

data class TodoUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val isLoading: Boolean = false
)