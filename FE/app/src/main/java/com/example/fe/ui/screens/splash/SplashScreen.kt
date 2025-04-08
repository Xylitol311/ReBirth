package com.example.fe.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun SplashScreen(
    onSplashComplete: (Boolean) -> Unit,
    isLoggedIn: Boolean
) {
    var isLoading by remember { mutableStateOf(true) }
    
    // Lottie 애니메이션 로드 - assets 폴더에서 로드
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("logoanime.json"))
    
    // 애니메이션 상태 - 빠르게 재생하기 위해 speed 파라미터 추가
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = 1,
        speed = 1.5f, // 애니메이션 재생 속도 1.5배 증가
        restartOnPlay = false
    )

    // 1초 후에 스플래시 화면 종료 (3초에서 1초로 변경)
    LaunchedEffect(key1 = true) {
        delay(1000) // 1초 대기로 단축
        isLoading = false
        onSplashComplete(isLoggedIn) // 로그인 상태를 전달하여 다음 화면 결정
    }

    // progress가 1.0f에 도달하면(애니메이션이 완료되면) 바로 다음 화면으로 이동
    LaunchedEffect(progress) {
        if (progress >= 0.99f && isLoading) {
            isLoading = false
            onSplashComplete(isLoggedIn)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // 밝은 배경(흰색)으로 변경
        contentAlignment = Alignment.Center
    ) {
        // Lottie 애니메이션 표시
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(350.dp)
        )
    }
}