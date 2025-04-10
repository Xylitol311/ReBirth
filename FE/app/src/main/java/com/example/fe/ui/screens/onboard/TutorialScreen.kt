package com.example.fe.ui.screens.onboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

/**
 * TutorialScreen
 *
 * - StarryBackground(별이 빛나는 배경) 위에서 HorizontalPager를 사용해 튜토리얼 페이지를 좌우로 넘깁니다.
 * - 페이지 하단에는 PagerIndicator(동그라미)을 표시하여 현재 위치를 안내합니다.
 * - 마지막 페이지에는 "회원가입" 버튼이 노출되어 onSignUpClick 콜백을 호출합니다.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun TutorialScreen(
    tutorialPages: List<TutorialPage>,
    onSignUpClick: () -> Unit
) {
    // Pager 상태: pageCount는 tutorialPages.size
    val pagerState = rememberPagerState()

    // OS 네비게이션 바 높이만큼 패딩을 주어 침범을 방지 (하단 영역 확보)
    // WindowInsets.navigationBars 를 사용해 기기마다 다른 navigation bar 크기를 파악
    val navigationBarHeightPx = WindowInsets.navigationBars
        .getBottom(LocalDensity.current)
    val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }

    // 별이 빛나는 배경
    StarryBackground {
        // 전체 화면에 Pager + 하단 인디케이터/버튼 배치
        Box(modifier = Modifier.fillMaxSize()) {
            // 1) HorizontalPager
            HorizontalPager(
                count = tutorialPages.size,
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp + navigationBarHeightDp) // 네비게이션 바 높이 고려
            ) { pageIndex ->
                val pageData = tutorialPages[pageIndex]
                // 수정된 부분: pageIndex 전달
                TutorialPageContent(pageData, pageIndex)
            }

            // 2) 하단 영역 구성
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navigationBarHeightDp + 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 마지막 페이지라면 "회원가입" 버튼 표시
                if (pagerState.currentPage == tutorialPages.lastIndex) {
                    Button(
                        onClick = onSignUpClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF212F50) // 원하는 색상으로 변경
                        ),
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .padding(bottom = 20.dp)
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "회원가입",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }

                // 인디케이터
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    activeColor = Color.White,
                    inactiveColor = Color.LightGray.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 8.dp),
                    indicatorWidth = 8.dp,
                    indicatorHeight = 8.dp
                )
            }
        }
    }
}

/**
 * TutorialPageContent
 *
 * 각 페이지에서 보여줄 상단 문구, 중앙 이미지, 하단 설명 박스를 구성합니다.
 * 제목-이미지 여백:이미지-설명 여백 = 1:1.5 비율 적용
 */
@Composable
fun TutorialPageContent(
    page: TutorialPage,
    pageIndex: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.6f))

        // 수정된 부분: 페이지에 따라 상단 타이틀을 두 줄로 나누어 표시
        val customTitle = when (pageIndex) {
            0 -> buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("당신의 소비는\n")
                }
                withStyle(style = SpanStyle(color = Color(0xFF36E1E1), fontWeight = FontWeight.Bold)) {
                    append("조화로운 지구형입니다.")
                }
            }
            1 -> buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("당신에게 필요한 별자리는\n")
                }
                withStyle(style = SpanStyle(color = Color(0xFF36E1E1), fontWeight = FontWeight.Bold)) {
                    append("신한카드 처음(ANNIVERSE)")
                }
            }
            2 -> buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("REBIRTH 슈퍼 카드로\n")
                }
                withStyle(style = SpanStyle(color = Color(0xFF36E1E1), fontWeight = FontWeight.Bold)) {
                    append("가장 최적의 별자리를 제안해드립니다")
                }
            }
            else -> buildAnnotatedString {
                append(page.title) // 혹시 모를 예외 케이스
            }
        }

        Text(
            text = customTitle,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 70.dp) // 제목-이미지 여백 (비율 1)
        )

        // 중앙 이미지
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(200.dp)
        )

        // 이미지-설명 여백 (비율 1.5)
        Spacer(modifier = Modifier.height(85.dp))

        // 하단 설명 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = page.description,
                fontSize = 21.5.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))
    }
}

/**
 * 데이터 클래스
 */
data class TutorialPage(
    val imageRes: Int,
    val title: String,
    val description: String
)
