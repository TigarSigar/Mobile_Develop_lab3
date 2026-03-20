package com.example.todotime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.todotime.data.TaskEntity
import com.example.todotime.ui.theme.AmberPrimary
import com.example.todotime.ui.theme.CreamBackground

@Composable
fun TaskEditDialog(
    task: TaskEntity?,
    tagOptions: List<TdtTagConfig>,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var hasTimer by remember { mutableStateOf(task?.hasTimer ?: false) }
    var selectedTagId by remember { mutableStateOf(task?.tag ?: "") }

    LaunchedEffect(tagOptions) {
        if (selectedTagId.isBlank() && tagOptions.isNotEmpty()) {
            selectedTagId = tagOptions.first().id
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CreamBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (task == null) "Новая задача" else "Редактировать задачу",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Категория", fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tagOptions, key = { it.id }) { tag ->
                        TagOptionCard(
                            label = tag.display_name,
                            selected = selectedTagId == tag.id,
                            onClick = { selectedTagId = tag.id }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasTimer,
                        onCheckedChange = { hasTimer = it },
                        colors = CheckboxDefaults.colors(checkedColor = AmberPrimary)
                    )
                    Text(
                        text = "Включить таймер (TV)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmberPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (task != null) {
                        TextButton(onClick = onDelete) { Text("Удалить", color = Color.Red) }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    TextButton(onClick = onDismiss) { Text("Отмена", color = Color.Gray) }
                    Button(
                        onClick = {
                            if (title.isNotBlank() && selectedTagId.isNotBlank()) {
                                onSave(title, selectedTagId, hasTimer)
                            }
                        },
                        enabled = title.isNotBlank() && selectedTagId.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                    ) {
                        Text("Сохранить", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun TagOptionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(6.dp))
            .background(CreamBackground, RoundedCornerShape(6.dp))
            .then(
                if (selected) Modifier.border(2.dp, AmberPrimary, RoundedCornerShape(6.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (selected) AmberPrimary else Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}
