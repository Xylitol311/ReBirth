package com.example.fe.ui.screens.onboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@Composable
fun NumberPad(
    numbers: List<Int>,
    input: String,
    onInputChange: (String) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1~9 무작위 배치
    val numbersOneToNine = numbers.filter { it in 1..9 }
    val zero = numbers.firstOrNull { it == 0 } ?: 0

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF2EDCFF))

    ) {
        // 1~9 (3x3 그리드)
        numbersOneToNine.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { num ->
                    NumberKey(
                        text = num.toString(),
                        onClick = {
                            if (input.length < 6) {
                                onInputChange(input + num)
                                if (input.length + 1 == 6) onComplete()
                            }
                        }
                    )
                }
            }
        }

        // 마지막 줄: 빈칸, 0, 백스페이스
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(64.dp))

            // 0 버튼
            NumberKey(
                text = zero.toString(),
                onClick = {
                    if (input.length < 6) {
                        onInputChange(input + "0")
                        if (input.length + 1 == 6) onComplete()
                    }
                }
            )

            // 백스페이스
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (input.isNotEmpty()) {
                            onInputChange(input.dropLast(1))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    fontSize = 32.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}