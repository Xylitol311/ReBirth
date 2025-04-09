package com.example.fe.ui.screens.onboard.auth
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.unit.IntOffset
import kotlin.math.hypot

@Composable
fun PatternLockView(
    modifier: Modifier = Modifier,
    patternSize: Int = 3,
    onPatternComplete: (List<Int>) -> Unit
) {
    val rows = patternSize
    val columns = patternSize
    val pointRadius = 13.dp
    val spacing = 110.dp
    val pointColor = Color(0xFF00D9FF)
    val touchSlop = 35f

    val density = LocalDensity.current
    val pointRadiusPx = with(density) { pointRadius.toPx() }
    val spacingPx = with(density) { spacing.toPx() }

    var currentPattern by remember { mutableStateOf(listOf<Int>()) }
    var isDragging by remember { mutableStateOf(false) }
    var currentDragPoint by remember { mutableStateOf<Offset?>(null) }

    val points = remember(spacingPx) {
        List(rows * columns) { idx ->
            val row = idx / columns
            val col = idx % columns
            val centerX = col * spacingPx + spacingPx / 2
            val centerY = row * spacingPx + spacingPx / 2
            Offset(centerX, centerY)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(spacing * columns)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            currentDragPoint = offset
                            currentPattern = listOf()

                            points.forEachIndexed { index, point ->
                                if (hypot(offset.x - point.x, offset.y - point.y) <= pointRadiusPx + touchSlop) {
                                    currentPattern = listOf(index)
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentDragPoint = change.position

                            points.forEachIndexed { index, point ->
                                if (!currentPattern.contains(index) &&
                                    hypot(change.position.x - point.x, change.position.y - point.y) <= pointRadiusPx + touchSlop
                                ) {
                                    currentPattern = currentPattern + index
                                }
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            currentDragPoint = null
                            onPatternComplete(currentPattern)
                        },
                        onDragCancel = {
                            isDragging = false
                            currentDragPoint = null
                            currentPattern = listOf()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                if (currentPattern.isNotEmpty()) {
                    for (i in 0 until currentPattern.size - 1) {
                        val start = points[currentPattern[i]]
                        val end = points[currentPattern[i + 1]]

                        drawLine(
                            color = pointColor,
                            start = start,
                            end = end,
                            strokeWidth = 10f,
                            cap = StrokeCap.Round
                        )
                    }

                    if (isDragging && currentDragPoint != null) {
                        val last = points[currentPattern.last()]
                        drawLine(
                            color = pointColor,
                            start = last,
                            end = currentDragPoint!!,
                            strokeWidth = 10f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // 점 그리기
            points.forEachIndexed { index, offset ->
                val isSelected = currentPattern.contains(index)
                Box(
                    modifier = Modifier
                        .size(pointRadius * 2)
                        .offset {
                            IntOffset(
                                (offset.x - pointRadiusPx).toInt(),
                                (offset.y - pointRadiusPx).toInt()
                            )
                        }
                        .background(
                            color = if (isSelected) pointColor else Color.LightGray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .animateContentSize()
                )
            }
        }
    }
}

// Helper function to determine if touch is within a dot
//private fun getDotIndexAtPosition(
//    touchPosition: Offset,
//    canvasSize: IntSize,  // Changed from Size to IntSize
//    patternSize: Int,
//    dotPositions: List<Offset>
//): Int {
//    val threshold = (minOf(canvasSize.width, canvasSize.height) / patternSize) * 0.3f
//
//    for (i in dotPositions.indices) {
//        val dotPosition = dotPositions[i]
//        val distance = sqrt(
//            (touchPosition.x - dotPosition.x).pow(2) +
//                    (touchPosition.y - dotPosition.y).pow(2)
//        )
//
//        if (distance < threshold) {
//            return i
//        }
//    }
//
//    return -1
//}