package com.example.fe.ui.screens.payment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.ui.components.zodiac.DynamicZodiacView
import com.example.fe.ui.screens.payment.components.PaymentBarcodeQRSection
import com.example.fe.ui.screens.payment.components.PaymentCardScroll
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.fe.ui.screens.payment.components.ConstellationCarousel
import com.example.fe.ui.screens.payment.components.PaymentAddCardSection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fe.ui.screens.payment.PaymentViewModel.PaymentState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import android.util.Log
import android.net.ConnectivityManager
import android.content.Context
import com.example.fe.ui.screens.payment.components.QRScannerScreen

// 카드 정보 데이터 클래스
data class PaymentCardInfo(
    val id: String,
    val cardNumber: String,
    val cardName: String,
    val cardImage: Int = R.drawable.card
)

@Composable
fun PaymentScreen(
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {},
    viewModel: PaymentViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {}
) {
    // QR 스캐너 표시 여부
    var showQRScanner by remember { mutableStateOf(false) }
    
    // 스크롤 오프셋 추적 (우주 배경 효과용)
    var scrollOffset by remember { mutableFloatStateOf(0f) }
    val lazyListState = rememberLazyListState()
    
    // 스크롤 오프셋 변경 감지 및 콜백 호출
    LaunchedEffect(lazyListState) {
        snapshotFlow { 
            lazyListState.firstVisibleItemIndex * 1000f + lazyListState.firstVisibleItemScrollOffset 
        }.collect { offset ->
            scrollOffset = offset
            onScrollOffsetChange(offset)
        }
    }
    
    // 카드 목록
    val cards = remember {
        listOf(
            PaymentCardInfo(
                id = "000",
                cardNumber = "8801062318551",
                cardName = "토스 신한카드 Mr.Life"
            ),
            PaymentCardInfo(
                id = "f662c4d2-6ec2-473c-9c07-de23530211ea",
                cardNumber = "8801062318552",
                cardName = "삼성카드 taptap O"
            ),
            PaymentCardInfo(
                id = "card3",
                cardNumber = "8801062318553",
                cardName = "현대카드 ZERO"
            ),
            PaymentCardInfo(
                id = "card4",
                cardNumber = "8801062318554",
                cardName = "KB국민 톡톡"
            ),
            PaymentCardInfo(
                id = "card5",
                cardNumber = "8801062318555",
                cardName = "하나 원큐"
            )
        )
    }
    
    // 코루틴 스코프 추가
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // 현재 선택된 카드와 인덱스
    var selectedCardIndex by remember { mutableStateOf(0) }
    var selectedCard by remember { mutableStateOf(cards.firstOrNull()) }
    
    // 카드 추가 모드인지 여부 (마지막 인덱스가 선택되었을 때)
    var isAddCardMode = selectedCardIndex >= cards.size
    
    // 바코드/QR 코드 갱신 트리거
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // 타이머 상태
    var remainingTime by remember { mutableStateOf(60) }
    
    // 네트워크 연결 상태 확인
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    val isConnected = networkCapabilities != null

    Log.d("PaymentScreen", "네트워크 연결 상태: $isConnected")
    
    // 화면 진입 시 모든 카드의 토큰 요청
    LaunchedEffect(Unit) {
        viewModel.initializePaymentProcess()
    }
    
    // 타이머 효과 추가
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            if (!isAddCardMode && remainingTime > 0) {
                remainingTime--
            } else if (!isAddCardMode) {
                refreshTrigger++
                viewModel.refreshTokens()
                remainingTime = 60
            }
        }
    }
    
    // 카드 변경 시 타이머 리셋 및 코드 갱신
    LaunchedEffect(selectedCard) {
        if (selectedCard != null && !isAddCardMode) {
            viewModel.selectCard(selectedCard!!.id)
            remainingTime = 60
            refreshTrigger++
        }
    }
    
    // 화면 이탈 시 결제 프로세스 종료
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPaymentProcess()
        }
    }
    
    // 화면 너비 계산
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val cardWidth = 280.dp
    val horizontalPadding = (screenWidth - cardWidth) / 2

    // 결제 상태 관찰
    val paymentState by viewModel.paymentState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. 상단 영역 (별자리 또는 캐러셀)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.25f)
            ) {
                if (!isAddCardMode) {
                    // 일반 카드 모드 - 카드별 별자리 표시
                    DynamicZodiacView(
                        cardId = selectedCard!!.id,
                        modifier = Modifier.fillMaxSize(),
                        useJSON = true
                    )
                    
                    // 카드 이름
                    Text(
                        text = selectedCard!!.cardName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                } else {
                    // 카드 추가 모드 - 랜덤 별자리 캐러셀
                    ConstellationCarousel(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // 2. 중간 영역 (바코드/QR 또는 안내 메시지)
            if (!isAddCardMode) {
                // 일반 카드 모드 - 바코드 및 QR 코드
                if (selectedCard != null) {
                    // 선택된 카드의 토큰 가져오기
                    val cardToken = viewModel.getTokenForCard(selectedCard!!.id)
//                    Log.e("PaymentScreen", "Card: ${selectedCard!!.id}, Token: $cardToken")
                    
                    PaymentBarcodeQRSection(
                        remainingTime = remainingTime,
                        refreshTrigger = refreshTrigger,
                        onRefresh = {
                            refreshTrigger++
                            viewModel.refreshTokens()
                            remainingTime = 60
                        },
                        paymentToken = cardToken,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                // 카드 추가 모드 - PaymentAddCardSection의 안내 메시지 활용
                PaymentAddCardSection(
                    onAddCardClick = { /* 카드 추가 로직 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            
            // 3. 카드 슬라이더 영역
            PaymentCardScroll(
                cards = cards,
                cardWidth = cardWidth,
                horizontalPadding = horizontalPadding,
                lazyListState = lazyListState,
                onCardIndexSelected = { index ->
                    try {
                        // 인덱스가 범위 내에 있는지 확인
                        if (index >= 0 && index < cards.size) {
                            selectedCard = cards[index]
                            selectedCardIndex = index
                            isAddCardMode = false
                        } else if (index == cards.size) {
                            // 카드 추가 모드
                            selectedCardIndex = index
                            isAddCardMode = true
                            selectedCard = null
                        } else {
                            // 범위를 벗어난 인덱스는 무시
                            Log.w("PaymentScreen", "Invalid card index: $index")
                        }
                    } catch (e: Exception) {
                        Log.e("PaymentScreen", "Error selecting card", e)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 결제 상태에 따른 오버레이 UI
        when (paymentState) {
            is PaymentState.Processing -> {
                // 결제 처리 중 오버레이
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 로딩 애니메이션
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(60.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "결제 처리 중...",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            is PaymentState.Completed -> {
                // 결제 완료 오버레이
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(300.dp)
                            .background(
                                color = Color(0xFF2D2A57),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check_circle),
                            contentDescription = "결제 완료",
                            tint = Color.Green,
                            modifier = Modifier.size(60.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "결제가 완료되었습니다",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onNavigateToHome,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF2D2A57)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("확인")
                        }
                    }
                }
                
                // 3초 후 자동으로 홈 화면으로 이동
                LaunchedEffect(Unit) {
                    delay(3000)
                    onNavigateToHome()
                }
            }
            
            is PaymentState.Failed -> {
                // 결제 실패 오버레이
                val failedState = paymentState as PaymentState.Failed
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(300.dp)
                            .background(
                                color = Color(0xFF2D2A57),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_error),
                            contentDescription = "결제 실패",
                            tint = Color.Red,
                            modifier = Modifier.size(60.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "결제 실패",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = failedState.reason,
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                // 결제 상태 초기화
                                viewModel.stopPaymentProcess()
                                // 토큰 갱신 및 현재 카드 다시 선택
                                viewModel.refreshTokens()
                                if (selectedCard != null) {
                                    viewModel.selectCard(selectedCard!!.id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF2D2A57)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("다시 시도")
                        }
                    }
                }
            }
            
            is PaymentState.Expired -> {
                // 토큰 만료 오버레이
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(300.dp)
                            .background(
                                color = Color(0xFF2D2A57),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_timeout),
                            contentDescription = "토큰 만료",
                            tint = Color.Yellow,
                            modifier = Modifier.size(60.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "결제 시간 만료",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "결제 시간이 만료되었습니다. 다시 시도해주세요.",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                // 결제 상태 초기화
                                viewModel.stopPaymentProcess()
                                // 토큰 갱신 및 현재 카드 다시 선택
                                viewModel.refreshTokens()
                                if (selectedCard != null) {
                                    viewModel.selectCard(selectedCard!!.id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF2D2A57)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("다시 시도")
                        }
                    }
                }
            }
            
            else -> {
                // 다른 상태는 오버레이 표시 없음
            }
        }
    }
}
