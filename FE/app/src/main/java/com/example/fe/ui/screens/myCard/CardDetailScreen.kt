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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.fe.R
import com.example.fe.ui.components.backgrounds.GlassSurface
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

    // 카드 정보 상태 수집
    val cardInfoState by viewModel.cardInfoState.collectAsState()
    val transactionHistoryState by viewModel.transactionHistoryState.collectAsState()

    // 페이지네이션 상태 수집
    val isLoadingMoreTransactions by viewModel.isLoadingMoreTransactions.collectAsState()
    val canLoadMoreTransactions by viewModel.canLoadMoreTransactions.collectAsState()

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
            viewModel.getCardTransactionHistory(cardId, selectedMonth, 0, 10)
        }
        // 탭이 내역이고 데이터가 없는 경우에만 추가 로드
        else if (selectedTab == 0) {
            val currentState = transactionHistoryState
            if (currentState !is MyCardViewModel.TransactionHistoryState.Success ||
                currentState.allTransactions.isEmpty()) {
                viewModel.resetTransactionPagination()
                viewModel.getCardTransactionHistory(cardId, selectedMonth, 0, 10)
            }
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

    // 배경 투명도 효과
    val backgroundAlpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.7f,
        animationSpec = tween(700),
        label = "backgroundAlpha"
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

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
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

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "다음 달",
                                tint = Color.White,
                                modifier = Modifier
                                    .clickable {
                                        if (selectedMonth < 12) selectedMonth++
                                    }
                                    .size(30.dp)
                            )
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
                                    color = if (selectedTab == 0) Color.White else Color.Gray,
                                    fontSize = 26.sp,
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
                                            color = if (selectedTab == 0) Color.White else Color.Transparent
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
                                    color = if (selectedTab == 1) Color.White else Color.Gray,
                                    fontSize = 26.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                // 인디케이터
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(2.dp)
                                        .align(Alignment.CenterHorizontally)
                                        .background(
                                            color = if (selectedTab == 1) Color.White else Color.Transparent
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
                            when (selectedTab) {
                                0 -> TransactionsContent(
                                    transactions = transactions,
                                    isLoadingMore = isLoadingMoreTransactions,
                                    canLoadMore = canLoadMoreTransactions,
                                    onLoadMore = { viewModel.loadMoreTransactions(cardId, selectedMonth) }
                                )
                                1 -> cardInfo?.let { BenefitsContent(it) } ?: EmptyBenefitsContent()
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
    onLoadMore: () -> Unit
) {
    // 날짜별로 거래 내역 그룹화
    val transactionsByDate = transactions.groupBy { it.date.substring(0, 10) }

    // 스크롤 상태 관찰
    val listState = rememberLazyListState()

    // 스크롤이 끝에 도달했는지 확인하고 더 로드
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= listState.layoutInfo.totalItemsCount - 3 &&
                    canLoadMore &&
                    !isLoadingMore) {
                    onLoadMore()
                }
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 총 소비 및 혜택 금액 헤더
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                cornerRadius = 16f,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)  // 패딩 값 키움 (16.dp -> 24.dp)
                ) {
                    // 총액 정보
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "총 소비",
                            color = Color.White,
                            fontSize = 26.sp  // 폰트 크기 더 증가
                        )
                        
                        Text(
                            text = formatAmount(transactions.sumOf { it.amount }),
                            color = Color.White,
                            fontSize = 26.sp,  // 폰트 크기 더 증가
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))  // 간격 약간 증가
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "혜택 받은 금액",
                            color = Color.White,
                            fontSize = 24.sp  // 폰트 크기 더 증가
                        )
                        
                        Text(
                            text = formatAmount(transactions.sumOf { it.benefitAmount }),
                            color = Color(0xFFCCFF00), // 연두색
                            fontSize = 24.sp,  // 폰트 크기 더 증가
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        // 날짜별로 거래 내역 표시
        transactionsByDate.forEach { (date, dateTransactions) ->
            item {
                // 날짜별 패널
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    cornerRadius = 16f,
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)  // 패딩 값 키움 (16.dp -> 24.dp)
                    ) {
                        val formattedDate = formatDate(date)
                        // 날짜 헤더
                        Text(
                            text = formattedDate,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // 날짜에 해당하는 모든 거래 내역
                        dateTransactions.forEach { transaction ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 왼쪽: 시간
                                val time = formatTime(transaction.date)
                                Text(
                                    text = time,
                                    color = Color.White,
                                    fontSize = 18.sp,  // 폰트 크기 증가
                                    modifier = Modifier.width(60.dp)
                                )
                                
                                // 중앙: 아이콘과 장소 (좌측 정렬)
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 아이콘

                                    val iconColor = getCategoryColor(transaction.category)

                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(iconColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = transaction.category.take(1),
                                            color = Color.Black,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // 장소
                                    Text(
                                        text = transaction.merchantName,
                                        color = Color.White,
                                        fontSize = 20.sp,  // 폰트 크기 증가
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // 오른쪽: 금액
                                Column(
                                    modifier = Modifier.width(90.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = formatAmount(transaction.amount),
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (transaction.benefitAmount > 0) {
                                        Text(
                                            text = "혜택 ${formatAmount(transaction.benefitAmount)}",
                                            color = Color(0xFFCCFF00),
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                            
                            // 마지막 항목이 아니면 구분선 추가
                            if (transaction != dateTransactions.last()) {
                                HorizontalDivider(
                                    color = Color.Gray.copy(alpha = 0.2f),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 로딩 인디케이터
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // 더 이상 로드할 내역이 없을 때 메시지
        if (!canLoadMore && transactions.isNotEmpty()) {
            item {
                Text(
                    text = "더 이상 거래 내역이 없습니다",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        // 거래 내역이 없을 때
        if (transactions.isEmpty() && !isLoadingMore) {
            item {
                EmptyTransactionsContent()
            }
        }


    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BenefitsContent(cardInfo: MyCardViewModel.CardInfo) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 실적 구간 표시
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                cornerRadius = 16f,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
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
                        val maxAmount = cardInfo.maxPerformanceAmount.toFloat()
                        val currentProgress = (currentAmount / maxAmount).coerceIn(0f, 1f)

                        // 현재 실적 바
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(currentProgress)
                                .height(16.dp)
                                .background(Color(0xFF00BCD4), RoundedCornerShape(8.dp))
                        )

                        // 구간 마커 배치
                        val maxTier = cardInfo.spendingMaxTier

                        // 구간이 1개 이상일 때만 마커 표시 (0구간만 있으면 마커 없음)
                        if (maxTier > 0) {
                            // 레이아웃 내에서 마커 위치 계산을 위한 BoxWithConstraints 사용
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                val barWidth = maxWidth

                                // 1부터 maxTier까지의 마커 표시
                                for (tier in 1..maxTier) {
                                    // 각 구간 마커의 위치 계산 (균등 분할)
                                    // 예: maxTier가 2면, 1번 마커는 1/2 지점에 위치
                                    val position = tier.toFloat() / (maxTier + 1).toFloat()
                                    val xOffset = barWidth * position - 12.dp // 마커 중앙이 위치에 오도록 조정

                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .offset(x = xOffset)
                                            .background(
                                                if (cardInfo.currentSpendingTier >= tier) Color(0xFF00BCD4) else Color.Gray.copy(alpha = 0.5f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tier.toString(),
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
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            Row {
                                Text(
                                    text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(cardInfo.currentPerformanceAmount)}원",
                                    color = Color(0xFF00BCD4),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = " / ${NumberFormat.getNumberInstance(Locale.KOREA).format(cardInfo.maxPerformanceAmount)}원",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }

                            // 다음 구간까지 남은 금액 계산 부분 수정
                            val nextTierAmount = if (cardInfo.currentSpendingTier < cardInfo.spendingMaxTier) {
                                // 다음 구간의 금액 계산 (구간별로 균등하게 나눈다고 가정)
                                val amountPerTier = cardInfo.maxPerformanceAmount / cardInfo.spendingMaxTier
                                val nextTierThreshold = amountPerTier * (cardInfo.currentSpendingTier + 1) // 다음 구간 임계값

                                // 사용금액 - 실적 으로 계산
                                nextTierThreshold - cardInfo.currentPerformanceAmount
                            } else {
                                0 // 이미 최대 구간
                            }

                            Text(
                                text = "${NumberFormat.getNumberInstance(Locale.KOREA).format(nextTierAmount)}원 남음",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 현재 구간 혜택 정보
        item {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                cornerRadius = 16f,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // 구간 혜택 헤더
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${cardInfo.currentSpendingTier}구간 혜택",
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
                    cardInfo.benefits.forEach { benefit ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
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
                            val totalAmount = benefit.receivedBenefitAmount + benefit.remainingBenefitAmount
                            val progress = if (totalAmount > 0) benefit.receivedBenefitAmount.toFloat() / totalAmount else 0f

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
                                    text = "잔여 : " + formatAmount(benefit.remainingBenefitAmount) + "원",
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                        }

                        // 구분선 추가
                        if (benefit != cardInfo.benefits.last()) {
                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF5F77F5), CircleShape)
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

// 카테고리별 색상 반환 함수
fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "카페" -> Color(0xFFFFD700) // 금색
        "쇼핑" -> Color(0xFFFFA500) // 주황색
        "음식점" -> Color(0xFFFF6347) // 토마토색
        "편의점" -> Color(0xFF00CED1) // 청록색
        "마트" -> Color(0xFF32CD32) // 라임색
        "교통" -> Color(0xFF1E90FF) // 도지블루
        "의료" -> Color(0xFFFF69B4) // 핫핑크
        "문화" -> Color(0xFF9370DB) // 보라색
        else -> Color(0xFFFFD700) // 기본 금색
    }
}