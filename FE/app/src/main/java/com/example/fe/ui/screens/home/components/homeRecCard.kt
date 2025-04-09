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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeRecCard(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {
    val recommendedCards by viewModel.recommendedCards.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    
    // 추천 카드가 없는 경우 검색 결과에서 카드 표시
    val cardsToShow = if (recommendedCards.isEmpty()) {
        searchResults.take(3) // 검색 결과에서 상위 3개 카드만 표시
    } else {
        recommendedCards
    }
    
    // 카드가 하나도 없는 경우 기본 카드 표시
    if (cardsToShow.isEmpty()) {
        GlassSurface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 8.dp),
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
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 8.dp),
        cornerRadius = 16f
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "이런 카드는 어떠세요?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // 현재 화면 밀도 가져오기
            val density = LocalDensity.current
            
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
                        val card = cardsToShow[page]
                        val hasImageUrl = card.imageUrl.isNotEmpty()
                        
                        if (hasImageUrl) {
                            // Coil을 사용하여 API로부터 받은 이미지 URL을 로드
                            AsyncImage(
                                model = card.imageUrl,
                                contentDescription = card.cardName,
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(170.dp)
                            )
                        } else {
                            // 기본 카드 이미지 사용
                            HorizontalCardLayout(
                                cardImage = R.drawable.card,
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(170.dp),
                                cardName = card.cardName,
                                cardImageUrl = ""
                            )
                        }
                    }
                }
            }

            // 카드 정보 (현재 선택된 카드)
            if (cardsToShow.isNotEmpty() && pagerState.currentPage < cardsToShow.size) {
                val currentCard = cardsToShow[pagerState.currentPage]
                
                // 카드 혜택 정보 추출
                val benefits = currentCard.cardInfo.split(",").firstOrNull() ?: "혜택 정보 없음"
                
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
                    Text(
                        text = benefits,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 페이지 인디케이터
            Spacer(modifier = Modifier.height(16.dp))
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
