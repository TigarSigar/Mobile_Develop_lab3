package com.example.todotime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.todotime.data.AppDatabase
import com.example.todotime.data.TodoRepository
import com.example.todotime.notification.TaskNotifications
import com.example.todotime.worker.SyncWorkScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TodoApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    val repository by lazy {
        TodoRepository(
            taskDao = database.taskDao(),
            firestore = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance()
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        SyncWorkScheduler.schedulePeriodicSync(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val syncChannel = NotificationChannel(
            TaskNotifications.SYNC_CHANNEL_ID,
            "Sync Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о синхронизации задач"
        }

        val externalTaskChannel = NotificationChannel(
            TaskNotifications.EXTERNAL_TASK_CHANNEL_ID,
            "External Tasks",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Уведомления о задачах, добавленных извне"
        }

        manager.createNotificationChannel(syncChannel)
        manager.createNotificationChannel(externalTaskChannel)
    }
}
