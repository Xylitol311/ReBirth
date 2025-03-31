package com.example.fe.ui.screens.home.components

import androidx.compose.animation.core.EaseOutQuart
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
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape

data class CardRecommendation(
    val cardImage: Int,
    val title: String,
    val benefit: String
)

@Composable
fun HomeRecCard() {
    // 카드 추천 섹션 - 배경 제거
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 제목 텍스트 - 흰색으로 변경 및 중앙 정렬
        Text(
            text = "이런 카드는 어떠세요?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // 카드 추천 목록
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
        
        // 카드 가로 스크롤
        val listState = rememberLazyListState()
        val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        
        // 화면 너비의 절반에서 카드 너비의 절반을 뺀 값을 계산
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val cardWidth = 280.dp
        val horizontalPadding = (screenWidth - cardWidth) / 2
        
        // 현재 선택된 카드 인덱스 계산
        val selectedCardIndex by remember {
            derivedStateOf {
                val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
                if (visibleItemsInfo.isEmpty()) return@derivedStateOf 0
                
                // 화면 중앙에 가장 가까운 아이템 찾기
                val listCenter = listState.layoutInfo.viewportSize.width / 2
                
                visibleItemsInfo.minByOrNull { 
                    abs((it.offset + it.size / 2) - listCenter)
                }?.index ?: 0
            }
        }
        
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            flingBehavior = snapBehavior
        ) {
            items(cardRecommendations) { card ->
                // 현재 아이템의 인덱스
                val index = cardRecommendations.indexOf(card)
                
                // 아이템의 중앙 위치 계산
                val itemCenter by remember(listState) {
                    derivedStateOf {
                        val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
                        val itemInfo = visibleItemsInfo.find { it.index == index }
                        
                        if (itemInfo != null) {
                            val itemCenterX = itemInfo.offset + (itemInfo.size / 2)
                            val listCenterX = listState.layoutInfo.viewportSize.width / 2
                            itemCenterX - listCenterX
                        } else {
                            10000 // 화면 밖에 있는 경우 큰 값 설정
                        }
                    }
                }
                
                // 중앙에 있는 정도 계산 (0: 중앙에서 멀리, 1: 정확히 중앙)
                val centeredness = remember(itemCenter) {
                    val maxDistance = 500f // 최대 거리 설정
                    val distance = minOf(abs(itemCenter.toFloat()), maxDistance)
                    val normalized = 1f - (distance / maxDistance)
                    EaseOutQuart.transform(normalized)
                }
                
                // 카드 아이템과 텍스트를 포함하는 컬럼
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(280.dp)
                        .graphicsLayer {
                            // 중앙에 있을수록 크기가 커지고 선명해짐
                            val scale = lerp(0.85f, 1f, centeredness)
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(0.7f, 1f, centeredness)
                        }
                ) {
                    // 카드 이미지
                    Box(
                        modifier = Modifier
                            .height(170.dp)
                            .fillMaxWidth()
                    ) {
                        HorizontalCardLayout(
                            cardName = card.title,
                            cardImageUrl = "",
                            cardImage = card.cardImage,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        
                        // 선택된 카드에만 표시되는 추가 효과 (체크 아이콘)
                        if (centeredness > 0.7f) { // 70% 이상 중앙에 있을 때만 표시
                            val checkAlpha = ((centeredness - 0.7f) / 0.3f).coerceIn(0f, 1f)
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-16).dp, y = 16.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF4CAF50).copy(alpha = checkAlpha))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "추천",
                                    tint = Color.White.copy(alpha = checkAlpha)
                                )
                            }
                        }
                    }
                    
                    // 카드 정보 텍스트 - 카드 아래에 배치
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 카드 이름
                    Text(
                        text = card.title,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    
                    // 혜택 텍스트
                    Text(
                        text = card.benefit,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        
        // 페이지 인디케이터 추가
        Spacer(modifier = Modifier.height(16.dp))
        
        // 페이지 인디케이터 (점 형태)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(cardRecommendations.size) { index ->
                val isSelected = index == selectedCardIndex
                
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

// 보간 함수 (Linear Interpolation)
private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

