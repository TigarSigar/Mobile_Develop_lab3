package com.example.todotime.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.todotime.data.TaskEntity
import com.example.todotime.data.model.ActiveTimer
import com.example.todotime.ui.components.PixelCheckbox
import com.example.todotime.ui.components.PixelIcons
import com.example.todotime.ui.theme.AmberPrimary
import com.example.todotime.ui.theme.CreamBackground
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

private val fallbackTags = listOf(
    TdtTagConfig(id = "intel", display_name = "Интеллект"),
    TdtTagConfig(id = "strength", display_name = "Сила"),
    TdtTagConfig(id = "craft", display_name = "Ремесло")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel, onAccountClick: () -> Unit) {
    val tasks by viewModel.allTasks.collectAsState(initial = emptyList())
    val sharedTimer by viewModel.sharedTimer.collectAsState()
    val remoteTags by viewModel.tagOptions.collectAsState()
    val conflictState = viewModel.conflictTimerState.value
    val tagOptions = if (remoteTags.isEmpty()) fallbackTags else remoteTags
    val user = FirebaseAuth.getInstance().currentUser

    var showDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }

    conflictState?.let { (remoteTimer, newTaskToStart) ->
        TimerConflictDialog(
            conflictingApp = remoteTimer.sourceApp,
            onForceStop = { viewModel.resolveTimerConflict(remoteTimer, newTaskToStart) },
            onDismiss = { viewModel.dismissConflict() }
        )
    }

    if (showDialog) {
        TaskEditDialog(
            task = taskToEdit,
            tagOptions = tagOptions,
            onDismiss = {
                showDialog = false
                taskToEdit = null
            },
            onSave = { title, tagId, hasTimer ->
                if (taskToEdit == null) {
                    viewModel.addTask(title, tagId, hasTimer)
                } else {
                    viewModel.updateTaskDetails(taskToEdit!!, title, tagId, hasTimer)
                }
                showDialog = false
                taskToEdit = null
            },
            onDelete = {
                taskToEdit?.let(viewModel::deleteTask)
                showDialog = false
                taskToEdit = null
            }
        )
    }

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ToDoTime",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2A2726),
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = onAccountClick,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (user?.photoUrl != null) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(7.dp))
                                    .border(2.dp, AmberPrimary, RoundedCornerShape(7.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .border(2.dp, AmberPrimary, RoundedCornerShape(7.dp))
                                    .background(Color(0xFFFFF9E8), RoundedCornerShape(7.dp))
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    taskToEdit = null
                    showDialog = true
                },
                containerColor = AmberPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 8.dp)
                    .size(58.dp)
            ) {
                Text("+", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        val activeTasks = tasks.filter { !it.isCompleted }
        val completedTasks = tasks.filter { it.isCompleted }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
            ) {
                items(activeTasks, key = { it.id }) { task ->
                    val isActive = viewModel.activeTaskId.value == task.id
                    TaskItemCard(
                        task = task,
                        isActive = isActive,
                        sharedTimer = sharedTimer,
                        onToggle = { viewModel.toggleTask(task) },
                        onTimerClick = { if (isActive) viewModel.stopTimer() else viewModel.startTimer(task) },
                        onEdit = {
                            taskToEdit = task
                            showDialog = true
                        }
                    )
                }

                items(completedTasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        isActive = false,
                        sharedTimer = null,
                        onToggle = { viewModel.toggleTask(task) },
                        onTimerClick = {},
                        onEdit = {
                            taskToEdit = task
                            showDialog = true
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskItemCard(
    task: TaskEntity,
    isActive: Boolean,
    sharedTimer: ActiveTimer?,
    onToggle: () -> Unit,
    onTimerClick: () -> Unit,
    onEdit: () -> Unit
) {
    val timerStart = sharedTimer
        ?.takeIf { it.isRunning && it.taskId == task.id }
        ?.startTime
        ?: 0L
    val isRunning = isActive && timerStart > 0L

    var displayTime by remember(task.id, task.timeSpentSeconds, isRunning, timerStart) {
        mutableLongStateOf(task.timeSpentSeconds)
    }

    LaunchedEffect(isRunning, timerStart, task.timeSpentSeconds) {
        if (!isRunning) {
            displayTime = task.timeSpentSeconds
            return@LaunchedEffect
        }
        while (true) {
            val elapsed = ((System.currentTimeMillis() - timerStart) / 1000).coerceAtLeast(0)
            displayTime = task.timeSpentSeconds + elapsed
            delay(1000)
        }
    }

    val tagIcon = when (task.tag.lowercase()) {
        "intel", "интеллект" -> PixelIcons.Brain
        "strength", "сила" -> PixelIcons.Sword
        "craft", "ремесло" -> PixelIcons.Hammer
        else -> PixelIcons.Hammer
    }
    val tagLabel = when (task.tag.lowercase()) {
        "intel", "интеллект" -> "Intel"
        "strength", "сила" -> "Strength"
        "craft", "ремесло" -> "Craft"
        else -> task.tag.replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase() else ch.toString()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (task.isCompleted) 0.6f else 1f)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(18.dp), ambientColor = Color(0x22000000))
            .background(Color(0xFFFFF8E9), RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFF2E8D3), RoundedCornerShape(18.dp))
            .combinedClickable(onClick = {}, onLongClick = onEdit)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            PixelCheckbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.padding(top = 1.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color(0xFF2D2A28),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFFF4D4), RoundedCornerShape(5.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = tagIcon,
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tagLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA67A00)
                        )
                    }

                    if (task.hasTimer) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(AmberPrimary, RoundedCornerShape(5.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text("TV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            if (task.hasTimer && !task.isCompleted) {
                Column(
                    modifier = Modifier.width(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .background(AmberPrimary, CircleShape)
                            .clickable(onClick = onTimerClick),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRunning) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(14.dp)
                                        .background(Color.White)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(14.dp)
                                        .background(Color.White)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatCardTimer(displayTime),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC66A00)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerConflictDialog(
    conflictingApp: String,
    onForceStop: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CreamBackground,
        title = { Text("Таймер запущен в другом приложении", fontWeight = FontWeight.Bold) },
        text = { Text("Таймер в данный момент работает в другом приложении ($conflictingApp).") },
        confirmButton = {
            Button(
                onClick = onForceStop,
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
            ) {
                Text("Завершить активный таймер", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color.Gray)
            }
        }
    )
}

internal fun formatElapsedTime(totalSeconds: Long): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val seconds = safeSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun formatCardTimer(totalSeconds: Long): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    if (safeSeconds == 0L) return "0 sec"
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return if (minutes == 0L) {
        "$seconds sec"
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
