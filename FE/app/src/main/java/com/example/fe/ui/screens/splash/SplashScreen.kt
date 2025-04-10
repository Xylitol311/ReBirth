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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.fe.R // R import 추가 (배경 이미지 리소스 접근용)

/**
 * SplashScreen.kt
 *
 * 1) 전체 화면을 꽉 채우는 배경 이미지 사용
 * 2) 중앙에 RE:BIRTH 로고 애니메이션(rebirth.json)을 재생
 * 3) 애니메이션 하단에 "카드를 새롭게 정의하다" 텍스트를 배치
 * 4) 애니메이션 종료 시 onSplashComplete 콜백을 통해 다음 화면 이동
 *
 * @param onSplashComplete 스플래시 종료 후 이동할 화면 결정 (로그인 여부에 따라 메인 or 온보딩)
 * @param isLoggedIn 사용자 로그인 여부
 */
/**
 * SplashScreen.kt
 *
 * 1) 전체 화면을 꽉 채우는 배경 이미지 사용
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

    // 배경 이미지가 꽉 차게 표시
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.background_image), // 배경 이미지 리소스 ID 설정
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop, // 이미지가 화면에 꽉 차도록 설정
            modifier = Modifier.fillMaxSize()
        )

        // Box의 contentAlignment를 CenterStart로 설정하여 콘텐츠를 수직 중앙, 좌측에 정렬
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            // Column을 좌측 여백(padding)과 함께 배치하여 로고와 텍스트의 왼쪽 시작점이 같도록 정렬
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp),  // 좌측 여백 지정 (필요에 따라 값 조정)
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                // 로고 영역: 화면 가로의 50%만 차지하도록 수정
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)  // 화면 가로 50%만 차지하도록 설정
                        .aspectRatio(2f)     // 가로:세로 비율 2:1로 설정 (가로가 세로의 2배)
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
                        // 실제 환경에서는 Lottie 애니메이션을 표시
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                        )
                    }
                }

                // 텍스트 배치 (위로 약간 올려서 로고와 겹치듯이)
                // 로고와 정확히 왼쪽 정렬되도록 start 패딩을 제거
                Text(
                    text = "카드를 새롭게 정의하다",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier
                        .offset(y = (-20).dp)
                        .padding(start = 2.dp)// 위로 올리는 값 조정
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