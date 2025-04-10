package com.example.fe.ui.screens.myCard

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.GlassSurface
import com.example.fe.ui.components.categoryIcons.getCategoryIcon
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CardDetailScreen(
    cardId: Int,
    onBackClick: () -> Unit,
    onNavigationBarVisibilityChange: (Boolean) -> Unit = {},
    viewModel: MyCardViewModel = viewModel()
) {
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 내역, 1: 혜택
    
    // 애니메이션 시작 상태
    var animationStarted by remember { mutableStateOf(false) }
    
    // 요소별 표시 상태
    var showHeader by remember { mutableStateOf(false) }
    var showMonthSelector by remember { mutableStateOf(false) }
    var showCardName by remember { mutableStateOf(false) }
    var showTabs by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    // 배경 전환 애니메이션
    var showSolidBackground by remember { mutableStateOf(false) }
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (showSolidBackground) 1f else 0f,
        animationSpec = tween(700),
        label = "backgroundAlpha"
    )

    // 카드 정보 상태 수집
    val cardInfoState by viewModel.cardInfoState.collectAsState()
    val transactionHistoryState by viewModel.transactionHistoryState.collectAsState()

    // 페이지네이션 상태 수집
    val isLoadingMoreTransactions by viewModel.isLoadingMoreTransactions.collectAsState()
    val canLoadMoreTransactions by viewModel.canLoadMoreTransactions.collectAsState()

    // 거래 내역 로딩 재시도 상태 추가
    var retryCount by remember { mutableIntStateOf(0) }
    val maxRetries = 3

    LaunchedEffect(key1 = cardId, key2 = selectedMonth, key3 = selectedTab) {
        // 초기 설정
        viewModel.setSelectedCard(cardId)
        viewModel.setSelectedTab(
            if (selectedTab == 0) MyCardViewModel.CardDetailTab.TRANSACTION
            else MyCardViewModel.CardDetailTab.BENEFIT
        )

        // 월이 변경되었는지 확인
        val monthChanged = selectedMonth != viewModel.selectedMonth.value
        // 카드가 변경되었는지 확인
        val cardChanged = cardId != viewModel.selectedCardId.value

        // 월 설정 업데이트
        viewModel.setSelectedMonth(selectedMonth)

        // 카드 정보는 항상 로드 (월이 변경되거나 카드가 변경될 때)
        if (monthChanged || cardChanged) {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            viewModel.getMyCardInfo(cardId, currentYear, selectedMonth)
        }

        // 월이 변경되면 항상 거래 내역 초기화 (탭에 상관없이)
        if (monthChanged || cardChanged) {
            viewModel.resetTransactionPagination()
            // 거래 내역 데이터 로드 (탭에 상관없이)
            viewModel.getCardTransactionHistory(cardId, selectedMonth, 0, 50)
            retryCount = 0 // 재시도 카운트 초기화
        }
        // 탭이 내역이고 데이터가 없는 경우에만 추가 로드
        else if (selectedTab == 0) {
            val currentState = transactionHistoryState
            if (currentState !is MyCardViewModel.TransactionHistoryState.Success ||
                currentState.allTransactions.isEmpty()) {
                viewModel.resetTransactionPagination()
                viewModel.getCardTransactionHistory(cardId, selectedMonth, 0, 50)
                retryCount = 0 // 재시도 카운트 초기화
            }
        }
    }

    // 거래 내역 상태 변경 감지 및 자동 재시도
    LaunchedEffect(transactionHistoryState) {
        when (val state = transactionHistoryState) {
            is MyCardViewModel.TransactionHistoryState.Error -> {
                // 오류 발생 시 최대 3회까지 자동 재시도
                if (retryCount < maxRetries && selectedTab == 0) {
                    delay(1000) // 1초 대기 후 재시도
                    retryCount++
                    viewModel.getCardTransactionHistory(cardId, selectedMonth, 0, 50)
                }
            }
            is MyCardViewModel.TransactionHistoryState.Success -> {
                // 성공했지만 데이터가 비어있는 경우 재시도
                if (state.allTransactions.isEmpty() && retryCount < maxRetries && selectedTab == 0) {
                    delay(1000) // 1초 대기 후 재시도
                    retryCount++
                    viewModel.getCardTransactionHistory(cardId, selectedMonth, 0, 50)
                }
            }
            else -> { /* 다른 상태는 처리하지 않음 */ }
        }
    }


    // 애니메이션 시작 - 지연 적용
    LaunchedEffect(key1 = true) {
        // 처음에는 애니메이션 시작하지 않음 (카드가 원래 위치에서 시작)
        delay(100) // 화면이 보이고 약간의 지연 후 애니메이션 시작
        animationStarted = true
        delay(700) // 카드 애니메이션이 완료될 시간을 기다림
        showHeader = true
        delay(100)
        showMonthSelector = true
        delay(100)
        showCardName = true
        delay(100)
        showTabs = true
        delay(100)
        showContent = true
    }
    
    // 네비게이션 바 숨기기 효과 전달
    LaunchedEffect(key1 = Unit) {
        // 화면이 그려질 때 네비게이션 바 숨기기 함수 호출
        onNavigationBarVisibilityChange(false)
    }

    // 화면이 종료될 때 네비게이션 바 다시 표시
    DisposableEffect(key1 = Unit) {
        onDispose {
            onNavigationBarVisibilityChange(true)
        }
    }

    // 카드 크기 조정 (비율 유지)
    val cardScale by animateFloatAsState(
        targetValue = if (animationStarted) 0.85f else 1.3f,
        animationSpec = tween(600, easing = EaseOutQuart),
        label = "cardScale"
    )

    // 카드 위치 이동 효과 (초기에는 화면 하단에서 시작, 그 다음 상단으로 이동)
    val cardYOffset by animateFloatAsState(
        targetValue = if (animationStarted) 200f else 1850f,
        animationSpec = tween(700, easing = EaseInOut),
        label = "cardYOffset"
    )

    // 카드 회전 효과 (세로에서 가로로)
    val cardRotation by animateFloatAsState(
        targetValue = if (animationStarted) 0f else 90f,
        animationSpec = tween(700, easing = EaseInOut),
        label = "cardRotation"
    )

    // 거래 내역 가져오기
    val transactions = when (transactionHistoryState) {
        is MyCardViewModel.TransactionHistoryState.Success -> (transactionHistoryState as MyCardViewModel.TransactionHistoryState.Success).transactions
        is MyCardViewModel.TransactionHistoryState.Loading -> (transactionHistoryState as MyCardViewModel.TransactionHistoryState.Loading).previousTransactions ?: emptyList()
        is MyCardViewModel.TransactionHistoryState.Error -> (transactionHistoryState as MyCardViewModel.TransactionHistoryState.Error).previousTransactions ?: emptyList()
        else -> emptyList()
    }

    // 카드 정보 가져오기
    val cardInfo = when (cardInfoState) {
        is MyCardViewModel.CardInfoState.Success -> (cardInfoState as MyCardViewModel.CardInfoState.Success).cardInfo
        is MyCardViewModel.CardInfoState.Loading -> (cardInfoState as MyCardViewModel.CardInfoState.Loading).previousCardInfo
        is MyCardViewModel.CardInfoState.Error -> (cardInfoState as MyCardViewModel.CardInfoState.Error).previousCardInfo
        else -> null
    }

    val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) + 1 }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A1931))
                .graphicsLayer {
                    alpha = backgroundAlpha
                }
        )

        when {
            cardInfoState is MyCardViewModel.CardInfoState.Loading && cardInfo == null -> LoadingContent()
            cardInfoState is MyCardViewModel.CardInfoState.Error && cardInfo == null -> ErrorContent(
                message = (cardInfoState as MyCardViewModel.CardInfoState.Error).message,
                onRetry = { viewModel.getMyCardInfo(cardId) }
            )
            else -> {
                // 콘텐츠 부분 - TopBar는 AppNavigation에서 관리됨
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp, bottom = 16.dp) // TopBar 높이 패딩 감소
                ) {
                    // 월 선택 네비게이터
                    AnimatedVisibility(
                        visible = showMonthSelector,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInVertically(animationSpec = tween(300)) { -40 }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp, vertical = 2.dp), // 상하 패딩 더 줄임
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "이전 달",
                                tint = Color.White,
                                modifier = Modifier
                                    .clickable {
                                        if (selectedMonth > 1) selectedMonth--
                                    }
                                    .size(30.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedMonth}월",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 현재 월 이후로는 이동 불가능하도록 수정
                            if (selectedMonth < currentMonth) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "다음 달",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .clickable {
                                            if (selectedMonth < currentMonth) selectedMonth++
                                        }
                                        .size(30.dp)
                                )
                            } else {
                                // 현재 월일 때는 비활성화된 아이콘 표시
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = Color.Gray.copy(alpha = 0.2f),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }

                    // 카드를 위한 빈 공간 (애니메이션이 완료된 후의 위치)
                    Spacer(modifier = Modifier.height(125.dp)) // 약간 더 줄임

                    // 카드 이름
                    AnimatedVisibility(
                        visible = showCardName,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInVertically(animationSpec = tween(300)) { 40 }
                    ) {
                        Text(
                            text = cardInfo?.name ?: "카드 정보 없음",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 45.dp, bottom = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // 내역/혜택 탭
                    AnimatedVisibility(
                        visible = showTabs,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInVertically(animationSpec = tween(300)) { 40 }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 왼쪽 탭 (내역)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = 0 },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "내역",
                                    color = if (selectedTab == 0) Color(0xFF00BCD4) else Color.Gray,
                                    fontSize = 18.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                // 인디케이터
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(2.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .background(
                                            color = if (selectedTab == 0) Color(0xFF00BCD4) else Color.Transparent
                                        )
                                )
                            }

                            // 오른쪽 탭 (혜택)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = 1 },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "혜택",
                                    color = if (selectedTab == 1) Color(0xFF00BCD4) else Color.Gray,
                                    fontSize = 18.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                // 인디케이터
                                Box(
                                    modifier = Modifier
                                        .width(30.dp)
                                        .height(2.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .background(
                                            color = if (selectedTab == 1) Color(0xFF00BCD4) else Color.Transparent
                                        )
                                )
                            }
                        }
                    }

                    // 내역/혜택 내용
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(animationSpec = tween(300)) +
                                slideInVertically(animationSpec = tween(300)) { 40 }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // 남은 공간 모두 차지
                                .padding(top = 8.dp) // 상단 여백 추가
                        ) {
                            if (selectedTab == 0) {
                                when (val state = transactionHistoryState) {
                                    is MyCardViewModel.TransactionHistoryState.Loading -> {
                                        LoadingContent()
                                    }
                                    is MyCardViewModel.TransactionHistoryState.Success -> {
                                        TransactionsContent(
                                            transactions = state.allTransactions,
                                            isLoadingMore = isLoadingMoreTransactions,
                                            canLoadMore = canLoadMoreTransactions,
                                            onLoadMore = {
                                                viewModel.loadMoreTransactions()
                                            },
                                            isRetrying = retryCount < maxRetries && state.allTransactions.isEmpty(),
                                            onRetry = {
                                                retryCount = 0
                                                viewModel.getCardTransactionHistory(cardId, selectedMonth, 0, 50)
                                            }
                                        )
                                    }
                                    is MyCardViewModel.TransactionHistoryState.Error -> {
                                        // 이전 데이터가 있으면 표시
                                        if (state.previousTransactions != null && state.previousTransactions.isNotEmpty()) {
                                            TransactionsContent(
                                                transactions = state.previousTransactions,
                                                isLoadingMore = false,
                                                canLoadMore = false,
                                                onLoadMore = {},
                                                isRetrying = retryCount < maxRetries,
                                                onRetry = {
                                                    retryCount = 0
                                                    viewModel.getCardTransactionHistory(cardId, selectedMonth, 0, 50)
                                                }
                                            )
                                        } else {
                                            // 재시도 중이면 로딩 표시
                                            if (retryCount < maxRetries) {
                                                LoadingContent()
                                            } else {
                                                // 최대 재시도 후에도 오류면 오류 화면
                                                ErrorContent(
                                                    message = state.message,
                                                    onRetry = {
                                                        retryCount = 0
                                                        viewModel.getCardTransactionHistory(cardId, selectedMonth, 0, 50)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        LoadingContent()
                                    }
                                }
                            } else {
                                // 혜택 탭 내용
                                cardInfo?.let { BenefitsContent(it) } ?: EmptyBenefitsContent()

                            }
                        }
                    }
                }

                // 카드 이미지 - UI와 분리된 절대 위치에 배치
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .height(140.dp)
                        .align(Alignment.TopCenter)
                        .graphicsLayer(
                            scaleX = cardScale,
                            scaleY = cardScale,
                            translationY = cardYOffset,
                            rotationZ = cardRotation
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF673AB7)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AsyncImage(
                            model = cardInfo?.imageUrl ?: R.drawable.card,
                            contentDescription = "카드 이미지",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsContent(
    transactions: List<MyCardViewModel.TransactionInfo>,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    isRetrying: Boolean = false,
    onRetry: () -> Unit = {}
) {
    // 날짜별로 거래 내역 그룹화
    val transactionsByDate = transactions.groupBy { it.date.substring(0, 10) }

    // 스크롤 상태 관찰
    val listState = rememberLazyListState()

    // 자동 재시도 로직
    LaunchedEffect(isRetrying) {
        if (isRetrying && transactions.isEmpty()) {
            delay(1000) // 1초 대기
            onRetry() // 재시도
        }
    }

    if(transactions.isEmpty()){
        if (isRetrying) {
            // 재시도 중이면 로딩 표시
            LoadingContent()
        } else {
            // 최종적으로 데이터가 없으면 빈 화면
            EmptyTransactionsContent()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            state = listState
        ) {
            // 총 소비 및 혜택 금액 헤더
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 20.dp), // 양 옆에 패딩 추가
                ) {
                    // 총액 정보
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "총 소비",
                            color = Color.White,
                            fontSize = 24.sp,  // 폰트 크기 더 증가
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = formatAmount(transactions.sumOf { it.amount }),
                            color = Color.White,
                            fontSize = 24.sp,  // 폰트 크기 더 증가
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "혜택 받은 금액",
                            color = Color(0xFF00BCD4),
                            fontSize = 24.sp,  // 폰트 크기 더 증가
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = formatAmount(transactions.sumOf { it.benefitAmount }),
                            color = Color(0xFF00BCD4),
                            fontSize = 24.sp,  // 폰트 크기 더 증가
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 구분선
                HorizontalDivider(
                    color = Color(0xFF00BCD4).copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 18.dp)
                )
            }

            // 날짜별로 거래 내역 표시
            transactionsByDate.forEach { (date, dailyTransactions) ->
                item {
                    // 날짜 헤더와 해당 날짜 총 소비액
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 28.dp), // 양 옆에 더 많은 패딩 추가
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 날짜
                        Text(
                            text = formatDate(date),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal
                        )

                        // 해당 날짜 총 소비액
                        Text(
                            text = "${formatAmount(dailyTransactions.sumOf { it.amount })}원",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                //해당 날짜의 거래 내역
                items(dailyTransactions.size) { index ->
                    val transaction = dailyTransactions[index]

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 28.dp) // 양 옆에 더 많은 패딩 추가
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 왼쪽: 카테고리 아이콘 및 상점명
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 카테고리 아이콘
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color(0xFF00BCD4), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = getCategoryIcon(transaction.category)),
                                        contentDescription = transaction.category,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // 상점명
                                Text(
                                    text = transaction.merchantName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // 오른쪽: 금액 및 혜택
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                // 금액
                                Text(
                                    text = "${formatAmount(transaction.amount)}원",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Light
                                )

                                // 혜택 금액 (있는 경우만)
                                if (transaction.benefitAmount > 0) {
                                    Text(
                                        text = "할인 ${formatAmount(transaction.benefitAmount)}원",
                                        color = Color(0xFF00BCD4),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 날짜 구분선 (마지막 날짜가 아니면)
                if (date != transactionsByDate.keys.last()) {
                    item {
                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 28.dp)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BenefitsContent(cardInfo: MyCardViewModel.CardInfo) {

    // lastMonthPerformance가 0이거나 혜택 목록이 비어있는 경우 혜택 없음으로 처리
    val hasNoBenefits = cardInfo.lastMonthPerformance == 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 실적 구간 표시
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                // 실적 구간 프로그레스 바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(horizontal = 12.dp)
                ) {
                    // 배경 바
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    )

                    // 현재 진행률 계산 (0.0 ~ 1.0)
                    val currentAmount = cardInfo.currentPerformanceAmount.toFloat()
                    val maxAmount = cardInfo.performanceRange.lastOrNull()?.toFloat() ?: cardInfo.maxPerformanceAmount.toFloat()
                    val currentProgress = (currentAmount / maxAmount).coerceIn(0f, 1f)

                    // 현재 실적 바
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(currentProgress)
                            .height(16.dp)
                            .background(Color(0xFF00BCD4), RoundedCornerShape(8.dp))
                    )

                    // 구간 마커 배치
                    val performanceRanges = cardInfo.performanceRange

                    // 구간이 1개 이상일 때만 마커 표시 (마지막 인덱스는 제외)
                    if (performanceRanges.size > 1) {
                        // 레이아웃 내에서 마커 위치 계산을 위한 BoxWithConstraints 사용
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val barWidth = maxWidth

                            // 마커 개수 (마지막 인덱스 제외)
                            val markerCount = performanceRanges.size - 1

                            // 각 구간별 마커 표시 (마지막 인덱스 제외)
                            for (i in 0 until markerCount) {
                                // 마커 위치는 균등 분할 (예: 마커가 2개면 33%, 66% 위치)
                                val position = (i + 1).toFloat() / (markerCount + 1).toFloat()
                                val xOffset = barWidth * position - 12.dp // 마커 중앙이 위치에 오도록 조정

                                // 현재 마커가 나타내는 실적 값
                                val markerValue = performanceRanges[i]

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .offset(x = xOffset)
                                        .background(
                                            if (cardInfo.currentPerformanceAmount >= markerValue) Color(0xFF006064) else Color.Gray.copy(alpha = 0.5f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (i + 1).toString(),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 실적 정보 표시
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 왼쪽: 실적/사용금액 정보
                    Column {
                        Text(
                            text = "사용금액 / 실적",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "다음 구간까지",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }

                    // 오른쪽: 금액 정보
                    Column(horizontalAlignment = Alignment.End) {
                        // 현재 사용 금액에 따른 실적 구간 계산
                        val currentPerformanceAmount = cardInfo.currentPerformanceAmount

                        // 다음 실적 구간 찾기
                        val nextPerformanceTarget = cardInfo.performanceRange.find { range ->
                            range > currentPerformanceAmount
                        } ?: cardInfo.performanceRange.lastOrNull() ?: 0

                        Row {
                            Text(
                                text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(currentPerformanceAmount)}원",
                                color = Color(0xFF00BCD4),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " / ${NumberFormat.getNumberInstance(Locale.KOREA).format(nextPerformanceTarget)}원",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        // 다음 구간까지 남은 금액 계산
                        val remainingAmount = if (nextPerformanceTarget > currentPerformanceAmount) {
                            nextPerformanceTarget - currentPerformanceAmount
                        } else {
                            0 // 이미 최대 구간
                        }

                        Text(
                            text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(remainingAmount)}원 남음",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            // 구분선
            HorizontalDivider(
                color = Color(0xFF00BCD4).copy(alpha = 0.4f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
            )
        }

        // 현재 구간 혜택 정보
        item {
            if(hasNoBenefits){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "이번 달 혜택이 없습니다",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "전월 실적이 없어 이번 달에는 혜택을 받을 수 없습니다.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "이번 달 실적을 쌓아 다음 달 혜택을 받아보세요!",
                            color = Color(0xFF00BCD4),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // 구간 혜택 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${cardInfo.lastMonthPerformance}구간 혜택",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${formatAmount(cardInfo.benefits.sumOf { it.receivedBenefitAmount })}원",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 혜택 목록
                cardInfo.benefits.forEachIndexed { index, benefit ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 28.dp)
                    ) {
                        // 혜택 카테고리 및 퍼센트
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = benefit.categories.joinToString(", "),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // 혜택 비율 (카테고리에 따라 다르게 표시)
                            val benefitPercentage = when {
                                benefit.categories.any { it.contains("롯데리아") || it.contains("외식") } -> "1.2% 할인"
                                benefit.categories.any { it.contains("쇼핑") || it.contains("소핑") } -> "2% 할인"
                                else -> "1% 할인"
                            }

                            Text(
                                text = benefitPercentage,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 혜택 프로그레스 바
                        val totalAmount = benefit.receivedBenefitAmount + benefit.maxBenefitAmount
                        val progress = if (benefit.maxBenefitAmount == 0) {
                            1f // 무제한일 경우 프로그레스 바 100% 채움
                        } else if (totalAmount > 0) {
                            benefit.receivedBenefitAmount.toFloat() / totalAmount
                        } else {
                            0f
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .height(10.dp)
                                    .background(Color(0xFF5F77F5), RoundedCornerShape(4.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 혜택 상세 금액
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // 사용한 혜택 금액
                            Text(
                                text = formatAmount(benefit.receivedBenefitAmount) + " 원",
                                color = Color.White,
                                fontSize = 18.sp
                            )

                            // 잔여 혜택 금액
                            Text(
                                text = if (benefit.maxBenefitAmount == 0) {
                                    "무제한"
                                } else {
                                    "잔여 : " + formatAmount(benefit.maxBenefitAmount) + "원"
                                },
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    }

                    // 구분선 추가 (마지막 항목이 아닌 경우)
                    if (index < cardInfo.benefits.size - 1) {
                        HorizontalDivider(
                            color = Color.Gray.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTransactionsContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "거래 내역이 없습니다",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "카드를 사용하면 거래 내역이 표시됩니다",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun EmptyBenefitsContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "혜택 정보가 없습니다",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "카드를 사용하면 혜택 정보가 표시됩니다",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}


@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 간단한 로딩 인디케이터
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "로딩 중...",
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "오류가 발생했습니다",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF5F77F5))
                    .clickable { onRetry() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "다시 시도",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 금액 포맷팅 함수
fun formatAmount(amount: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale.KOREA)
    return formatter.format(amount)
}

// 거래 내역 날짜 포맷팅 함수
fun formatDate(date: String): String {
    // 예: "2023-03-13T09:34:00" -> "03.13(월)"
    try {
        val parts = date.split("T")[0].split("-")
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        // 요일 계산 (간단한 구현, 실제로는 Calendar나 LocalDate 사용 권장)
        val dayOfWeek = when (day % 7) {
            0 -> "일"
            1 -> "월"
            2 -> "화"
            3 -> "수"
            4 -> "목"
            5 -> "금"
            else -> "토"
        }

        return String.format("%02d.%02d(%s)", month, day, dayOfWeek)
    } catch (e: Exception) {
        return date
    }
}

// 거래 내역 시간 포맷팅 함수
fun formatTime(date: String): String {
    // 예: "2023-03-13T09:34:00" -> "09:34"
    try {
        val timePart = date.split("T")[1]
        return timePart.substring(0, 5)
    } catch (e: Exception) {
        return ""
    }
}
