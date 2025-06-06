package com.example.fe.ui.screens.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.res.painterResource
import com.example.fe.R
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
    onAddCardButtonClick: () -> Unit,
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
                index // 인덱스 그대로 사용 (0: 자동 카드, 1~N: 실제 카드, N+1: 카드 추가)
            } catch (e: Exception) {
                // 예외 발생 시 기본값 반환
                0
            }
        }
    }
    
    // 선택된 카드 인덱스가 변경되면 콜백 호출
    LaunchedEffect(selectedCardIndex) {
        // 인덱스 변환: 
        // 0 -> -1 (자동 카드)
        // 1~N -> 0~(N-1) (실제 카드)
        // N+1 이상 -> -2 (카드 추가)
        val adjustedIndex = when {
            selectedCardIndex == 0 -> -1 // 자동 카드
            selectedCardIndex >= cards.size + 1 -> -2 // 카드 추가 (N+1 이상)
            else -> selectedCardIndex - 1 // 실제 카드
        }
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
            // 자동 카드 추천 항목 (첫 번째 항목)
            item {
                PaymentAddCardItem(
                    cardWidth = cardWidth,
                    centeredness = calculateCenteredness(0, lazyListState, selectedCardIndex),
                    onClick = { onCardIndexSelected(-1) } // 자동 카드는 -1 인덱스로 표현
                )
            }
            
            // 실제 카드 목록
            items(cards) { card ->
                val index = cards.indexOf(card)
                
                PaymentCardItemScroll(
                    card = card,
                    centeredness = calculateCenteredness(index + 1, lazyListState, selectedCardIndex),
                    cardWidth = cardWidth,
                    onClick = { onCardIndexSelected(index) } // 실제 카드는 0부터 시작
                )
            }

            // 카드 추가 버튼 (마지막 항목)
            item {
                AddCardButton(
                    cardWidth = cardWidth,
                    centeredness = calculateCenteredness(cards.size + 1, lazyListState, selectedCardIndex),
                    onClick = { 
                        // 인덱스 선택 (스크롤 이동용)
                        onCardIndexSelected(cards.size + 1)
                    },
                    onAddCardClick = {
                        // 카드 추가 버튼 클릭 시 카드 추가 화면 열기
                        onAddCardButtonClick()
                    }
                )
            }
        }
        
//        // 카드 인디케이터
//        Row(
//            horizontalArrangement = Arrangement.Center,
//            modifier = Modifier
//                .padding(top = 16.dp)
//                .fillMaxWidth()
//        ) {
//            // 자동 카드 인디케이터
//            Box(
//                modifier = Modifier
//                    .padding(horizontal = 4.dp)
//                    .size(if (selectedCardIndex == 0) 10.dp else 8.dp)
//                    .clip(CircleShape)
//                    .background(
//                        if (selectedCardIndex == 0) Color.White else Color.White.copy(alpha = 0.5f)
//                    )
//            )
//
//            // 카드 인디케이터들
//            repeat(cards.size) { index ->
//                val isSelected = index + 1 == selectedCardIndex
//                Box(
//                    modifier = Modifier
//                        .padding(horizontal = 4.dp)
//                        .size(if (isSelected) 10.dp else 8.dp)
//                        .clip(CircleShape)
//                        .background(
//                            if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
//                        )
//                )
//            }
//
//            // 카드 추가 인디케이터
//            Box(
//                modifier = Modifier
//                    .padding(horizontal = 4.dp)
//                    .size(if (selectedCardIndex == cards.size + 1) 10.dp else 8.dp)
//                    .clip(CircleShape)
//                    .background(
//                        if (selectedCardIndex == cards.size + 1) Color.White else Color.White.copy(alpha = 0.5f)
//                    )
//            )
//        }

    }
}

@Composable
fun PaymentCardItemScroll(
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
        // HorizontalCardLayout 사용하여 card.png 표시
        HorizontalCardLayout(
            cardName = "리버스 추천 카드",
            cardImageUrl = "", // URL 대신 로컬 이미지 사용
            cardImage = R.drawable.card, // 로컬 이미지 리소스
            isRecommended = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 카드 이름
        Text(
            text = "리버스 추천 카드",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = lerp(0.8f, 1f, centeredness)),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 설명 텍스트
        Text(
            text = "가장 혜택이 좋은 카드로 자동 결제됩니다",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = lerp(0.7f, 0.9f, centeredness)),
            textAlign = TextAlign.Center
        )
    }
}

// 카드 추가 버튼 컴포넌트 수정
@Composable
fun AddCardButton(
    cardWidth: Dp,
    centeredness: Float,
    onClick: () -> Unit, // 스크롤 이동용
    onAddCardClick: () -> Unit // 카드 추가 화면 열기용
) {
    // 전체 영역을 감싸는 Box 추가
    Box(
        modifier = Modifier
            .width(cardWidth)
            .padding(horizontal = 12.dp)
            .height(200.dp)
            // 테두리 추가
            .border(
                width = 2.dp,
                color = Color(0xFF00BCD4).copy(alpha = lerp(0.5f, 0.8f, centeredness)),
                shape = RoundedCornerShape(8.dp)
            )
            // 배경색 추가 (약간 투명하게)
            .background(
                color = Color(0xFF1A1A2E).copy(alpha = lerp(0.3f, 0.5f, centeredness)),
                shape = RoundedCornerShape(8.dp)
            )
            .graphicsLayer {
                val scale = lerp(0.85f, 1f, centeredness)
                scaleX = scale
                scaleY = scale
                alpha = lerp(0.7f, 1f, centeredness)
            }
            // 전체 영역 클릭 시 두 가지 동작 수행
            .clickable { 
                onClick() // 스크롤 이동
                onAddCardClick() // 카드 추가 화면 열기
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {

            // 카드 추가 UI - 아이콘만 표시
            Box(
                modifier = Modifier
                    .size(40.dp)  // 아이콘 크기만큼만 영역 지정
                    .clickable {
                        onClick() // 스크롤 이동
                        onAddCardClick() // 카드 추가 화면 열기
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "카드 추가",
                    tint = Color(0xFF00BCD4),
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
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