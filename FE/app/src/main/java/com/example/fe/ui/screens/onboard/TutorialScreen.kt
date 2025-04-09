package com.example.fe.ui.screens.onboard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

/**
 * TutorialScreen.kt (Compose 버전 - Material3)
 *
 * Accompanist Pager의 HorizontalPager를 사용하여 튜토리얼 페이지를 표시합니다.
 */
@Composable
fun TutorialScreen(
    tutorialPages: List<TutorialPage>,
    onSignUpClick: () -> Unit
) {
    // Pager 상태 초기화
    val pagerState = rememberPagerState(initialPage = 0)

    HorizontalPager(
        state = pagerState,
        count = tutorialPages.size,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val tutorial = tutorialPages[page]

        // 이미지 애니메이션: 상하로 부드럽게 이동하는 효과
        val infiniteTransition = rememberInfiniteTransition()
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, delayMillis = 300),
                repeatMode = RepeatMode.Reverse
            )
        )

        // 각 페이지 레이아웃 구성
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단 타이틀
            Text(
                text = tutorial.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            // 중간 이미지 (애니메이션 적용)
            Image(
                painter = painterResource(id = tutorial.imageRes),
                contentDescription = tutorial.title,
                modifier = Modifier
                    .size(200.dp)
                    .offset(y = offsetY.dp)
                    .align(Alignment.CenterHorizontally)
            )
            // 하단 설명
            Text(
                text = tutorial.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            // 마지막 페이지일 경우 회원가입 버튼 노출
            if (page == tutorialPages.lastIndex) {
                Button(
                    onClick = { onSignUpClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(text = "회원가입")
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
