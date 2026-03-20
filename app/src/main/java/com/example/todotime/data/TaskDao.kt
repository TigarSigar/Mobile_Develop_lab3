package com.example.todotime.data

import android.database.Cursor
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAllTasksList(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isCompleted = 1")
    fun getCompletedTasksCursor(): Cursor

    @Query(
        "SELECT id, title, '' AS description, timeSpentSeconds, tag, hasTimer AS isTvTask " +
            "FROM tasks WHERE hasTimer = 1"
    )
    fun getTvTasksCursor(): Cursor

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks") fun getAllTasksCursor(): Cursor

    @Query("UPDATE tasks SET timeSpentSeconds = timeSpentSeconds + :deltaSeconds WHERE id = :taskId")
    suspend fun incrementTimeSpent(taskId: String, deltaSeconds: Long)
}
