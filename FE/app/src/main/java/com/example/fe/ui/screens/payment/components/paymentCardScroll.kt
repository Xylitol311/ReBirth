package com.example.fe.ui.screens.payment.components

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import com.example.fe.ui.components.cards.HorizontalCardLayout
import com.example.fe.ui.screens.payment.PaymentCardInfo
import kotlin.math.abs
import kotlin.math.pow

@Composable
fun PaymentCardScroll(
    cards: List<PaymentCardInfo>,
    cardWidth: Dp,
    horizontalPadding: Dp,
    lazyListState: LazyListState,
    onCardIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
    
    // 현재 선택된 카드 인덱스
    val selectedCardIndex by remember {
        derivedStateOf {
            val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf 0
            
            try {
                // 화면 중앙에 가장 가까운 아이템 찾기
                val listCenter = lazyListState.layoutInfo.viewportSize.width / 2
                
                val closestItem = visibleItemsInfo.minByOrNull { 
                    abs((it.offset + it.size / 2) - listCenter)
                }
                
                // 인덱스가 범위를 벗어나지 않도록 확인
                val index = closestItem?.index ?: 0
                if (index <= cards.size) index else cards.size
            } catch (e: Exception) {
                // 예외 발생 시 기본값 반환
                0
            }
        }
    }
    
    // 선택된 카드 인덱스가 변경되면 콜백 호출
    LaunchedEffect(selectedCardIndex) {
        onCardIndexSelected(selectedCardIndex)
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 카드 슬라이더
        LazyRow(
            state = lazyListState,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            flingBehavior = snapBehavior,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            items(cards) { card ->
                val index = cards.indexOf(card)
                
                // 아이템의 중앙 위치 계산 - 개선된 방식
                val itemCenter by remember(lazyListState) {
                    derivedStateOf {
                        val listCenter = lazyListState.layoutInfo.viewportSize.width / 2
                        val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo
                        
                        // 현재 선택된 인덱스와의 거리 계산
                        val distanceFromSelected = abs(index - selectedCardIndex)
                        
                        // 화면에 보이는 아이템 중에서 현재 카드 찾기
                        val itemInfo = visibleItemsInfo.find { it.index == index }
                        
                        if (itemInfo != null) {
                            // 실제 위치 기반 계산
                            val itemCenterX = itemInfo.offset + (itemInfo.size / 2)
                            itemCenterX - listCenter
                        } else if (distanceFromSelected <= 2) {
                            // 화면에 보이지 않지만 선택된 카드와 가까운 경우
                            // 거리에 따라 적절한 값 반환
                            when (distanceFromSelected) {
                                0 -> 0f       // 선택된 카드는 중앙에 위치
                                1 -> 300f     // 바로 옆 카드
                                else -> 600f  // 두 칸 떨어진 카드
                            } * if (index < selectedCardIndex) -1 else 1
                        } else {
                            // 화면에서 멀리 떨어진 카드
                            1000f * if (index < selectedCardIndex) -1 else 1
                        }
                    }
                }
                
                // 중앙에 있는 정도 계산 (0: 중앙에서 멀리, 1: 정확히 중앙)
                val centeredness = remember(itemCenter, selectedCardIndex) {
                    val maxDistance = 500f // 최대 거리 설정
                    val distance = minOf(abs(itemCenter.toFloat()), maxDistance)
                    val normalized = 1f - (distance / maxDistance)
                    
                    // 선택된 카드와의 거리도 고려
                    val distanceFactor = when (abs(index - selectedCardIndex)) {
                        0 -> 1.0f
                        1 -> 0.8f
                        2 -> 0.6f
                        else -> 0.4f
                    }
                    
                    // 두 요소를 결합하여 최종 centeredness 계산
                    val combined = normalized * distanceFactor
                    EaseOutQuart.transform(combined)
                }
                
                PaymentCardItem(
                    card = card,
                    centeredness = centeredness,
                    cardWidth = cardWidth,
                    onClick = { onCardIndexSelected(index) }
                )
            }
            
            // 카드 추가 아이템
            item {
                val index = cards.size
                
                // 아이템의 중앙 위치 계산 - 개선된 방식
                val itemCenter by remember(lazyListState, selectedCardIndex) {
                    derivedStateOf {
                        val listCenter = lazyListState.layoutInfo.viewportSize.width / 2
                        val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo
                        
                        // 현재 선택된 인덱스와의 거리 계산
                        val distanceFromSelected = abs(index - selectedCardIndex)
                        
                        // 화면에 보이는 아이템 중에서 현재 카드 찾기
                        val itemInfo = visibleItemsInfo.find { it.index == index }
                        
                        if (itemInfo != null) {
                            // 실제 위치 기반 계산
                            val itemCenterX = itemInfo.offset + (itemInfo.size / 2)
                            itemCenterX - listCenter
                        } else if (distanceFromSelected <= 2) {
                            // 화면에 보이지 않지만 선택된 카드와 가까운 경우
                            300f * if (index < selectedCardIndex) -1 else 1
                        } else {
                            // 화면에서 멀리 떨어진 카드
                            1000f
                        }
                    }
                }
                
                // 중앙에 있는 정도 계산 (0: 중앙에서 멀리, 1: 정확히 중앙)
                val centeredness = remember(itemCenter, selectedCardIndex) {
                    // 선택된 카드인 경우 바로 1.0 반환
                    if (selectedCardIndex == index) return@remember 1.0f
                    
                    val maxDistance = 500f // 최대 거리 설정
                    val distance = minOf(abs(itemCenter.toFloat()), maxDistance)
                    val normalized = 1f - (distance / maxDistance)
                    EaseOutQuart.transform(normalized)
                }
                
                PaymentAddCardItem(
                    cardWidth = cardWidth,
                    centeredness = centeredness,
                    onClick = { onCardIndexSelected(cards.size) }
                )
            }
        }
        
        // 카드 인디케이터
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            repeat(cards.size) { index ->
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
            
            // 카드 추가 인디케이터
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (selectedCardIndex >= cards.size) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectedCardIndex >= cards.size) Color.White 
                        else Color.White.copy(alpha = 0.5f)
                    )
            )
        }
    }
}

