package com.example.todotime.ui

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todotime.data.TaskEntity
import com.example.todotime.data.TodoRepository
import com.example.todotime.data.UserTimeStats
import com.example.todotime.data.model.ActiveTimer
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    private var localStartTimeMs: Long? = null

    private val _sharedTimer = MutableStateFlow<ActiveTimer?>(null)
    val sharedTimer: StateFlow<ActiveTimer?> = _sharedTimer.asStateFlow()

    val userTimeStats: StateFlow<UserTimeStats> = repository.observeUserTimeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserTimeStats())

    private val _tagOptions = MutableStateFlow<List<TdtTagConfig>>(emptyList())
    val tagOptions: StateFlow<List<TdtTagConfig>> = _tagOptions.asStateFlow()

    // Pair<ConflictingTimer, TaskUserWantsToStart>
    val conflictTimerState = mutableStateOf<Pair<ActiveTimer, TaskEntity>?>(null)

    init {
        fetchRemoteConfig()

        viewModelScope.launch {
            repository.getSharedTimerFlow().collectLatest { timer ->
                _sharedTimer.value = timer
                if (timer != null && timer.isRunning && timer.sourceApp == "TDT") {
                    activeTaskId.value = timer.taskId
                    localStartTimeMs = timer.startTime
                } else {
                    activeTaskId.value = null
                    localStartTimeMs = null
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
            if (activeTaskId.value == task.id) stopTimer()
            repository.delete(task)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            if (activeTaskId.value == task.id) stopTimer()
            repository.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun startTimer(task: TaskEntity) {
        val currentRemoteTimer = _sharedTimer.value

        if (
            currentRemoteTimer != null &&
            currentRemoteTimer.isRunning &&
            currentRemoteTimer.sourceApp != "TDT"
        ) {
            conflictTimerState.value = currentRemoteTimer to task
            return
        }

        viewModelScope.launch {
            if (
                currentRemoteTimer != null &&
                currentRemoteTimer.isRunning &&
                currentRemoteTimer.sourceApp == "TDT" &&
                currentRemoteTimer.taskId.isNotBlank() &&
                currentRemoteTimer.taskId != task.id
            ) {
                val delta = ((System.currentTimeMillis() - currentRemoteTimer.startTime) / 1000)
                    .coerceAtLeast(0)
                repository.applyTimerDelta(currentRemoteTimer.taskId, delta)
            }

            activeTaskId.value = task.id
            val now = System.currentTimeMillis()
            localStartTimeMs = now
            repository.updateSharedTimer(
                ActiveTimer(
                    taskId = task.id,
                    taskName = task.title,
                    tag = task.tag,
                    startTime = now,
                    isRunning = true,
                    sourceApp = "TDT"
                )
            )
        }
    }

    fun resolveTimerConflict(remoteTimer: ActiveTimer, newTaskToStart: TaskEntity) {
        viewModelScope.launch {
            val elapsedSeconds = if (remoteTimer.startTime > 0) {
                (System.currentTimeMillis() - remoteTimer.startTime) / 1000
            } else 0

            if (elapsedSeconds > 0) {
                repository.applyTimerDelta(remoteTimer.taskId, elapsedSeconds)
            }

            repository.clearSharedTimer()
            conflictTimerState.value = null
            startTimer(newTaskToStart)
        }
    }

    fun dismissConflict() {
        conflictTimerState.value = null
    }

    fun stopTimer() {
        val currentTimer = _sharedTimer.value
        val startTime = when {
            currentTimer != null && currentTimer.isRunning && currentTimer.sourceApp == "TDT" -> currentTimer.startTime
            localStartTimeMs != null -> localStartTimeMs ?: 0L
            else -> 0L
        }
        if (startTime <= 0L) return
        val taskId = currentTimer?.taskId?.takeIf { it.isNotBlank() } ?: activeTaskId.value.orEmpty()

        val elapsedSeconds = ((System.currentTimeMillis() - startTime) / 1000).coerceAtLeast(0)

        viewModelScope.launch {
            repository.applyTimerDelta(taskId, elapsedSeconds)
            repository.clearSharedTimer()
        }
        activeTaskId.value = null
        localStartTimeMs = null
    }
}
