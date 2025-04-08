package com.example.fe.ui.screens.onboard.auth
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    patternSize: Int = 3,
    onPatternComplete: (List<Int>) -> Unit
) {
    val pattern = remember { mutableStateListOf<Int>() }
    val touchPosition = remember { mutableStateOf<Offset?>(null) }
    val dotPositions = remember { mutableStateListOf<Offset>() }
    val dotRadius = 12.dp
    val strokeWidth = 5.dp

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            pattern.clear()
                            touchPosition.value = offset
                            // Check if touch is within any dot
                            val dotIndex = getDotIndexAtPosition(offset, size, patternSize, dotPositions)
                            if (dotIndex != -1 && !pattern.contains(dotIndex)) {
                                pattern.add(dotIndex)
                            }
                        },
                        onDrag = { change, _ ->
                            touchPosition.value = change.position
                            // Check if touch is within any dot
                            val dotIndex = getDotIndexAtPosition(change.position, size, patternSize, dotPositions)
                            if (dotIndex != -1 && !pattern.contains(dotIndex)) {
                                pattern.add(dotIndex)
                            }
                        },
                        onDragEnd = {
                            if (pattern.isNotEmpty()) {
                                onPatternComplete(pattern.toList())
                            }
                            touchPosition.value = null
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val cellSize = minOf(canvasWidth, canvasHeight) / patternSize
            val paddingFactor = 0.25f  // Adjust dot spacing

            // Calculate and store all dot positions if not already done
            if (dotPositions.isEmpty()) {
                for (row in 0 until patternSize) {
                    for (col in 0 until patternSize) {
                        val x = col * cellSize + cellSize / 2
                        val y = row * cellSize + cellSize / 2
                        dotPositions.add(Offset(x, y))
                    }
                }
            }

            // Draw dots
            for (i in dotPositions.indices) {
                val pos = dotPositions[i]
                val isSelected = pattern.contains(i)

                // Dot border
                drawCircle(
                    color = if (isSelected) Color(0xFF3700B3) else Color.Gray,
                    radius = (dotRadius * 1.2f).toPx(),
                    center = pos
                )

                // Dot fill
                drawCircle(
                    color = if (isSelected) Color(0xFF6200EE) else Color.White,
                    radius = dotRadius.toPx(),
                    center = pos
                )
            }

            // Draw lines between selected dots
            if (pattern.size > 1) {
                for (i in 0 until pattern.size - 1) {
                    val startDot = dotPositions[pattern[i]]
                    val endDot = dotPositions[pattern[i + 1]]

                    drawLine(
                        color = Color(0xFF6200EE),
                        start = startDot,
                        end = endDot,
                        strokeWidth = strokeWidth.toPx()
                    )
                }
            }

            // Draw line from last selected dot to current touch position
            if (pattern.isNotEmpty() && touchPosition.value != null) {
                val lastDot = dotPositions[pattern.last()]
                drawLine(
                    color = Color(0xFF6200EE),
                    start = lastDot,
                    end = touchPosition.value!!,
                    strokeWidth = strokeWidth.toPx()
                )
            }
        }
    }
}

// Helper function to determine if touch is within a dot
private fun getDotIndexAtPosition(
    touchPosition: Offset,
    canvasSize: IntSize,  // Changed from Size to IntSize
    patternSize: Int,
    dotPositions: List<Offset>
): Int {
    val threshold = (minOf(canvasSize.width, canvasSize.height) / patternSize) * 0.3f

    for (i in dotPositions.indices) {
        val dotPosition = dotPositions[i]
        val distance = sqrt(
            (touchPosition.x - dotPosition.x).pow(2) +
                    (touchPosition.y - dotPosition.y).pow(2)
        )

        if (distance < threshold) {
            return i
        }
    }

    return -1
}