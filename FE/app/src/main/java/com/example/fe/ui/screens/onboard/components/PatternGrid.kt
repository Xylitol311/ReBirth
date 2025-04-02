package com.example.fe.ui.screens.onboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot

@Composable
fun PatternGrid(
    onPatternComplete: (List<Int>) -> Unit,
    showConfirmButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    val rows = 3
    val columns = 3
    val pointRadius = 16.dp
    val pointCount = rows * columns
    val touchSlop = 35f
    val gridSize = 400.dp
    val spacing = gridSize / 1.5f

    val pointColor = Color(0xFF4169E1)
    val density = LocalDensity.current
    val pointRadiusPx = with(density) { pointRadius.toPx() }

    var currentPattern by remember { mutableStateOf(listOf<Int>()) }
    var isDragging by remember { mutableStateOf(false) }
    var currentDragPoint by remember { mutableStateOf<Offset?>(null) }

    val points = remember {
        List(pointCount) { idx ->
            val row = idx / columns
            val col = idx % columns
            val centerX = col * (spacing.value) + (spacing.value / 2)
            val centerY = row * (spacing.value) + (spacing.value / 2)
            Offset(centerX, centerY)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(150.dp))

            Box(
                modifier = Modifier
                    .size(gridSize)
                    .background(Color.Transparent)
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
                                        hypot(change.position.x - point.x, change.position.y - point.y) <= pointRadiusPx + touchSlop) {
                                        currentPattern = currentPattern + index
                                    }
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                currentDragPoint = null
                                if (!showConfirmButton) {
                                    onPatternComplete(currentPattern)
                                }
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
                                strokeWidth = 12f,
                                cap = StrokeCap.Round
                            )
                        }

                        if (isDragging && currentPattern.isNotEmpty() && currentDragPoint != null) {
                            val start = points[currentPattern.last()]
                            drawLine(
                                color = pointColor,
                                start = start,
                                end = currentDragPoint!!,
                                strokeWidth = 12f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                points.forEachIndexed { index, offset ->
                    val isSelected = currentPattern.contains(index)

                    Box(
                        modifier = Modifier
                            .size(pointRadius * 2)
                            .offset(
                                x = with(density) { offset.x.toDp() } - pointRadius,
                                y = with(density) { offset.y.toDp() } - pointRadius
                            )
                            .background(
                                color = if (isSelected) pointColor else pointColor.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                    )
                }
            }

            if (showConfirmButton && currentPattern.isNotEmpty()) {
                Spacer(modifier = Modifier.height(0.dp))
                Button(
                    onClick = { onPatternComplete(currentPattern) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF191E3F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "확인",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}