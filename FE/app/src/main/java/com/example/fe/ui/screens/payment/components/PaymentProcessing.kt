package com.example.fe.ui.screens.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.ui.components.backgrounds.StarryBackground
import kotlinx.coroutines.delay

@Composable
fun PaymentProcessing(
    paymentState: String,
    onMinimumTimeElapsed: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showProcessing by remember { mutableStateOf(true) }
    var elapsedMinimumTime by remember { mutableStateOf(false) }

    // 최소 3초 타이머
    LaunchedEffect(Unit) {
        delay(3000) // 3초 대기
        elapsedMinimumTime = true
        // 이미 결제 상태가 변경되었다면 결과 화면으로 전환
        if (paymentState != "Processing") {
            showProcessing = false
            onMinimumTimeElapsed()
        }
    }

    // 결제 상태 변경 감지
    LaunchedEffect(paymentState) {
        if (paymentState != "Processing" && elapsedMinimumTime) {
            // 최소 시간이 지났고 상태가 변경되었으면 결과 화면으로 전환
            showProcessing = false
            onMinimumTimeElapsed()
        }
    }

    // 배경과 별자리 애니메이션
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 별이 빛나는 배경
        StarryBackground(
            modifier = Modifier.fillMaxSize(),
            starCount = 100,
            scrollOffset = 0f
        ) {}

        // 결제 진행 중 UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "결제 진행 중",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "잠시만 기다려 주세요...",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

        }
    }
}