package com.example.todotime.worker

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todotime.data.AppDatabase
import com.example.todotime.data.TaskEntity
import com.example.todotime.notification.TaskNotifications
import com.example.todotime.sync.JsonPlaceholderClient
import com.example.todotime.sync.JsonPlaceholderCreateTodoRequest
import com.example.todotime.sync.SyncPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyncWorker(
    context: android.content.Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.taskDao()
        val api = JsonPlaceholderClient.api

        return try {
            val remoteTodos = api.getTodos(limit = 40)

            remoteTodos.forEach { todo ->
                val mapped = TaskEntity(
                    id = "jp-${todo.id}",
                    title = todo.title,
                    isCompleted = todo.completed,
                    tag = mapTodoToTag(todo.id),
                    hasTimer = false,
                    timeSpentSeconds = 0L,
                    createdAt = System.currentTimeMillis() - todo.id * 1_000L
                )
                dao.insert(mapped)
            }

            // Демонстрация двустороннего sync: отправляем часть локальных задач на mock API.
            val localTasks = dao.getAllTasksList().take(5)
            localTasks.forEach { task ->
                api.createTodo(
                    JsonPlaceholderCreateTodoRequest(
                        userId = 1,
                        title = task.title,
                        completed = task.isCompleted
                    )
                )
            }

            val now = System.currentTimeMillis()
            SyncPreferences.saveLastSyncTime(applicationContext, now)
            TaskNotifications.showSyncCompleted(
                context = applicationContext,
                contentText = "Последняя синхронизация: ${formatTimestamp(now)}"
            )

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun mapTodoToTag(id: Int): String {
        return when (id % 3) {
            0 -> "intel"
            1 -> "strength"
            else -> "craft"
        }
    }

    private fun formatTimestamp(timestampMs: Long): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return formatter.format(Date(timestampMs))
    }
}
