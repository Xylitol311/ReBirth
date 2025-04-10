package com.example.fe.ui.screens.splash

// Lottie Compose 관련 임포트 (의존성: lottie-compose:6.0.0)
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.fe.ui.components.backgrounds.StarryBackground

/**
 * SplashScreen.kt
 *
 * 1) StarryBackground를 사용해 어두운 밤하늘 배경을 표시
 * 2) 중앙에 RE:BIRTH 로고 애니메이션(rebirth.json)을 재생
 * 3) 애니메이션 하단에 "카드를 새롭게 정의하다" 텍스트를 배치
 * 4) 애니메이션 종료 시 onSplashComplete 콜백을 통해 다음 화면 이동
 *
 * @param onSplashComplete 스플래시 종료 후 이동할 화면 결정 (로그인 여부에 따라 메인 or 온보딩)
 * @param isLoggedIn 사용자 로그인 여부
 */
@Composable
fun SplashScreen(
    onSplashComplete: (Boolean) -> Unit,
    isLoggedIn: Boolean
) {
    // Preview 환경인 경우 에러 회피를 위해 별도 분기
    val isInPreview = LocalInspectionMode.current

    // rebirth.json Lottie 파일을 불러와서 Composition 생성
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("rebirth.json")
    )

    // 애니메이션 진행 상태 (iterations = 1로 설정하여 1회만 재생)
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    // 애니메이션이 끝났는지 검사 (진행도가 1f 이상)
    if (progress >= 1f) {
        onSplashComplete(isLoggedIn)
    }

    // StarryBackground로 배경 설정
    StarryBackground {
        // Box의 contentAlignment를 CenterStart로 설정하여 콘텐츠를 수직 중앙, 좌측에 정렬합니다.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            // Column을 좌측 여백(padding)과 함께 배치하여 로고와 텍스트의 왼쪽 시작점이 같도록 정렬합니다.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),  // 좌측 여백 지정 (필요에 따라 값 조정)
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                // 로고 영역: 가로 300.dp, 높이 150.dp의 박스에 Lottie 애니메이션 배치
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .width(300.dp)
                        .height(150.dp)
                        .clip(RectangleShape) // 클리핑을 통해 영역 외는 잘림
                ) {
                    if (isInPreview) {
                        // Preview 환경에서는 간단한 텍스트 플레이스홀더 표시
                        Text(
                            text = "RE:BIRTH 로고",
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        // 실제 환경에서는 Lottie 애니메이션을 fillMaxWidth()로 표시하며, 중앙 정렬
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        )
                    }
                }
                // 실제 여백을 최대한 줄임 (값은 상황에 따라 조정 가능)
                Text(
                    text = "카드를 새롭게 정의하다",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier
                        // y를 음수로 주어 위로 이동
                        .offset(y = -28.dp)
                        .padding(start = 18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    // Preview에서는 간단하게 onSplashComplete 콜백을 비워두고 isLoggedIn 값을 false로 설정하여 미리보기 표시
    SplashScreen(onSplashComplete = {}, isLoggedIn = false)
}