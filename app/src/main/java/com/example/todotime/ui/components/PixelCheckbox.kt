package com.example.todotime.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PixelCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedColor: Color = Color(0xFFFE9601), // Amber
    uncheckedColor: Color = Color(0xFFFE9601).copy(alpha = 0.4f),
    checkmarkColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // No ripple for pure pixel feel
                onClick = { onCheckedChange(!checked) }
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val canvasSize = size.width
            val pixel = canvasSize / 10f // grid of 10x10

            // Draw border
            drawRect(
                color = if (checked) checkedColor else uncheckedColor,
                topLeft = Offset.Zero,
                size = Size(canvasSize, canvasSize),
                style = Stroke(width = pixel) // 1 pixel border
            )
            
            // Draw filled inner square if checked
            if (checked) {
                drawRect(
                    color = checkedColor,
                    topLeft = Offset.Zero,
                    size = size
                )

                // Draw checkmark in white pixels
                // V shape composed of small rectangles
                drawRect(color = checkmarkColor, topLeft = Offset(pixel * 2, pixel * 5), size = Size(pixel * 2, pixel * 2))
                drawRect(color = checkmarkColor, topLeft = Offset(pixel * 4, pixel * 7), size = Size(pixel * 2, pixel * 2))
                drawRect(color = checkmarkColor, topLeft = Offset(pixel * 6, pixel * 5), size = Size(pixel * 2, pixel * 2))
                drawRect(color = checkmarkColor, topLeft = Offset(pixel * 6, pixel * 3), size = Size(pixel * 2, pixel * 2))
            }
        }
    }
}
