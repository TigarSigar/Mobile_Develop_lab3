package com.example.todotime.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object PixelIcons {
    // Basic pixelated sword representation
    val Sword: ImageVector
        get() = ImageVector.Builder(
            name = "Sword",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(14f, 2f)
                lineTo(22f, 2f)
                lineTo(22f, 10f)
                lineTo(14f, 18f)
                lineTo(10f, 18f)
                lineTo(8f, 20f)
                lineTo(4f, 20f)
                lineTo(4f, 16f)
                lineTo(6f, 14f)
                lineTo(6f, 10f)
                close()
            }
        }.build()

    val Brain: ImageVector
        get() = ImageVector.Builder(
            name = "Brain",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 2f)
                lineTo(18f, 2f)
                lineTo(22f, 8f)
                lineTo(22f, 16f)
                lineTo(18f, 22f)
                lineTo(6f, 22f)
                lineTo(2f, 16f)
                lineTo(2f, 8f)
                lineTo(6f, 2f)
                close()
            }
        }.build()

    val Hammer: ImageVector
        get() = ImageVector.Builder(
            name = "Hammer",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(2f, 6f)
                lineTo(10f, 6f)
                lineTo(10f, 12f)
                lineTo(2f, 12f)
                close()
                moveTo(4f, 12f)
                lineTo(8f, 12f)
                lineTo(8f, 22f)
                lineTo(4f, 22f)
                close()
            }
        }.build()
}
