package com.example.fe.ui.screens.onboard

import com.example.fe.R
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

/**
 * TutorialScreen
 *
 * - StarryBackground(별이 빛나는 배경) 위에서 HorizontalPager를 사용해 튜토리얼 페이지를 좌우로 넘깁니다.
 * - 페이지 하단에는 PagerIndicator(동그라미)을 표시하여 현재 위치를 안내합니다.
 * - 마지막 페이지에는 "회원가입" 버튼이 노출되어 onSignUpClick 콜백을 호출합니다.
 */
/**
 * TutorialScreen
 *
 * - 배경 이미지 위에서 HorizontalPager를 사용해 튜토리얼 페이지를 좌우로 넘깁니다.
 * - 페이지 하단에는 PagerIndicator(동그라미)을 표시하여 현재 위치를 안내합니다.
 * - 첫 번째, 두 번째 페이지에는 "다음" 버튼이 노출되어 다음 페이지로 이동합니다.
 * - 마지막 페이지에는 "회원가입" 버튼이 노출되어 onSignUpClick 콜백을 호출합니다.
 *//**
 * TutorialScreen
 *
 * - 배경 이미지 위에서 HorizontalPager를 사용해 튜토리얼 페이지를 좌우로 넘깁니다.
 * - 페이지 하단에는 PagerIndicator(동그라미)을 표시하여 현재 위치를 안내합니다.
 * - 첫 번째, 두 번째 페이지에는 "다음" 버튼이 노출되어 다음 페이지로 이동합니다.
 * - 마지막 페이지에는 "회원가입" 버튼이 노출되어 onSignUpClick 콜백을 호출합니다.
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun TutorialScreen(
    tutorialPages: List<TutorialPage>,
    onSignUpClick: () -> Unit
) {
    // 색상 정의
    val white = Color(0xFFFFFFFF)
    val brightRed = Color(0xD2FF7777)
    val brightGreen = Color(0xFF69F0AE)
    val calendarlightBlue = Color(0xFF72909A)
    val calendarBlue = Color(0xFF00BCD4) // 네온 블루
    val calenderBlackBlue = Color(0xFF797979) // 빛나는 노란색

    // Pager 상태: pageCount는 tutorialPages.size
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    // OS 네비게이션 바 높이만큼 패딩을 주어 침범을 방지 (하단 영역 확보)
    // WindowInsets.navigationBars 를 사용해 기기마다 다른 navigation bar 크기를 파악
    val navigationBarHeightPx = WindowInsets.navigationBars
        .getBottom(LocalDensity.current)
    val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }

    // 배경 이미지가 꽉 차게 표시
    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.background_image), // 배경 이미지 리소스 ID 설정
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop, // 이미지가 화면에 꽉 차도록 설정
            modifier = Modifier.fillMaxSize()
        )

        // 기존 화면 요소들
        Box(modifier = Modifier.fillMaxSize()) {
            // 1) HorizontalPager
            HorizontalPager(
                count = tutorialPages.size,
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 160.dp + navigationBarHeightDp) // 네비게이션 바 높이 + 버튼 영역 고려
                    .padding(top = 40.dp)// 네비게이션 바 높이 + 버튼 영역 고려
            ) { pageIndex ->
                val pageData = tutorialPages[pageIndex]
                TutorialPageContent(pageData, pageIndex)
            }

            // 2) 하단 영역 구성
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navigationBarHeightDp + 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 현재 페이지가 마지막 페이지인지 확인
                val isLastPage = pagerState.currentPage == tutorialPages.lastIndex

                // 버튼 영역
                Button(
                    onClick = {
                        if (isLastPage) {
                            onSignUpClick()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastPage) Color.White else Color(0xFF212F50)
                    ),
                    //border = BorderStroke(1.dp, calendarBlue), // 테두리 추가
                    modifier = Modifier
                        .padding(40.dp) // 좌우 너비 줄임
                        .padding(bottom = 20.dp)
                        .fillMaxWidth()
                        .height(60.dp) // 버튼 높이 증가
                ) {
                    Text(
                        text = if (isLastPage) "회원가입" else "다음",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color =if (isLastPage) Color(0xFF212F50) else Color.White
                    )
                }

                // 인디케이터
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    activeColor = calendarBlue,
                    inactiveColor = calendarlightBlue,
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
    val calendarBlue = Color(0xFF00BCD4) // 네온 블루

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
                withStyle(style = SpanStyle(color = calendarBlue, fontWeight = FontWeight.Bold)) {
                    append("조화로운 지구형입니다.")
                }
            }
            1 -> buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("당신에게 필요한 별자리는\n")
                }
                withStyle(style = SpanStyle(color = calendarBlue, fontWeight = FontWeight.Bold)) {
                    append("신한카드 처음(ANNIVERSE)")
                }
            }
            2 -> buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("REBIRTH 슈퍼 카드로\n")
                }
                withStyle(style = SpanStyle(color = calendarBlue, fontWeight = FontWeight.Bold)) {
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