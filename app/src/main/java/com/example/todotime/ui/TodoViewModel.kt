package com.example.todotime.ui

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todotime.data.TaskEntity
import com.example.todotime.data.TodoRepository
import com.example.todotime.data.UserTimeStats
import com.example.todotime.data.model.ActiveTimer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class TdtTagConfig(
    val id: String = "",
    val display_name: String = "",
    val suffix: String = ""
)

data class TdtSettings(
    val tags: List<TdtTagConfig> = emptyList()
)

data class GameConfig(
    val tdt_settings: TdtSettings? = null
)

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var activeTaskId = mutableStateOf<String?>(null)

    private val _sharedTimer = MutableStateFlow<ActiveTimer?>(null)
    val sharedTimer: StateFlow<ActiveTimer?> = _sharedTimer.asStateFlow()

    val userTimeStats: StateFlow<UserTimeStats> = repository.observeUserTimeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserTimeStats())

    private val _tagOptions = MutableStateFlow<List<TdtTagConfig>>(emptyList())
    val tagOptions: StateFlow<List<TdtTagConfig>> = _tagOptions.asStateFlow()

    // Pair<ConflictingTimer, TaskUserWantsToStart>
    val conflictTimerState = mutableStateOf<Pair<ActiveTimer, TaskEntity>?>(null)
    private val stopMutex = Mutex()
    private val auth = FirebaseAuth.getInstance()
    private var sharedTimerJob: Job? = null
    private val authStateListener = FirebaseAuth.AuthStateListener {
        subscribeSharedTimer()
    }

    init {
        fetchRemoteConfig()
        subscribeSharedTimer()
        auth.addAuthStateListener(authStateListener)
    }

    private fun subscribeSharedTimer() {
        sharedTimerJob?.cancel()
        sharedTimerJob = viewModelScope.launch {
            repository.getSharedTimerFlow().collectLatest { timer ->
                _sharedTimer.value = timer
                if (timer != null && timer.isRunning && timer.taskId.isNotBlank()) {
                    activeTaskId.value = timer.taskId
                } else {
                    activeTaskId.value = null
                }
            }
        }
    }

    private fun fetchRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val json = remoteConfig.getString("buildings_json")
                try {
                    val config = Gson().fromJson(json, GameConfig::class.java)
                    val fetchedTags = config?.tdt_settings?.tags
                    if (!fetchedTags.isNullOrEmpty()) {
                        _tagOptions.value = fetchedTags
                    }
                } catch (e: Exception) {
                    Log.e("REMOTE_CONFIG", "Tag parsing error: ${e.message}")
                }
            }
        }
    }

    fun addTask(title: String, tagId: String, hasTimer: Boolean) {
        viewModelScope.launch {
            repository.insert(TaskEntity(title = title, tag = tagId, hasTimer = hasTimer))
        }
    }

    fun updateTaskDetails(task: TaskEntity, newTitle: String, newTagId: String, newHasTimer: Boolean) {
        viewModelScope.launch {
            repository.update(task.copy(title = newTitle, tag = newTagId, hasTimer = newHasTimer))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            val runningThisTask = _sharedTimer.value?.let { it.isRunning && it.taskId == task.id } == true
            if (runningThisTask) {
                val stopped = stopActiveTimer(expectedTaskId = task.id)
                val stillRunning = _sharedTimer.value?.let { it.isRunning && it.taskId == task.id } == true
                if (!stopped && stillRunning) return@launch
            }
            repository.delete(task)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            val runningThisTask = _sharedTimer.value?.let { it.isRunning && it.taskId == task.id } == true
            if (runningThisTask) {
                val stopped = stopActiveTimer(expectedTaskId = task.id)
                val stillRunning = _sharedTimer.value?.let { it.isRunning && it.taskId == task.id } == true
                if (!stopped && stillRunning) return@launch
            }
            repository.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun startTimer(task: TaskEntity) {
        viewModelScope.launch {
            val currentRemoteTimer = _sharedTimer.value
            if (currentRemoteTimer != null && currentRemoteTimer.isRunning) {
                if (currentRemoteTimer.taskId == task.id) {
                    stopActiveTimer(expectedTaskId = task.id)
                    return@launch
                }
                if (currentRemoteTimer.sourceApp != "TDT") {
                    conflictTimerState.value = currentRemoteTimer to task
                    return@launch
                }
                if (currentRemoteTimer.taskId.isNotBlank()) {
                    val switched = stopActiveTimer(expectedTaskId = currentRemoteTimer.taskId)
                    val stillRunning = _sharedTimer.value?.isRunning == true
                    if (!switched && stillRunning) return@launch
                }
            }

            repository.updateSharedTimer(
                ActiveTimer(
                    taskId = task.id,
                    taskName = task.title,
                    tag = task.tag,
                    startTime = System.currentTimeMillis(),
                    isRunning = true,
                    sourceApp = "TDT"
                )
            )
        }
    }

    fun resolveTimerConflict(remoteTimer: ActiveTimer, newTaskToStart: TaskEntity) {
        viewModelScope.launch {
            val currentlyRunning = _sharedTimer.value?.isRunning == true
            if (currentlyRunning) {
                val stopped = stopActiveTimer()
                if (!stopped && (_sharedTimer.value?.isRunning == true)) return@launch
            }
            conflictTimerState.value = null
            startTimer(newTaskToStart)
        }
    }

    fun dismissConflict() {
        conflictTimerState.value = null
    }

    fun stopTimerForTask(taskId: String) {
        viewModelScope.launch {
            stopActiveTimer(expectedTaskId = taskId)
        }
    }

    fun stopTimer() {
        viewModelScope.launch {
            stopActiveTimer()
        }
    }

    private suspend fun stopActiveTimer(expectedTaskId: String? = null): Boolean {
        return stopMutex.withLock {
            val timer = _sharedTimer.value ?: return@withLock false
            if (!timer.isRunning) return@withLock false
            if (expectedTaskId != null && timer.taskId != expectedTaskId) return@withLock false

            val taskId = timer.taskId.takeIf { it.isNotBlank() } ?: run {
                repository.clearSharedTimer()
                return@withLock true
            }

            val elapsedSeconds = if (timer.startTime > 0L) {
                ((System.currentTimeMillis() - timer.startTime) / 1000).coerceAtLeast(0)
            } else {
                0L
            }

            val saved = repository.applyTimerDelta(taskId, elapsedSeconds)
            if (!saved) return@withLock false

            repository.clearSharedTimer()
            return@withLock true
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedTimerJob?.cancel()
        auth.removeAuthStateListener(authStateListener)
    }
}
