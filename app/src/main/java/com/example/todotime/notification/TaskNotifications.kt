package com.example.todotime.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.todotime.R
import kotlin.random.Random

object TaskNotifications {
    const val SYNC_CHANNEL_ID = "SYNC_CHANNEL"
    const val EXTERNAL_TASK_CHANNEL_ID = "EXTERNAL_TASK_CHANNEL"

    fun showSyncCompleted(context: Context, contentText: String) {
        show(
            context = context,
            channelId = SYNC_CHANNEL_ID,
            title = "Синхронизация завершена",
            message = contentText,
            notificationId = 101
        )
    }

    fun showExternalTaskAdded(context: Context, taskTitle: String) {
        show(
            context = context,
            channelId = EXTERNAL_TASK_CHANNEL_ID,
            title = "Новая внешняя задача",
            message = "Добавлена задача: $taskTitle",
            notificationId = 200 + Random.nextInt(1, 1000)
        )
    }

    private fun show(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(notificationId, notification)
    }
}
