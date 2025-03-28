package com.example.fe.ui.screens.splash

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fe.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashComplete: (Boolean) -> Unit,
    isLoggedIn: Boolean
) {
    var isLoading by remember { mutableStateOf(true) }

    // 3초 후에 스플래시 화면 종료
    LaunchedEffect(key1 = true) {
        delay(3000) // 3초 대기
        isLoading = false
        onSplashComplete(isLoggedIn) // 로그인 상태를 전달하여 다음 화면 결정
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // 밝은 배경(흰색)으로 변경
        contentAlignment = Alignment.Center
    ) {
        // 로고 이미지 크기 증가
        Image(
            painter = painterResource(id = R.drawable.app_logo), // 앱 로고로 변경 필요
            contentDescription = "App Logo",
            modifier = Modifier.size(300.dp) // 300dp로 크기 증가
        )
    }
}