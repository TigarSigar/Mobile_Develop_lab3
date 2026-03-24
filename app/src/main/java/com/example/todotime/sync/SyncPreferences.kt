package com.example.todotime.sync

import android.content.Context

object SyncPreferences {
    private const val PREFS_NAME = "todo_sync_prefs"
    private const val KEY_LAST_SYNC_TIME_MS = "last_sync_time_ms"

    fun saveLastSyncTime(context: Context, timestampMs: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_SYNC_TIME_MS, timestampMs)
            .apply()
    }

    fun getLastSyncTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_SYNC_TIME_MS, 0L)
    }
}