@Composable
fun PaymentCardItem(
    card: PaymentCardInfo,
    centeredness: Float,
    cardWidth: Dp,
    onClick: () -> Unit
) {
    // 카드 아이템
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(cardWidth)
            .padding(horizontal = 12.dp)
            .graphicsLayer {
                // 중앙에 있을수록 점진적으로 크기가 커지고 선명해짐
                val scale = lerp(0.85f, 1f, centeredness)
                scaleX = scale
                scaleY = scale
                alpha = lerp(0.7f, 1f, centeredness)
            }
            .clickable { onClick() }
    ) {
        // 카드 이미지
        HorizontalCardLayout(
            cardImage = painterResource(id = card.cardImage),
            modifier = Modifier
                .height(170.dp)
                .fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 카드 이름
        Text(
            text = card.cardName,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = lerp(0.8f, 1f, centeredness)),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun PaymentAddCardItem(
    cardWidth: Dp,
    centeredness: Float,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(cardWidth)
            .padding(horizontal = 12.dp)
            .height(200.dp)
            .graphicsLayer {
                // 중앙에 있을수록 점진적으로 크기가 커지고 선명해짐
                val scale = lerp(0.85f, 1f, centeredness)
                scaleX = scale
                scaleY = scale
                alpha = lerp(0.7f, 1f, centeredness)
            }
            .clickable { onClick() }
    ) {
        // 카드 추가 UI
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF2D2A57).copy(alpha = lerp(0.5f, 0.7f, centeredness))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "카드 추가",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "카드 추가하기",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = lerp(0.8f, 1f, centeredness)),
            textAlign = TextAlign.Center
        )
    }
}

// 보간 함수 (Linear Interpolation)
private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

// 이징 함수
private object EaseOutQuart {
    fun transform(x: Float): Float {
        return 1f - (1f - x).pow(4)
    }
}