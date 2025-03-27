package com.example.fe.ui.screens.myCard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.fe.ui.components.backgrounds.StarryBackground
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.fe.ui.screens.myCard.CardItem
import com.example.fe.ui.screens.myCard.CardItemWithVisibility
import com.example.fe.ui.screens.myCard.CardOrderManager
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

// 카드 위치 조정에 필요한 정보
data class CardSwapInfo(
    val cardId: Int,
    val sourceIndex: Int,
    val targetIndex: Int
)

@Composable
fun CardManagementScreen(
    onBackClick: () -> Unit
) {
    // 초기 카드 데이터
    val initialCards = remember {
        listOf(
            CardItemWithVisibility(CardItem(1, "토스 신한카드 Mr.Life", "•••• •••• •••• 3456")),
            CardItemWithVisibility(CardItem(2, "현대카드", "•••• •••• •••• 4567")),
            CardItemWithVisibility(CardItem(3, "삼성카드", "•••• •••• •••• 5678"))
        )
    }
    
    // 카드 매니저 초기화
    LaunchedEffect(Unit) {
        CardOrderManager.initializeIfEmpty(initialCards)
    }
    
    // 상태 관리
    var cards by remember { mutableStateOf(CardOrderManager.sortedCards.toList()) }
    var hasChanges by remember { mutableStateOf(false) }
    
    // 카드 정렬 함수: 보이는 카드가 위로, 숨겨진 카드가 아래로
    fun sortCards(cardList: List<CardItemWithVisibility>): List<CardItemWithVisibility> {
        return cardList.sortedBy { !it.isVisible }
    }
    
    // 햅틱 피드백
    val haptic = LocalHapticFeedback.current

    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            val fromItem = cards[from.index]
            val toItem = cards[to.index]
            
            // 숨겨진 카드는 이동 불가능
            if (!fromItem.isVisible || !toItem.isVisible) return@rememberReorderableLazyListState
            
            // 보이는 카드들 중에서의 인덱스만 계산
            val visibleCards = cards.filter { it.isVisible }
            val fromVisibleIndex = visibleCards.indexOf(fromItem)
            val toVisibleIndex = visibleCards.indexOf(toItem)
            
            if (fromVisibleIndex != -1 && toVisibleIndex != -1) {
                val newCards = cards.toMutableList()
                newCards.removeAt(from.index)
                newCards.add(to.index, fromItem)
                cards = newCards
                hasChanges = true
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        },
        canDragOver = { draggedOver, dragging ->
            // 보이는 카드 사이에서만 드래그 가능
            val draggedOverItem = cards[draggedOver.index]
            val draggingItem = cards[dragging.index]
            draggedOverItem.isVisible && draggingItem.isVisible
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1E))
    ) {
        StarryBackground(
            scrollOffset = 0f,
            starCount = 150,
            horizontalOffset = 0f,
            modifier = Modifier.fillMaxSize()
        ) {
            Box {}
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "카드 관리",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "햄버거 아이콘을 드래그하여 카드 순서를 변경하거나 토글 버튼으로 카드를 숨길 수 있습니다.",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                state = state.listState,
                modifier = Modifier
                    .weight(1f)
                    .reorderable(state),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = cards,
                    key = { it.card.id }
                ) { cardItem ->
                    ReorderableItem(
                        reorderableState = state,
                        key = cardItem.card.id
                    ) { isDragging ->
                        val elevation by animateFloatAsState(
                            targetValue = if (isDragging && cardItem.isVisible) 16f else 1f,
                            label = "elevation"
                        )
                        
                        val scale by animateFloatAsState(
                            targetValue = if (isDragging && cardItem.isVisible) 1.05f else 1f,
                            animationSpec = tween(150, easing = EaseInOutQuad),
                            label = "scale"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(72.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.shadowElevation = elevation
                                    alpha = if (cardItem.isVisible) 1f else 0.6f
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2A2A40)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "드래그하여 순서 변경",
                                        tint = if (cardItem.isVisible) Color.Gray else Color(0xFF4A4A4A),
                                        modifier = if (cardItem.isVisible) {
                                            Modifier.detectReorder(state)
                                        } else {
                                            Modifier
                                        }
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = cardItem.card.name,
                                        color = if (cardItem.isVisible) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = cardItem.card.cardNumber,
                                        color = if (cardItem.isVisible) Color.LightGray else Color(0xFF4A4A4A),
                                        fontSize = 12.sp
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val newCards = cards.toMutableList()
                                        val index = newCards.indexOf(cardItem)
                                        newCards[index] = cardItem.copy(isVisible = !cardItem.isVisible)
                                        // 카드 숨김 상태가 변경될 때마다 정렬
                                        cards = sortCards(newCards)
                                        hasChanges = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (cardItem.isVisible) Icons.Default.Check else Icons.Default.Clear,
                                        contentDescription = if (cardItem.isVisible) "숨기기" else "표시하기",
                                        tint = if (cardItem.isVisible) Color(0xFF64B5F6) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (hasChanges) {
                        CardOrderManager.updateCards(cards)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                )
            ) {
                Text(
                    text = "저장",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
} 