package com.example.fe.ui.screens.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.GlassSurface
import com.example.fe.ui.components.backgrounds.StarryBackground
import com.example.fe.ui.components.cards.HorizontalCardLayout
import com.example.fe.ui.screens.payment.PaymentCardInfo
import com.example.fe.ui.screens.payment.PaymentViewModel

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

    // 결제 확인 팝업 표시 여부
    var showPaymentConfirmDialog by remember { mutableStateOf(false) }

    // 결제 상태 관찰
    val paymentState by viewModel.paymentState.collectAsState()

    // 추천 카드가 있는지 확인하고 카드 목록 정렬
    val allCards = remember(cards) {
        val hasRecommendCard = cards.any { it.cardName == "추천카드" }
        if (hasRecommendCard) {
            // 추천 카드를 첫 번째로 정렬
            cards.sortedBy { it.cardName != "추천카드" }
        } else {
            cards
        }
    }

    // 결제 상태에 따른 처리
    LaunchedEffect(paymentState) {
        when (paymentState) {
            is PaymentViewModel.PaymentState.Completed -> {
                // 결제 완료 시 콜백 호출
                onPaymentComplete()
            }
            else -> {}
        }
    }


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
        // 상단 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White
                )
            }
        }

        // 메인 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 70.dp, bottom = 16.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // 가맹점 이름
            Text(
                text = paymentInfo?.merchantName ?: "태인생일",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // 결제 금액
            Text(
                text = "${paymentInfo?.amount?.let { "%,d".format(it) } ?: "981006"}원",
                color = Color(0xFF00CCFF), // 이미지의 청록색
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 카드 선택 영역 (GlassSurface)
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                cornerRadius = 16f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 페이지 인디케이터
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(allCards.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(8.dp)
                                    .background(
                                        color = if (index == selectedCardIndex) Color.White else Color.Gray,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 카드 슬라이더
                    LazyRow(
                        state = lazyListState,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(allCards.size) { index ->
                            val card = allCards[index]
                            PaymentCardItem(
                                card = card,
                                isSelected = index == selectedCardIndex,
                                isRecommended = card.cardName == "추천카드",
                                onClick = {
                                    selectedCardIndex = index
                                    selectedCard = card
                                    // 카드 클릭 시 결제 확인 팝업 표시
                                    showPaymentConfirmDialog = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                }
            }
        }
    }

    // 결제 확인 팝업
    if (showPaymentConfirmDialog && selectedCard != null) {
        AlertDialog(
            onDismissRequest = { showPaymentConfirmDialog = false },
            title = {
                Text(
                    text = "결제 확인",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "다음 카드로 결제를 진행하시겠습니까?",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedCard?.cardName == "추천카드") "리버스 추천 카드" else selectedCard?.cardName ?: "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "결제 금액: ${paymentInfo?.amount?.let { "%,d".format(it) } ?: "5,500"}원",
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentConfirmDialog = false
                        // 선택된 카드의 토큰으로 결제 완료 요청
                        selectedCard?.token?.let { token ->
                            viewModel.completePayment(token)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00CCFF)
                    )
                ) {
                    Text("결제하기")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPaymentConfirmDialog = false }
                ) {
                    Text("취소")
                }
            },
            containerColor = Color(0xFF2D2A57),
            textContentColor = Color.White,
            titleContentColor = Color.White
        )
    }
}

@Composable
fun PaymentCardItem(
    card: PaymentCardInfo,
    isSelected: Boolean,
    isRecommended: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 카드 이미지 - HorizontalCardLayout 사용
        HorizontalCardLayout(
            cardName = card.cardName,
            cardImageUrl = card.cardImageUrl,
            cardImage = R.drawable.card,
            isSelected = isSelected,
            isRecommended = isRecommended,
            width = 280.dp,
            height = 180.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 카드 이름
        Text(
            text = if (isRecommended) "리버스 추천 카드 결제" else card.cardName,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 구분선
        Divider(
            modifier = Modifier
                .width(240.dp)
                .padding(vertical = 8.dp),
            color = Color.White.copy(alpha = 0.3f)
        )

        // 결제 방식 텍스트
        Text(
            text = if (isRecommended) "가장 혜택이 좋은 카드로 자동 결제됩니다" else "일시불",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}