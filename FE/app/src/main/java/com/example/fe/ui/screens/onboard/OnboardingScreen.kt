package com.example.fe.ui.screens.onboard

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(navController: NavController, viewModel: OnboardingViewModel) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    // 페이지 정보
    val pages = listOf(
        OnboardingPageData(
            imageRes = R.drawable.earth,
            title = "당신의 소비 유형을 분석하고",
            description = "나만의 목표를 가져보세요"
        ),
        OnboardingPageData(
            imageRes = R.drawable.constellation,
            title = "당신에게 딱맞게 챙겨주는\n첫 신용카드 taptap O",
            description = "특별한 별자리로\n내 카드를 표현하세요"
        ),
        OnboardingPageData(
            imageRes = R.drawable.godcard,
            title = "새우지코 또한 매력 있다면",
            description = "별자리와 함께 하여\n최고 혜택 카드로 지금 경험 하세요"
        )
    )

    // 현재 페이지에 따른 수평 오프셋 계산
    val horizontalOffset = (pagerState.currentPage + pagerState.currentPageOffset) * 300f

    // 페이지 오프셋 비율 (애니메이션 용)
    val currentPageOffset = pagerState.currentPageOffset

    Box(modifier = Modifier.fillMaxSize()) {
        // 별이 빛나는 배경
        StarryBackground(
            horizontalOffset = horizontalOffset
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 임시 보안 설정 버튼 추가
                Button(
                    onClick = {
                        navController.navigate("pin_setup") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4169E1)
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Text("보안설정 테스트", fontSize = 16.sp)
                }

                // 콘텐츠 영역 (이미지와 텍스트) - 화면 중앙으로 배치
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f) // 화면 85% 영역 사용 (흰색 부분 제거)
                        .align(Alignment.Center), // 중앙 정렬로 변경
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center // 중앙 배치
                ) {
                    // 페이저 (이미지)
                    HorizontalPager(
                        count = pages.size,
                        state = pagerState,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        Image(
                            painter = painterResource(id = pages[page].imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(220.dp), // 이미지 크기도 키움
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 텍스트 (현재 페이지)
                    Text(
                        text = pages[pagerState.currentPage].title,
                        fontSize = 24.sp, // 18sp에서 24sp로 키움
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )

                    Text(
                        text = pages[pagerState.currentPage].description,
                        fontSize = 20.sp, // 16sp에서 20sp로 키움
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 페이지 인디케이터
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        activeColor = Color.White,
                        inactiveColor = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.padding(16.dp),
                        indicatorWidth = 12.dp, // 인디케이터 크기도 키움
                        indicatorHeight = 12.dp // 인디케이터 크기도 키움
                    )
                }

                // 버튼
                Button(
                    onClick = {
                        scope.launch {
                            if (pagerState.currentPage < pages.lastIndex) {
                                pagerState.animateScrollToPage(
                                    page = pagerState.currentPage + 1,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = androidx.compose.animation.core.LinearEasing
                                    )
                                )
                            } else {
                                navController.navigate("auth") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .height(60.dp), // 버튼 높이 증가
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEE82EE) // 분홍색
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.lastIndex) "시작하기" else "다음",
                        fontSize = 20.sp, // 16sp에서 20sp로 키움
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String
)