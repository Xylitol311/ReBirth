package com.example.fe.ui.screens.payment.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.material.icons.filled.Star
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
                index // 인덱스 그대로 사용 (0: 카드 추가, 1~N: 실제 카드)
            } catch (e: Exception) {
                // 예외 발생 시 기본값 반환
                0
            }
        }
    }
    
    // 선택된 카드 인덱스가 변경되면 콜백 호출
    LaunchedEffect(selectedCardIndex) {
        // 카드 추가 화면은 -1로 표현, 실제 카드는 0부터 시작하도록 변환
        val adjustedIndex = if (selectedCardIndex == 0) -1 else selectedCardIndex - 1
        onCardIndexSelected(adjustedIndex)
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
            // 카드 추가 버튼을 첫 번째 항목으로 배치
            item {
                PaymentAddCardItem(
                    cardWidth = cardWidth,
                    centeredness = calculateCenteredness(0, lazyListState, selectedCardIndex),
                    onClick = { onCardIndexSelected(-1) } // 카드 추가는 -1 인덱스로 표현
                )
            }
            
            // 실제 카드 목록
            items(cards) { card ->
                val index = cards.indexOf(card)
                
                PaymentCardItem(
                    card = card,
                    centeredness = calculateCenteredness(index + 1, lazyListState, selectedCardIndex),
                    cardWidth = cardWidth,
                    onClick = { onCardIndexSelected(index) } // 실제 카드는 0부터 시작
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
            // 카드 추가 인디케이터
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (selectedCardIndex == 0) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectedCardIndex == 0) Color.White else Color.White.copy(alpha = 0.5f)
                    )
            )
            
            // 카드 인디케이터들
            repeat(cards.size) { index ->
                val isSelected = index + 1 == selectedCardIndex
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

@Composable
fun PaymentCardItem(
    card: PaymentCardInfo,
    centeredness: Float,
    cardWidth: Dp,
    onClick: () -> Unit
) {
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
        // 카드 UI
        HorizontalCardLayout(
            cardName = card.cardName,
            cardImageUrl = card.cardImageUrl,
            cardImage = card.cardImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
                imageVector = Icons.Default.Star,
                contentDescription = "자동 카드 추천",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "어떤 카드가 등장할까요?",
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

// centeredness 계산 함수 수정
private fun calculateCenteredness(index: Int, lazyListState: LazyListState, selectedCardIndex: Int): Float {
    // 선택된 카드인 경우 바로 1.0 반환
    if (selectedCardIndex == index) return 1.0f
    
    val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo
    val listCenter = lazyListState.layoutInfo.viewportSize.width / 2
    
    // 화면에 보이는 아이템 중에서 현재 카드 찾기
    val itemInfo = visibleItemsInfo.find { it.index == index }
    
    if (itemInfo != null) {
        // 실제 위치 기반 계산
        val itemCenterX = itemInfo.offset + (itemInfo.size / 2)
        val distance = abs(itemCenterX - listCenter)
        val maxDistance = 500f // 최대 거리 설정
        return 1f - minOf(distance / maxDistance, 1f)
    }
    
    // 화면에 보이지 않는 경우 거리에 따른 값 반환
    val distanceFromSelected = abs(index - selectedCardIndex)
    return when {
        distanceFromSelected <= 1 -> 0.7f
        distanceFromSelected <= 2 -> 0.5f
        else -> 0.3f
    }
}