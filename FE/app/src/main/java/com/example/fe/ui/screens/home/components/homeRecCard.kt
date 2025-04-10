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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.fe.data.model.cardRecommend.CardInfoApi
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
import com.example.fe.ui.screens.home.HomeViewModel
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.util.Log
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeRecCard(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onCardClick: (Int) -> Unit = {}
) {
    val top3Cards by viewModel.top3Cards.collectAsState()
    val recommendAmount by viewModel.recommendAmount.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    
    // coroutineScope 추가 - 최상위 레벨에서 한 번만 선언
    val coroutineScope = rememberCoroutineScope()
    
    // 추천 카드가 없는 경우 검색 결과에서 카드 표시
    val cardsToShow = if (top3Cards.isEmpty()) {
        searchResults.take(3) // 검색 결과에서 상위 3개 카드만 표시
    } else {
        top3Cards
    }
    
    // 카드가 하나도 없는 경우 기본 카드 표시
    if (cardsToShow.isEmpty()) {
        GlassSurface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 8.dp),
            cornerRadius = 16f
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "추천 카드가 없습니다",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // 화면 너비 가져오기
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    // 카드 너비 지정
    val cardWidth = 170.dp
    
    // 화면 중앙에 배치하기 위한 양쪽 패딩 계산 (MyCardScreen 방식 적용)
    val horizontalPadding = (screenWidth - cardWidth) / 2
    
    // 현재 화면 밀도 가져오기
    val density = LocalDensity.current.density
    
    // 디버깅을 위한 로그
    LaunchedEffect(Unit) {
        Log.d("HomeRecCard", "screenWidth: $screenWidth")
        Log.d("HomeRecCard", "cardWidth: $cardWidth")
        Log.d("HomeRecCard", "horizontalPadding: $horizontalPadding")
    }
    
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp, vertical = 8.dp),
        cornerRadius = 16f
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "이런 카드는 어떠세요?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                // 추천 금액 표시 추가
                if (recommendAmount > 0) {
                    val formattedAmount = NumberFormat.getNumberInstance(Locale.KOREA).format(recommendAmount)
                    Text(
                        text = "3개월 간 총 ${formattedAmount}원 사용",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // 상단 텍스트와 카드 슬라이더 사이에 간격 추가
            Spacer(modifier = Modifier.height(24.dp))
            
            // 페이저 상태
            val pagerState = rememberPagerState(
                pageCount = { cardsToShow.size },
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
                    .height(160.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 10.dp,  // 카드가 약간 붙도록 조정
                    contentPadding = PaddingValues(horizontal = 70.dp), // CardCarousel 방식 적용
                    modifier = Modifier.fillMaxWidth(),
                    flingBehavior = flingBehavior,
                    key = { index -> cardsToShow[index].cardId }
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val pageOffset = (
                                (pagerState.currentPage - page) + pagerState
                                    .currentPageOffsetFraction
                                ).absoluteValue

                        // 카드 스케일 및 투명도를 CardCarousel 방식으로 조정
                        val scale = 1.2f - (pageOffset * 0.3f).coerceIn(0f, 0.3f)
                        val alpha = 1f - (pageOffset * 0.4f).coerceIn(0f, 0.4f)
                        
                        Box(
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    alpha = alpha,
                                    clip = false,
                                    cameraDistance = 12f * density
                                )
                                .clickable {
                                    if (page != pagerState.currentPage) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(page)
                                        }
                                    }
                                },
                        ) {
                            // 카드 이미지
                            val card = cardsToShow[page]
                            val hasImageUrl = !card.imageUrl.isNullOrEmpty()
                            
                            if (hasImageUrl) {
                                // Coil을 사용하여 API로부터 받은 이미지 URL을 로드
                                AsyncImage(
                                    model = card.imageUrl,
                                    contentDescription = card.cardName,
                                    modifier = Modifier
                                        .width(cardWidth)
                                        .height(110.dp),
                                    alignment = Alignment.Center
                                )
                            } else {
                                // 기본 카드 이미지 사용
                                HorizontalCardLayout(
                                    cardImage = R.drawable.card,
                                    modifier = Modifier
                                        .width(cardWidth)
                                        .height(110.dp),
                                    cardName = card.cardName,
                                    cardImageUrl = ""
                                )
                            }
                        }
                    }
                }
            }

            // 페이지 인디케이터
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(cardsToShow.size) { index ->
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

            Spacer(modifier = Modifier.height(12.dp))

            // 카드 정보 (현재 선택된 카드)
            if (cardsToShow.isNotEmpty() && pagerState.currentPage < cardsToShow.size) {
                val currentCard = cardsToShow[pagerState.currentPage]
                
                // 카드 혜택 정보 추출
                val benefits = currentCard.cardInfo.split(",")
                
                // 슬라이더와 카드 정보 사이 간격 최소화
                Spacer(modifier = Modifier.height(2.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentCard.cardName,
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    
                    // 혜택 목록 표시
                    benefits.forEachIndexed { index, benefit ->
                        if (index == 0) { // 첫 번째 혜택
                            Text(
                                text = benefit.trim(),
                                fontSize = 16.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else if (index == 1) { // 두 번째 혜택
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = benefit.trim(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                
                                // 혜택이 더 있는 경우 같은 줄에 표시
                                if (benefits.size > 2) {
                                    Text(
                                        text = " 외 ${benefits.size - 2}개 혜택",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
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
