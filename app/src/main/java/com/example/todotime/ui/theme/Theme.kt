package com.example.todotime.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ToDoColorScheme = lightColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.White,
    background = CreamBackground,
    surface = CreamBackground,
    onBackground = TextTitle,
    onSurface = TextTitle
)

@Composable
fun ToDoTimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ToDoColorScheme,
        typography = Typography,
        content = content
    )
}
