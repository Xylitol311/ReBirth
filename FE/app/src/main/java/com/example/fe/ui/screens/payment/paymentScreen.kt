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
import com.example.fe.ui.screens.payment.components.PaymentAutoSection
import androidx.compose.material3.IconButton
import com.example.fe.ui.screens.payment.components.PaymentAddCardSection

// 카드 정보 데이터 클래스
data class PaymentCardInfo(
    val cardName: String,
    val cardImageUrl: String = "",
    val constellationInfo: String = "{}",
    val cardImage: Int = R.drawable.card, // 기본 이미지 (URL 로딩 실패 시 사용)
    val token: String = "" // 토큰 정보 추가
)

@Composable
fun PaymentScreen(
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {},
    viewModel: PaymentViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onShowQRScanner: () -> Unit = {}
) {

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
    
    // API에서 가져온 카드 목록
    val apiCards by viewModel.cards.collectAsState()
    
    // 현재 선택된 카드와 인덱스
    var selectedCardIndex by remember { mutableStateOf(-1) } // 초기값은 자동 카드(-1)
    var selectedCard by remember { mutableStateOf<PaymentCardInfo?>(null) }
    
    // 자동 카드 추천 모드인지 여부
    var showAutoCardMode by remember { mutableStateOf(true) } // 초기값은 true
    
    // 카드 추가 모드인지 여부
    var showAddCardMode by remember { mutableStateOf(false) }
    
    // 바코드/QR 코드 갱신 트리거
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // 타이머 상태
    var remainingTime by remember { mutableStateOf(60) }
    var isTimerActive by remember { mutableStateOf(false) }
    var timerResetTrigger by remember { mutableStateOf(0) }
    
    // 이전 상태 저장 변수 추가
    var previousCardIndex by remember { mutableStateOf(-1) }
    var previousAutoCardMode by remember { mutableStateOf(true) }
    
    // 화면 진입 시 초기화
    LaunchedEffect(Unit) {
        Log.d("PaymentScreen", "화면 초기화 시작")
        viewModel.initializePaymentProcess()
        
        // 카드 추가 화면으로 스크롤 (첫 번째 항목)
        lazyListState.scrollToItem(0)
        
        // 상태 일관성 유지
        selectedCardIndex = -1
        showAutoCardMode = true
        showAddCardMode = false
        selectedCard = null  // 명시적으로 null 설정
        
    }
    
    // 타이머 활성화 여부
    LaunchedEffect(selectedCard, showAutoCardMode, isTimerActive, timerResetTrigger) {
        // 카드가 선택되었거나 자동 카드 모드이고 타이머가 활성화된 경우에 타이머 작동
        if ((selectedCard != null || showAutoCardMode) && isTimerActive) {
            remainingTime = 60 // 타이머 초기화
            while (remainingTime > 0) {
                delay(1000) // 1초 대기
                remainingTime-- // 시간 감소
            }
            
            // 타이머가 0이 되면 토큰 갱신
            if (remainingTime <= 0) {
                Log.d("PaymentScreen", "타이머 종료: 토큰 갱신")
                refreshTrigger++
                viewModel.refreshTokens()
                // 타이머 다시 시작하기 위한 트리거 증가
                timerResetTrigger++
            }
        }
    }
    
    // 카드 선택 시 타이머 활성화
    LaunchedEffect(selectedCard, showAutoCardMode) {
        if (selectedCard != null || showAutoCardMode) {
            isTimerActive = true
        } else {
            isTimerActive = false
        }
    }
    
    // 화면 이탈 시 결제 프로세스 종료
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPaymentProcess()
            isTimerActive = false
            Log.d("PaymentScreen", "화면 이탈: 타이머 정리")
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
                if (!showAutoCardMode && selectedCard != null) {
                    // 일반 카드 모드 - 카드별 별자리 표시
                    DynamicZodiacView(
                        cardId = selectedCard!!.cardName,
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
                } else if (selectedCardIndex == -2) { // 카드 추가 모드
                    // 카드 추가 안내 별자리 표시
                    ConstellationCarousel(
                        modifier = Modifier.fillMaxSize()
                    )
                    
                } else {
                    // 자동 카드 추천 모드 - 랜덤 별자리 캐러셀
                    ConstellationCarousel(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // 2. 중간 영역 (바코드/QR 또는 안내 메시지)
            if (selectedCardIndex == -2 || (selectedCardIndex >= apiCards.size && selectedCardIndex <= apiCards.size + 1)) {
                // 카드 추가 안내 문구
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "당신만의 별자리를 추가해보세요",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "혜택을 챙겨드릴 카드를 등록해주세요",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                // 일반 모드 - 바코드/QR 표시
                // 자동 카드 추천 모드
                if (showAutoCardMode) {
                    val autoToken = viewModel.getAutoCardToken()
                    
                    // autoToken이 null이 아닐 때만 PaymentAutoSection 표시
                    if (autoToken != null) {
                        PaymentAutoSection(
                            remainingTime = remainingTime,
                            refreshTrigger = refreshTrigger,
                            onRefresh = { 
                                refreshTrigger++
                                viewModel.refreshTokens()
                                remainingTime = 60
                                isTimerActive = true
                                Log.d("PaymentScreen", "자동 결제 토큰 새로고침: refreshTrigger=$refreshTrigger")
                            },
                            paymentToken = autoToken,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    } else {
                        // 토큰이 없을 경우 대체 UI 표시
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "카드 정보를 불러오는 중...",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else if (selectedCard != null) {
                    // 선택된 카드가 있는 경우
                    PaymentBarcodeQRSection(
                        remainingTime = remainingTime,
                        refreshTrigger = refreshTrigger,
                        onRefresh = {
                            refreshTrigger++
                            viewModel.refreshTokens()
                            remainingTime = 60
                            isTimerActive = true
                            Log.d("PaymentScreen", "결제 토큰 새로고침: refreshTrigger=$refreshTrigger")
                        },
                        paymentToken = viewModel.getTokenForCard(selectedCard!!.cardName),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
            
            // 카드 선택 처리 함수
            val onCardSelected = { index: Int ->
                try {
                    when (index) {
                        -1 -> { // 자동 카드
                            selectedCardIndex = -1
                            showAutoCardMode = true
                            showAddCardMode = false
                            selectedCard = null
                        }
                        -2 -> { // 카드 추가 (스크롤만으로는 화면 전환 안함)
                            selectedCardIndex = -2
                            showAutoCardMode = false
                            showAddCardMode = false
                            selectedCard = null
                        }
                        else -> { // 실제 카드
                            // 카드 추가 버튼의 인덱스가 cards.size와 같거나 1 크면 카드 추가로 처리
                            if (index >= apiCards.size && index <= apiCards.size + 1) {
                                // 카드 추가 버튼으로 처리
                                selectedCardIndex = -2
                                showAutoCardMode = false
                                showAddCardMode = false
                                selectedCard = null
                                Log.d("PaymentScreen", "카드 추가 버튼 선택: index=$index")
                            } else if (index >= 0 && index < apiCards.size) {
                                // 실제 카드 선택
                                selectedCardIndex = index
                                showAutoCardMode = false
                                showAddCardMode = false
                                selectedCard = apiCards[index]
                                
                                // null 체크 추가
                                if (selectedCard != null) {
                                    viewModel.selectCard(selectedCard!!.cardName)
                                    val cardToken = viewModel.getTokenForCard(selectedCard!!.cardName)
                                    remainingTime = 60
                                    isTimerActive = true
                                    refreshTrigger++
                                } else {
                                    Log.e("PaymentScreen", "선택된 카드가 null입니다: index=$index")
                                }
                            } else {
                                // 범위를 벗어난 인덱스는 로그만 남기고 상태 변경 안함
                                Log.e("PaymentScreen", "인덱스 범위 오류: $index, 카드 수: ${apiCards.size}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PaymentScreen", "카드 선택 중 오류 발생", e)
                }
            }

            // 카드 추가 버튼 클릭 핸들러
            val onAddCardButtonClick = {
                // 현재 상태 저장
                previousCardIndex = selectedCardIndex
                previousAutoCardMode = showAutoCardMode
                // 카드 추가 모드 활성화
                showAddCardMode = true
            }

            // 카드 슬라이더 영역
            PaymentCardScroll(
                cards = apiCards,
                cardWidth = cardWidth,
                horizontalPadding = horizontalPadding,
                lazyListState = lazyListState,
                onCardIndexSelected = { index -> onCardSelected(index) },
                onAddCardButtonClick = onAddCardButtonClick, // 카드 추가 버튼 클릭 핸들러 전달
                modifier = Modifier.fillMaxWidth()
            )

            // 카메라 아이콘 클릭 시 QR 스캐너 표시
            IconButton(
                onClick = onShowQRScanner,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "QR 스캔",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
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
                                    viewModel.selectCard(selectedCard!!.cardName)
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
                                    viewModel.selectCard(selectedCard!!.cardName)
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

    // 카드 추가 모드 오버레이
    if (showAddCardMode) {
        PaymentAddCardSection(
            onClose = { 
                // 카드 추가 모드 종료 시 이전 상태로 돌아감
                showAddCardMode = false
                
                // 이전에 카드 추가 영역이었다면 그대로 유지
                if (previousCardIndex == -2 || 
                    (previousCardIndex >= apiCards.size && previousCardIndex <= apiCards.size + 1)) {
                    selectedCardIndex = -2
                    showAutoCardMode = false
                    selectedCard = null
                } else {
                    // 그 외의 경우 이전 상태로 복원
                    selectedCardIndex = previousCardIndex
                    showAutoCardMode = previousAutoCardMode
                }
            },
            onAddCardComplete = {
                // 카드 추가 완료 후 자동 카드 모드로 돌아감
                selectedCardIndex = -1
                showAutoCardMode = true
                showAddCardMode = false
                // 카드 목록 새로고침
                viewModel.refreshTokens()
            },
            viewModel = viewModel
        )
    }
    
}
