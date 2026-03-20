package com.example.todotime.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.todotime.R
import com.example.todotime.data.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val firestore = FirebaseFirestore.getInstance()
        val userId = "V7jMNV5PsaeXhnnw3nn8ohwFKDE2" // Твой ID

        return try {
            // 1. Синхронизация с Firebase
            val tasks = db.taskDao().getAllTasksList()
            for (task in tasks) {
                firestore.collection("users").document(userId)
                    .collection("tasks").document(task.id)
                    .set(task).await()
            }

            // 2. Показываем уведомление (Требование ТЗ)
            showNotification("Синхронизация завершена", "Все задачи успешно выгружены в облако")

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(applicationContext, "SYNC_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Проверь, есть ли такой ресурс, или замени на свой
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}