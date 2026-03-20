package com.example.todotime.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.todotime.data.AppDatabase

class TodoProvider : ContentProvider() {
    private lateinit var database: AppDatabase

    companion object {
        const val AUTHORITY = "com.example.todotime.provider"
        private const val TASKS = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "tasks", TASKS)
        }
    }

    override fun onCreate(): Boolean {
        context?.let { database = AppDatabase.getDatabase(it) }
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val cursor = if (uriMatcher.match(uri) == TASKS) {
            database.taskDao().getAllTasksCursor() // Убедись, что в DAO этот метод возвращает Cursor
        } else null

        // ВАЖНО: Эта строка говорит системе следить за этим URI
        cursor?.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/$AUTHORITY.tasks"
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}