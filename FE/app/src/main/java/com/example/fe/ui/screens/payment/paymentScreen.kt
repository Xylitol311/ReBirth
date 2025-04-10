package com.example.fe.ui.screens.payment

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.GlassSurface
import com.example.fe.ui.components.zodiac.DynamicZodiacView
import com.example.fe.ui.screens.payment.components.ConstellationCarousel
import com.example.fe.ui.screens.payment.components.PaymentAddCardSection
import com.example.fe.ui.screens.payment.components.PaymentCardScroll
import com.example.fe.ui.screens.payment.components.PaymentCodeContainer
import com.example.fe.ui.screens.payment.components.PaymentProcessing
import com.example.fe.ui.screens.payment.components.PaymentResultScreen
import com.example.fe.ui.screens.payment.components.QRScannerScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 화면 상태를 열거형으로 관리
enum class PaymentScreenState {
    CARD_SELECTION,  // 카드 선택 화면
    PROCESSING,      // 결제 진행 중 화면
    RESULT           // 결제 결과 화면
}

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
    onShowQRScanner: () -> Unit = {},
    onShowCardOCRScan: () -> Unit = {},
    onQRScanModeChange: (Boolean) -> Unit = {},
    onShowPaymentInfo: () -> Unit = {} // 결제 정보 화면 표시 콜백 추가
) {

    val coroutineScope = rememberCoroutineScope()

    // 현재 화면 상태
    var screenState by remember { mutableStateOf(PaymentScreenState.CARD_SELECTION) }

    var showResult by remember { mutableStateOf(false) }
    // 결제 상태 관찰 부분 수정
    val paymentState by viewModel.paymentState.collectAsState()

    // 카드 등록 결과 상태 관찰
    val cardRegistrationState by viewModel.cardRegistrationState.collectAsState()
    // 결과 팝업 표시 여부
    var showResultPopup by remember { mutableStateOf(false) }

    // 스크롤 오프셋 추적 (우주 배경 효과용)
    var scrollOffset by remember { mutableFloatStateOf(0f) }
    val lazyListState = rememberLazyListState()

    // 카드 등록 결과 변경 감지 및 팝업 표시
    LaunchedEffect(cardRegistrationState) {
        if (cardRegistrationState is PaymentViewModel.CardRegistrationState.Success ||
            cardRegistrationState is PaymentViewModel.CardRegistrationState.Error) {
            // 초기 상태가 아닌 경우에만 팝업 표시
            if (cardRegistrationState !is PaymentViewModel.CardRegistrationState.Initial) {
                showResultPopup = true
                // 5초 후 팝업 자동 닫기
                delay(5000)
                showResultPopup = false
                // 상태 초기화
                viewModel.resetCardRegistrationState()
            }
        }
    }

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

    // 화면 너비 계산
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val cardWidth = 280.dp
    val horizontalPadding = (screenWidth - cardWidth) / 2


    // 바코드/QR 탭 선택 상태 (true: 바코드, false: QR)
    var isBarcodeSelected by remember { mutableStateOf(true) }
    // 내부 QR 스캐너 표시 여부 상태 추가
    var showInternalQRScanner by remember { mutableStateOf(false) }

    val paymentResult by viewModel.paymentResult.collectAsState()

// 결제 상태 변경 감지 및 화면 상태 업데이트
    LaunchedEffect(paymentState) {
        Log.d("PaymentScreen", "결제 상태 변경: $paymentState")

        when (paymentState) {
            is PaymentViewModel.PaymentState.Processing -> {
                screenState = PaymentScreenState.PROCESSING
                Log.d("PaymentScreen", "화면 상태 변경: PROCESSING")
            }
            is PaymentViewModel.PaymentState.Completed,
            is PaymentViewModel.PaymentState.Failed -> {
                screenState = PaymentScreenState.RESULT
                Log.d("PaymentScreen", "화면 상태 변경: RESULT")
            }
            else -> {
                // 결과 화면이 아니라면 카드 선택 화면으로
                if (screenState != PaymentScreenState.RESULT) {
                    screenState = PaymentScreenState.CARD_SELECTION
                }
            }
        }
    }

    // 탭 선택 시 내부 QR 스캐너 표시 여부 업데이트
    LaunchedEffect(isBarcodeSelected) {
        showInternalQRScanner = !isBarcodeSelected
        onQRScanModeChange(showInternalQRScanner) // 상태 변경 시 콜백 호출
    }

    // 화면 이탈 시 QR 스캔 모드 해제
    DisposableEffect(Unit) {
        onDispose {
            onQRScanModeChange(false) // QR 스캔 모드 해제
            viewModel.stopPaymentProcess()
            isTimerActive = false
            Log.d("PaymentScreen", "화면 이탈: 타이머 정리")
        }
    }

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

    @SuppressLint("UnusedBoxWithConstraintsScope")
    @Composable
    fun AnimatedTabRow(
        isBarcodeSelected: Boolean,
        onTabSelected: (Boolean) -> Unit
    ) {
        // 바코드/QR 탭 선택 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxHeight(0.15f)
                    .fillMaxWidth(0.68f)
                    .height(48.dp),
                cornerRadius = 12f
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 여기서 maxWidth는 GlassSurface의 실제 너비입니다
                    val containerWidth = this.maxWidth

                    // 애니메이션 적용된 선택 인디케이터
                    val transition = updateTransition(
                        targetState = isBarcodeSelected,
                        label = "TabIndicator"
                    )

                    val indicatorOffset by transition.animateFloat(
                        transitionSpec = {
                            tween(durationMillis = 300, easing = FastOutSlowInEasing)
                        },
                        label = "IndicatorOffset"
                    ) { selected ->
                        if (selected) 0f else 1f
                    }

                    // 흰색 배경 인디케이터
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(containerWidth / 2)
                            .offset(x = containerWidth * indicatorOffset / 2)
                            .background(Color.White, RoundedCornerShape(12.dp))
                    )

                    // 탭 버튼들
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 바코드 탭
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onTabSelected(true) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "바코드",
                                color = if (isBarcodeSelected) Color.Black else Color.White,
                                fontWeight = if (isBarcodeSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        // QR 탭
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onTabSelected(false) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "QR스캔",
                                color = if (!isBarcodeSelected) Color.Black else Color.White,
                                fontWeight = if (!isBarcodeSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if(!showInternalQRScanner){
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. 상단 영역 (바코드/QR 코드)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.37f)
                ) {
                    if (selectedCardIndex == -2 || (selectedCardIndex >= apiCards.size && selectedCardIndex <= apiCards.size + 1)) {
                        // 카드 추가 안내 문구
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = "결제 카드 등록",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = "다른 별자리를 등록해 보세요",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AnimatedTabRow(
                                isBarcodeSelected = isBarcodeSelected,
                                onTabSelected = { selected -> isBarcodeSelected = selected }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 바코드 또는 QR 코드 표시
                            if (showAutoCardMode) {
                                val autoToken = viewModel.getAutoCardToken()

                                if (autoToken != null) {
                                    // PaymentCodeContainer 사용
                                    PaymentCodeContainer(
                                        isBarcodeSelected = isBarcodeSelected,
                                        barcodeData = autoToken,
                                        qrData = autoToken,
                                        refreshTrigger = refreshTrigger,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        // 왼쪽 여백 (균형을 위한 빈 공간)
                                        Spacer(modifier = Modifier.weight(1f))

                                        // 중앙: 카드 이름
                                        Text(
                                            text = selectedCard?.cardName?:"REBIRTH 추천 카드",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(2f)
                                        )

                                        // 오른쪽: 타이머와 새로고침 버튼
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // 타이머 표시
                                            Text(
                                                text = "${remainingTime}초",
                                                color = if (remainingTime <= 10) Color.Red else Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // 새로고침 버튼
                                            IconButton(
                                                onClick = {
                                                    refreshTrigger++
                                                    viewModel.refreshTokens()
                                                    remainingTime = 60
                                                    isTimerActive = true
                                                },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_refresh),
                                                    contentDescription = "새로고침",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.card),
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(48.dp)
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Text(
                                                text = "카드가 없습니다",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = "새로운 결제 카드를 등록해주세요",
                                                fontSize = 16.sp,
                                                color = Color.White.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = {
                                                    // 카드 슬라이더를 오른쪽 끝(카드 추가 슬라이더)으로 이동
                                                    // apiCards.size는 실제 카드의 개수이고, 그 다음 인덱스가 카드 추가 슬라이더의 위치
                                                    coroutineScope.launch {
                                                        // 코루틴 내에서 애니메이션 실행
                                                        lazyListState.animateScrollToItem(apiCards.size + 1)
                                                    }

                                                    // 카드 추가 모드로 상태 변경
                                                    selectedCardIndex = -2
                                                    showAutoCardMode = false
                                                    showAddCardMode = false
                                                    selectedCard = null
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF00BCD4)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("카드 등록하기")
                                            }
                                        }
                                    }
                                }
                            } else if (selectedCard != null) {
                                val cardToken = viewModel.getTokenForCard(selectedCard!!.cardName)

                                if (cardToken != null) {
                                    // PaymentCodeContainer 사용
                                    PaymentCodeContainer(
                                        isBarcodeSelected = isBarcodeSelected,
                                        barcodeData = cardToken,
                                        qrData = cardToken,
                                        refreshTrigger = refreshTrigger,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        // 왼쪽 여백 (균형을 위한 빈 공간)
                                        Spacer(modifier = Modifier.weight(1f))

                                        // 중앙: 카드 이름
                                        Text(
                                            text = selectedCard!!.cardName,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(2f)
                                        )

                                        // 오른쪽: 타이머와 새로고침 버튼
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // 타이머 표시
                                            Text(
                                                text = "${remainingTime}초",
                                                color = if (remainingTime <= 10) Color.Red else Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // 새로고침 버튼
                                            IconButton(
                                                onClick = {
                                                    refreshTrigger++
                                                    viewModel.refreshTokens()
                                                    remainingTime = 60
                                                    isTimerActive = true
                                                },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_refresh),
                                                    contentDescription = "새로고침",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.card),
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(48.dp)
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Text(
                                                text = "카드가 없습니다",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = "새로운 결제 카드를 등록해주세요",
                                                fontSize = 16.sp,
                                                color = Color.White.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = {
                                                    // 카드 슬라이더를 오른쪽 끝(카드 추가 슬라이더)으로 이동
                                                    // apiCards.size는 실제 카드의 개수이고, 그 다음 인덱스가 카드 추가 슬라이더의 위치
                                                    coroutineScope.launch {
                                                        // 코루틴 내에서 애니메이션 실행
                                                        lazyListState.animateScrollToItem(apiCards.size + 1)
                                                    }

                                                    // 카드 추가 모드로 상태 변경
                                                    selectedCardIndex = -2
                                                    showAutoCardMode = false
                                                    showAddCardMode = false
                                                    selectedCard = null
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF00BCD4)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("카드 등록하기")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // 2. 중간 영역 (별자리)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.18f)
                ) {
                    if (!showAutoCardMode && selectedCard != null) {
                        // 일반 카드 모드 - 카드별 별자리 표시
                        DynamicZodiacView(
                            cardId = selectedCard!!.cardName,
                            modifier = Modifier.fillMaxSize(),
                            useJSON = true
                        )
                    } else if (selectedCardIndex == -2) { // 카드 추가 모드
                        // 카드 추가 안내 별자리 표시
                        ConstellationCarousel(
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // 자동 카드 추천 모드 - 별자리 생성 애니메이션 추가ㅎㅎ
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. 카드 슬라이더 영역
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
                    // 이전 상태 저장
                    previousCardIndex = selectedCardIndex
                    previousAutoCardMode = showAutoCardMode

                    // 카드 추가 모드로 전환
                    onShowCardOCRScan()
                }

                // 카드 슬라이더 영역
                PaymentCardScroll(
                    cards = apiCards,
                    cardWidth = cardWidth,
                    horizontalPadding = horizontalPadding,
                    lazyListState = lazyListState,
                    onCardIndexSelected = { index -> onCardSelected(index) },
                    onAddCardButtonClick = onAddCardButtonClick, // 카드 추가 버튼 클릭 핸들러 전달
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.3f)
                )
            }
        }

// QR 스캔 모드일 때는 전체 화면 QR 스캐너 표시 (탭 제외)
        if (showInternalQRScanner) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // QR 스캐너 전체 화면 표시
                QRScannerScreen(
                    onClose = {
                        // 닫기 버튼 클릭 시 바코드 모드로 전환
                        isBarcodeSelected = true
                    },
                    onQRCodeScanned = { qrCode ->
                        // QR 코드 스캔 결과 처리
                        Log.d("PaymentScreen", "스캔된 QR 코드: $qrCode")
                        viewModel.sendQRToken(qrCode)
                        // 바코드 모드로 전환
                        isBarcodeSelected = true
                        // 결제 정보 화면으로 이동
                        onShowPaymentInfo()
                    },
                    hideSystemBars = false // 시스템 바 유지
                )

                // 상단에 탭만 표시 - 원래 레이아웃과 동일한 구조 유지
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.44f) // 원래 상단 영역과 동일한 높이
                        .padding(top = 8.dp)
                ) {
                    // 원래 레이아웃과 동일한 구조 사용
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp), // 원래와 동일한 패딩
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 원래와 동일한 AnimatedTabRow 사용
                        AnimatedTabRow(
                            isBarcodeSelected = isBarcodeSelected,
                            onTabSelected = { selected -> isBarcodeSelected = selected }
                        )

                        // 나머지 공간은 투명하게 처리 (QR 스캐너가 보이도록)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

// 화면 상태에 따라 다른 UI 표시
    when (screenState) {
        PaymentScreenState.CARD_SELECTION -> {
            // 카드 선택 화면은 이미 표시되어 있음
        }

        PaymentScreenState.PROCESSING -> {
            // 결제 처리 중 화면 표시
            PaymentProcessing(
                paymentState = when (paymentState) {
                    is PaymentViewModel.PaymentState.Processing -> "Processing"
                    is PaymentViewModel.PaymentState.Completed -> "Completed"
                    is PaymentViewModel.PaymentState.Failed -> "Failed"
                    else -> "Idle"
                },
                onMinimumTimeElapsed = {
                    // 최소 3초가 지난 후 결과 화면으로 전환
                    if (paymentState is PaymentViewModel.PaymentState.Completed ||
                        paymentState is PaymentViewModel.PaymentState.Failed) {
                        screenState = PaymentScreenState.RESULT
                    }
                }
            )
        }

        PaymentScreenState.RESULT -> {
            // 결제 결과 화면 표시
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
            ) {
                PaymentResultScreen(
                    paymentResult = paymentResult,
                    isSuccess = paymentState is PaymentViewModel.PaymentState.Completed,
                    onConfirm = {
                        // 결제 결과 확인 후 홈 화면으로 이동
                        onNavigateToHome()
                    }
                )
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

    // 화면 하단에 결과 팝업 표시
    if (showResultPopup && cardRegistrationState !is PaymentViewModel.CardRegistrationState.Initial &&
        cardRegistrationState !is PaymentViewModel.CardRegistrationState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (cardRegistrationState is PaymentViewModel.CardRegistrationState.Success)
                        Color(0xFF4CAF50) else Color(0xFFE53935)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (cardRegistrationState is PaymentViewModel.CardRegistrationState.Success)
                            Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = when (cardRegistrationState) {
                            is PaymentViewModel.CardRegistrationState.Success ->
                                (cardRegistrationState as PaymentViewModel.CardRegistrationState.Success).message
                            is PaymentViewModel.CardRegistrationState.Error ->
                                "카드 등록 실패: ${(cardRegistrationState as PaymentViewModel.CardRegistrationState.Error).message}"
                            else -> ""
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
