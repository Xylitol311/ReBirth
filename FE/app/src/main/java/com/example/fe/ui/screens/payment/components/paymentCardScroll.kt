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
                
                // 아이템의 중앙 위치 계산
                val itemCenter by remember(lazyListState) {
                    derivedStateOf {
                        val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo
                        // 안전하게 인덱스 확인
                        val itemInfo = if (index < visibleItemsInfo.size) {
                            visibleItemsInfo.find { it.index == index }
                        } else null
                        
                        if (itemInfo != null) {
                            val itemCenterX = itemInfo.offset + (itemInfo.size / 2)
                            val listCenterX = lazyListState.layoutInfo.viewportSize.width / 2
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
                
                PaymentCardItem(
                    card = card,
                    centeredness = centeredness,
                    cardWidth = cardWidth,
                    onClick = { onCardIndexSelected(index) }
                )
            }
            
            // 카드 추가 아이템
            item {
                // 아이템의 중앙 위치 계산
                val index = cards.size
                val itemCenter by remember(lazyListState) {
                    derivedStateOf {
                        val visibleItemsInfo = lazyListState.layoutInfo.visibleItemsInfo
                        // 안전하게 인덱스 확인
                        val itemInfo = if (index < visibleItemsInfo.size) {
                            visibleItemsInfo.find { it.index == index }
                        } else null
                        
                        if (itemInfo != null) {
                            val itemCenterX = itemInfo.offset + (itemInfo.size / 2)
                            val listCenterX = lazyListState.layoutInfo.viewportSize.width / 2
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