package com.example.todotime

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.todotime.data.AppDatabase
import com.example.todotime.data.TodoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TodoApplication : Application() {

    // Инициализация базы Room
    val database by lazy { AppDatabase.getDatabase(this) }

    // Теперь передаем все 3 параметра: DAO, Firestore и Auth
    val repository by lazy {
        TodoRepository(
            taskDao = database.taskDao(),
            firestore = FirebaseFirestore.getInstance(),
            auth = FirebaseAuth.getInstance()
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sync Notifications"
            val descriptionText = "Уведомления о синхронизации задач"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("SYNC_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}