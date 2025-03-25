package com.example.fe.ui.screens.onboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch
import com.example.fe.R

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(navController: NavController, viewModel: OnboardingViewModel) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPageData(R.drawable.earth, "당신의 소비 유형을 분석하고\n나만의 행성을 가져보세요"),
        OnboardingPageData(R.drawable.constellation, "특별한 별자리로\n내 카드를 표현하세요"),
        OnboardingPageData(R.drawable.godcard, "신의 힘을 받아\n최고 혜택 카드로 자동 결제하세요")
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPager(
            count = pages.size,
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .weight(1f)
                .padding(top = 32.dp)
        ) { page ->
            OnboardingPage(pageData = pages[page])
        }

        Button(
            onClick = {
                scope.launch {
                    if (pagerState.currentPage < pages.lastIndex) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    } else {
                        navController.navigate("auth") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = if (pagerState.currentPage == pages.lastIndex) "시작하기" else "다음"
            )
        }
    }
}

@Composable
fun OnboardingPage(pageData: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = pageData.imageRes),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = pageData.text,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

data class OnboardingPageData(
    val imageRes: Int,
    val text: String
)
