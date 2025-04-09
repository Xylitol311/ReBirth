package com.example.fe.ui.screens.splash

// Lottie Compose 관련 임포트 (의존성: lottie-compose:6.0.0)
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * SplashScreen.kt
 *
 * 스플래시 스크린 컴포저블 함수입니다.
 * assets 폴더에 위치한 "rebirth.json" Lottie 애니메이션을 한 번 재생하고,
 * 애니메이션이 종료되는 시점에 로그인 여부(isLoggedIn)를 확인하여
 * onSplashComplete 콜백을 호출합니다.
 *
 * @param onSplashComplete 스플래시 종료 후 이동할 화면을 결정하는 콜백
 *                         - isLoggedIn가 true이면 메인 화면으로,
 *                         - false이면 튜토리얼(회원가입) 화면으로 이동
 * @param isLoggedIn 사용자 로그인 여부
 */
@Composable
fun SplashScreen(
    onSplashComplete: (Boolean) -> Unit,
    isLoggedIn: Boolean
) {
    // assets 폴더의 rebirth.json 파일로부터 Lottie 애니메이션 컴포지션을 로드합니다.
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("rebirth.json")
    )
    // 애니메이션 재생 상태 관리: iterations = 1로 지정하여 단 한 번 재생.
    // progress 값이 1f에 도달하면 애니메이션이 끝났다는 의미입니다.
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    // 애니메이션이 완료되면 onSplashComplete 콜백을 호출하여 다음 화면을 결정합니다.
    if (progress == 1f) {
        onSplashComplete(isLoggedIn)
    }

    // 스플래시 화면 UI 구성: 흰색 배경의 중앙에 Lottie 애니메이션을 300dp 크기로 표시합니다.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(300.dp)
        )
    }
}
