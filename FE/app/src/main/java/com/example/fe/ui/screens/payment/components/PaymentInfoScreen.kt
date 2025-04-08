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
import androidx.compose.material.icons.filled.Close
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.screens.payment.PaymentCardInfo
import com.example.fe.ui.screens.payment.PaymentViewModel
import coil.compose.AsyncImage

@Composable
fun PaymentInfoScreen(
    cards: List<PaymentCardInfo>,
    onClose: () -> Unit,
    onPaymentComplete: () -> Unit,
    onScrollOffsetChange: (Float) -> Unit = {},
    viewModel: PaymentViewModel = viewModel()
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
    
    // 결제 정보
    val paymentInfo by viewModel.paymentInfo.collectAsState()
    
    // 선택된 카드
    var selectedCardIndex by remember { mutableStateOf(0) }
    var selectedCard by remember { mutableStateOf(cards.firstOrNull()) }
    
    // 별자리 배경
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 별자리 배경
        StarryBackground(
            scrollOffset = 0f,
            modifier = Modifier.fillMaxSize(),
            horizontalOffset = 0f,
            starCount = 100
        ) {
            // 여기에 내용을 추가할 수 있습니다.
            // 비워두면 별만 표시됩니다.
        }
        
        // 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 16.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 바
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로 가기",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "결제 정보",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 결제 정보 카드
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D2A57)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 가맹점 이름
                    Text(
                        text = paymentInfo?.merchantName ?: "가맹점",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 결제 금액
                    Text(
                        text = "${paymentInfo?.amount?.let { "%,d".format(it) } ?: "0"}원",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 부가세 포함 텍스트
                    Text(
                        text = "부가세 100원 포함",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            
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
                        isRecommended = card.cardName.contains("추천", ignoreCase = true),
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
                onClick = {
                    // 선택된 카드의 토큰 가져오기
                    val selectedCard = cards.getOrNull(selectedCardIndex)
                    if (selectedCard != null && selectedCard.token.isNotEmpty()) {
                        // 결제 완료 요청
                        viewModel.completePayment(selectedCard.token)
                        // 결제 완료 후 콜백 호출
                        onPaymentComplete()
                    } else {
                        // 토큰이 없는 경우 오류 처리
                        // 여기에 오류 메시지 표시 로직 추가
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCard?.cardName?.contains("추천", ignoreCase = true) == true) 
                        Color(0xFFFFC107) else Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (selectedCard?.cardName?.contains("추천", ignoreCase = true) == true) 
                        "추천 카드로 결제하기" else "결제하기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedCard?.cardName?.contains("추천", ignoreCase = true) == true) 
                        Color.Black else Color.White
                )
            }
        }
    }
}

@Composable
fun PaymentCardItem(
    card: PaymentCardInfo,
    isSelected: Boolean,
    isRecommended: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecommended) Color(0xFFFFC107) else 
                            if (isSelected) Color(0xFF2196F3) else Color(0xFF424242)
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
                    color = if (isRecommended) Color.Black else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isRecommended) {
                    Text(
                        text = "30% 소득공제 혜택",
                        color = Color.Black.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 카드 로고 또는 아이콘
            if (card.cardImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = card.cardImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.BottomEnd),
                    error = painterResource(id = R.drawable.card)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.card),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.BottomEnd)
                )
            }
            
            // 추천 카드 표시
            if (isRecommended) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "추천",
                        color = Color.Yellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
} 