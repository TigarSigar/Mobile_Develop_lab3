package com.example.todotime.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.todotime.data.AppDatabase
import com.example.todotime.data.TaskEntity
import com.example.todotime.notification.TaskNotifications
import java.util.UUID
import kotlinx.coroutines.runBlocking

class TaskContentProvider : ContentProvider() {
    private lateinit var database: AppDatabase

    companion object {
        const val AUTHORITY = "com.example.todotime.provider"
        val TV_ONLY_URI: Uri = Uri.parse("content://$AUTHORITY/tasks/tv_only")

        private const val TASKS_ALL = 1
        private const val TASKS_TV_ONLY = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "tasks", TASKS_ALL)
            addURI(AUTHORITY, "tasks/tv_only", TASKS_TV_ONLY)
        }
    }

    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        database = AppDatabase.getDatabase(ctx)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val cursor = when (uriMatcher.match(uri)) {
            TASKS_ALL -> database.taskDao().getAllTasksCursor()
            TASKS_TV_ONLY -> database.taskDao().getTvTasksCursor()
            else -> null
        }
        cursor?.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String {
        return "vnd.android.cursor.dir/vnd.$AUTHORITY.tasks"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (uriMatcher.match(uri) != TASKS_ALL || values == null) return null

        val title = values.getAsString("title")?.trim().orEmpty()
        if (title.isBlank()) return null

        val task = TaskEntity(
            id = values.getAsString("id")?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString(),
            title = title,
            isCompleted = values.getAsBoolean("isCompleted") ?: false,
            tag = values.getAsString("tag")?.ifBlank { "intel" } ?: "intel",
            hasTimer = values.getAsBoolean("hasTimer")
                ?: values.getAsBoolean("isTvTask")
                ?: false,
            timeSpentSeconds = values.getAsLong("timeSpentSeconds") ?: 0L,
            createdAt = values.getAsLong("createdAt") ?: System.currentTimeMillis()
        )

        runBlocking {
            database.taskDao().insert(task)
        }

        val insertedUri = ContentUris.withAppendedId(uri, task.createdAt)
        context?.contentResolver?.notifyChange(uri, null)
        context?.contentResolver?.notifyChange(Uri.parse("content://$AUTHORITY/tasks"), null)
        context?.let { ctx ->
            TaskNotifications.showExternalTaskAdded(ctx, task.title)
        }

        return insertedUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
