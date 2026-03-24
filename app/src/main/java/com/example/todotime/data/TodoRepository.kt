package com.example.todotime.data

import android.util.Log
import com.example.todotime.data.model.ActiveTimer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class UserTimeStats(
    val accumulatedTime: Long = 0L,
    val globalTime: Long = 0L
)

class TodoRepository(
    private val taskDao: TaskDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val database = FirebaseDatabase
        .getInstance("https://timevillage-42-default-rtdb.europe-west1.firebasedatabase.app")
        .reference

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    private fun getTimerRef(): DatabaseReference {
        val uid = auth.currentUser?.uid ?: "anonymous"
        return database.child("users").child(uid).child("active_timer")
    }

    private fun isFirestoreTaskId(taskId: String): Boolean {
        return taskId.isNotBlank() && !taskId.startsWith("local:")
    }

    fun getSharedTimerFlow(): Flow<ActiveTimer?> = callbackFlow {
        val ref = getTimerRef()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val timer = snapshot.getValue(ActiveTimer::class.java)
                trySend(timer)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RTDB", "Read error: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun updateSharedTimer(timer: ActiveTimer) {
        try {
            getTimerRef().setValue(timer).await()
        } catch (e: Exception) {
            Log.e("RTDB", "Write error: ${e.message}")
        }
    }

    suspend fun insert(task: TaskEntity) {
        taskDao.insert(task)
        syncTaskToFirebase(task)
    }

    suspend fun update(task: TaskEntity) {
        taskDao.update(task)
        syncTaskToFirebase(task)
    }

    suspend fun delete(task: TaskEntity) {
        taskDao.delete(task)
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("tasks")
            .document(task.id)
            .delete()
    }

    suspend fun incrementTaskTimeSpent(taskId: String, deltaSeconds: Long) {
        if (deltaSeconds <= 0L || !isFirestoreTaskId(taskId)) return
        val userId = auth.currentUser?.uid ?: return
        try {
            taskDao.incrementTimeSpent(taskId, deltaSeconds)
            firestore.collection("users")
                .document(userId)
                .collection("tasks")
                .document(taskId)
                .update("timeSpentSeconds", FieldValue.increment(deltaSeconds))
                .await()
        } catch (e: Exception) {
            Log.e("SYNC", "incrementTaskTimeSpent error: ${e.message}")
        }
    }

    fun observeUserTimeStats(): Flow<UserTimeStats> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(UserTimeStats())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SYNC", "User stats listener error: ${error.message}")
                    return@addSnapshotListener
                }
                val accumulated = snapshot?.getLong("accumulatedTime") ?: 0L
                val global = snapshot?.getLong("globalTime") ?: 0L
                trySend(UserTimeStats(accumulated, global))
            }

        awaitClose { registration.remove() }
    }

    suspend fun applyTimerDelta(taskId: String, deltaSeconds: Long): Boolean {
        if (deltaSeconds <= 0L) return true

        val userId = auth.currentUser?.uid ?: return false
        val userDocRef = firestore.collection("users").document(userId)
        val taskDocRef = userDocRef.collection("tasks").document(taskId)
        val shouldUpdateTask = isFirestoreTaskId(taskId)

        return try {
            firestore.runBatch { batch ->
                batch.set(
                    userDocRef,
                    mapOf(
                        "accumulatedTime" to FieldValue.increment(deltaSeconds),
                        "globalTime" to FieldValue.increment(deltaSeconds)
                    ),
                    SetOptions.merge()
                )
                if (shouldUpdateTask) {
                    batch.set(
                        taskDocRef,
                        mapOf("timeSpentSeconds" to FieldValue.increment(deltaSeconds)),
                        SetOptions.merge()
                    )
                }
            }.await()

            if (shouldUpdateTask) {
                taskDao.incrementTimeSpent(taskId, deltaSeconds)
            }
            true
        } catch (e: Exception) {
            Log.e("SYNC", "applyTimerDelta error: ${e.message}")
            false
        }
    }

    suspend fun clearSharedTimer() {
        try {
            getTimerRef().setValue(
                ActiveTimer(
                    isRunning = false,
                    startTime = 0L,
                    sourceApp = "",
                    taskId = "",
                    taskName = "",
                    tag = ""
                )
            ).await()
        } catch (e: Exception) {
            Log.e("RTDB", "clearSharedTimer error: ${e.message}")
        }
    }

    private fun syncTaskToFirebase(task: TaskEntity) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(userId)
            .collection("tasks")
            .document(task.id)
            .set(
                mapOf(
                    "id" to task.id,
                    "title" to task.title,
                    "description" to "",
                    "timeSpentSeconds" to task.timeSpentSeconds,
                    "tags" to listOf(task.tag),
                    "isTvTask" to task.hasTimer,
                    "isCompleted" to task.isCompleted,
                    "createdAt" to task.createdAt
                )
            )
            .addOnFailureListener { e ->
                Log.e("SYNC", "Firestore task sync error: ${e.message}")
            }
    }
}
