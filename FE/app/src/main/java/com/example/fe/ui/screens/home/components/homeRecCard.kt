package com.example.fe.ui.screens.home.components

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.ui.components.cards.HorizontalCardLayout
import kotlin.math.abs
import kotlin.math.pow
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import com.example.fe.ui.components.backgrounds.GlassSurface
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.res.painterResource

data class CardRecommendation(
    val cardImage: Int,
    val title: String,
    val benefit: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeRecCard(
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        cornerRadius = 16f
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "이런 카드는 어떠세요?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            val cardRecommendations = listOf(
                CardRecommendation(
                    cardImage = R.drawable.card,
                    title = "하나 VIVA e Platinum 카드",
                    benefit = "카페 최대 20% 할인"
                ),
                CardRecommendation(
                    cardImage = R.drawable.card,
                    title = "신한 Deep Dream 카드",
                    benefit = "식당 최대 15% 할인"
                ),
                CardRecommendation(
                    cardImage = R.drawable.card,
                    title = "KB 국민 톡톡 카드",
                    benefit = "쇼핑 최대 10% 할인"
                )
            )

            // 현재 화면 밀도 가져오기
            val density = LocalDensity.current
            
            // 페이저 상태
            val pagerState = rememberPagerState(
                pageCount = { cardRecommendations.size },
                initialPage = 0
            )

            // 페이저 스냅 동작 개선을 위한 설정
            val flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapPositionalThreshold = 0.1f
            )

            // 카드 슬라이더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 0.dp,
                    flingBehavior = flingBehavior,
                    contentPadding = PaddingValues(start = 70.dp, end = 50.dp),
                    pageSize = PageSize.Fixed(280.dp),
                    key = { it }
                ) { page ->
                    val pageOffset = abs(
                        (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    )

                    // 현재 카드는 더 크게, 다른 카드는 작게
                    val scale = if (page == pagerState.currentPage) 1.3f else 0.85f
                    
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .height(130.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                alpha = lerp(0.7f, 1f, 1f - pageOffset.coerceIn(0f, 1f)),
                                clip = false,
                                cameraDistance = 12f * density.density
                            )
                    ) {
                        // 카드 이미지
                        HorizontalCardLayout(
                            cardImage = painterResource(id = cardRecommendations[page].cardImage),
                            modifier = Modifier
                                .width(280.dp)
                                .height(170.dp)
                        )
                    }
                }
            }

            // 카드 정보 (현재 선택된 카드)
            val currentCard = cardRecommendations[pagerState.currentPage]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentCard.title,
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = currentCard.benefit,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 페이지 인디케이터
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(cardRecommendations.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

// 보간 함수
private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

// 이징 함수
private object EaseOutQuart {
    fun transform(x: Float): Float {
        return 1f - (1f - x).pow(4)
    }
}
