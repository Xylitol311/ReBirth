package com.example.fe.ui.screens.onboard.components

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
    // 1-9 숫자만 추출하여 새로운 리스트 생성 (0은 별도 배치)
    val numbersOneToNine = numbers.filter { it in 1..9 }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // 1-9 숫자들을 3x3 그리드로 배치
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

        // 마지막 줄 (0과 백스페이스 버튼) - Row의 기준선 정렬 사용
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically // 세로 중앙 정렬로 변경
        ) {
            // 왼쪽 공간
            Spacer(modifier = Modifier.width(80.dp))
            
            // 가운데 0 버튼
            NumberKey(
                text = "0",
                onClick = {
                    if (input.length < 6) {
                        onInputChange(input + "0")
                        if (input.length + 1 == 6) onComplete()
                    }
                }
            )
            
            // 오른쪽 백스페이스 버튼 - 정렬을 위한 Box로 감싸기
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (input.isNotEmpty()) {
                                onInputChange(input.dropLast(1))
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // 화살표 텍스트를 약간 위로 이동시키기 위한 추가 패딩
                    Text(
                        text = "←",
                        fontSize = 44.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp) // 더 큰 하단 패딩 적용
                    )
                }
            }
        }
    }
}