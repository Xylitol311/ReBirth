package com.example.fe.ui.screens.payment.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.ui.components.zodiac.DynamicZodiacView
import com.example.fe.ui.screens.payment.PaymentCardInfo

@Composable
fun PaymentInfoScreen(
    cards: List<PaymentCardInfo>,
    onClose: () -> Unit,
    onPaymentComplete: () -> Unit,
    onScrollOffsetChange: (Float) -> Unit = {}
) {
    // 스크롤 상태
    val lazyListState = rememberLazyListState()
    
    // 스크롤 오프셋 변경 감지 및 콜백 호출
    LaunchedEffect(lazyListState) {
        snapshotFlow { 
            lazyListState.firstVisibleItemIndex * 1000f + lazyListState.firstVisibleItemScrollOffset 
        }.collect { offset ->
            onScrollOffsetChange(offset)
        }
    }
    
    // 선택된 카드
    var selectedCardIndex by remember { mutableStateOf(0) }
    var selectedCard by remember { mutableStateOf(cards.firstOrNull()) }
    
    // 더미 결제 정보
    val merchantName = "스타벅스 강남점"
    val paymentAmount = "5,800원"
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 상단 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF2D2A57))
                .align(Alignment.TopCenter)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로 가기",
                    tint = Color.White
                )
            }
            
            Text(
                text = "결제 정보",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, bottom = 16.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // 별자리 표시
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF2D2A57).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                // 별자리 뷰
                DynamicZodiacView(
                    cardId = selectedCard?.cardName ?: "card1",
                    useJSON = false,
                    useBackend = false,
                    modifier = Modifier.size(180.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 가맹점 정보
            Text(
                text = merchantName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 결제 금액
            Text(
                text = paymentAmount,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 카드 선택 섹션
            Text(
                text = "결제 카드 선택",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 카드 가로 스크롤
            LazyRow(
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(cards) { card ->
                    PaymentCardItem(
                        card = card,
                        isSelected = card == selectedCard,
                        onClick = {
                            selectedCard = card
                            selectedCardIndex = cards.indexOf(card)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 결제하기 버튼
            Button(
                onClick = onPaymentComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "결제하기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PaymentCardItem(
    card: PaymentCardInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2196F3) else Color(0xFF424242)
        ),
        border = if (isSelected) BorderStroke(2.dp, Color.White) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = card.cardName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 카드 로고 또는 아이콘 (예시)
            Image(
                painter = painterResource(id = R.drawable.card),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
} 