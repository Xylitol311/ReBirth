package com.example.fe.ui.screens.onboard.components

import androidx.compose.ui.unit.Dp


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PinDots(
    count: Int,
    totalDots: Int = 6,
    filledColor: Color = Color(0xFF2EDCFF),
    unfilledColor: Color = Color(0xFFE0E0E0),
    dotSize: Dp = 15.dp,
    spacing: Dp = 25.dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
        repeat(totalDots) {
            Surface(

                modifier = Modifier.size(dotSize),
                shape = CircleShape,
                color = if (it < count) filledColor else unfilledColor

            ) {}
        }
    }
}