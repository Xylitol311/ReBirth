package com.example.fe.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fe.ui.components.backgrounds.GlassSurface
import com.example.fe.ui.components.backgrounds.StarryBackground
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import android.util.Log

// 가계부 항목 데이터 클래스
data class TransactionItem(
    val id: Int,
    val date: LocalDate,
    val category: String,
    val place: String,
    val amount: Int, // 양수: 수입, 음수: 지출
    val paymentMethod: String
)

// 날짜별 수입/지출 요약 데이터 클래스
data class DailySummary(
    val income: Int = 0,
    val expense: Int = 0
)

// 색상 정의
val brightRed = Color(0xFFFF6B6B)
val brightGreen = Color(0xFF69F0AE)
val calendarBlue = Color(0xFF00E1FF) // 네온 블루

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {},
    viewModel: CalendarViewModel = viewModel()
) {
    // ViewModel에서 선택된 탭 인덱스를 가져옴
    var selectedTabIndex = viewModel.selectedTabIndex
    var scrollOffset by remember { mutableStateOf(0f) }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // ViewModel에서 상태 가져오기
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val monthlyData by viewModel.monthlyData.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val monthlyInfo by viewModel.monthlyInfo.collectAsState()
    val reportData by viewModel.reportData.collectAsState()
    
    // 날짜별 데이터 그룹화 - monthlyData가 변경될 때마다 다시 계산
    val dailySummaries by remember(monthlyData) {
        mutableStateOf(viewModel.getDailySummaries())
    }
    
    // 이번 달 전체 수입/지출 합계 - monthlyData가 변경될 때마다 다시 계산
    val monthlySummary by remember(monthlyData) {
        mutableStateOf(viewModel.getMonthlySummary())
    }
    
    // 데이터가 로드되면 로그에 출력
    LaunchedEffect(monthlyData) {
        if (monthlyData.isNotEmpty()) {
            Log.d("CalendarScreen", "데이터 로드됨: ${monthlyData.size}일, 요약: ${dailySummaries.size}일")
        }
    }
    
    // 위로 가기 버튼 표시 여부
    val showScrollToTopButton by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 200
        }
    }
    
    // 현재 선택된 연월 및 날짜
    val selectedYearMonth = viewModel.selectedYearMonth
    val selectedDate = viewModel.selectedDate
    
    // 날짜별 위치 인덱스를 저장하는 맵
    val dateIndexMap = remember { mutableMapOf<LocalDate, Int>() }
    
    // 현재 연월 저장
    val currentMonth = remember { YearMonth.now() }
    
    // 특정 날짜의 거래 기록
    val dateTransactions = remember(transactions, selectedDate) {
        viewModel.getTransactionsForDate(selectedDate)
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
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        StarryBackground(
            scrollOffset = scrollOffset,
            starCount = 150,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                state = lazyListState
            ) {
                // 상단 탭
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 탭 바
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 왼쪽 탭 (가계부)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { 
                                    viewModel.selectTab(0)
                                    selectedTabIndex = 0
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "가계부",
                                color = if (selectedTabIndex == 0) calendarBlue else Color.Gray,
                                fontSize = 18.sp,
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(2.dp)
                                    .background(
                                        color = if (selectedTabIndex == 0) calendarBlue else Color.Transparent
                                    )
                            )
                        }
                        
                        // 오른쪽 탭 (소비 리포트)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { 
                                    viewModel.selectTab(1) 
                                    selectedTabIndex = 1
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "소비 리포트",
                                color = if (selectedTabIndex == 1) calendarBlue else Color.Gray,
                                fontSize = 18.sp,
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(2.dp)
                                    .background(
                                        color = if (selectedTabIndex == 1) calendarBlue else Color.Transparent
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                when (selectedTabIndex) {
                    0 -> {
                        // 월 선택 (GlassSurface 바깥)
                        item {
                            // 월 선택 및 화살표
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp, start = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 왼쪽 화살표 (<)
                                Text(
                                    text = "<",
                                    color = Color.White,
                                    fontSize = 26.sp,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.navigateToPreviousMonth()
                                        }
                                        .padding(end = 12.dp)
                                )
                                
                                // 월과 화살표 표시
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${selectedYearMonth.monthValue}월",
                                        color = Color.White,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    // 오른쪽 화살표 (>)
                                    // 현재 월이면 비활성화된 스타일로 보여주기
                                    val isCurrentMonth = selectedYearMonth == currentMonth
                                    Text(
                                        text = ">",
                                        color = if (isCurrentMonth) Color.Gray else Color.White,
                                        fontSize = 26.sp,
                                        modifier = Modifier
                                            .alpha(if (isCurrentMonth) 0.5f else 1f)
                                            .clickable(enabled = !isCurrentMonth) {
                                                if (!isCurrentMonth) {
                                                    viewModel.navigateToNextMonth()
                                                }
                                            }
                                            .padding(start = 12.dp, end = 16.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // 연도 표시 (오른쪽 끝)
                                Text(
                                    text = "${selectedYearMonth.year}",
                                    color = Color.Gray,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        
                        // 요일 헤더 (GlassSurface 바깥)
                        item {
                            // 요일 표시
                            val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            ) {
                                daysOfWeek.forEach { day ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = day,
                                            color = when(day) {
                                                "일" -> Color(0xFFFF5252)
                                                "토" -> Color(0xFF448AFF)
                                                else -> Color.White
                                            },
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        
                        // 달력 그리드 (GlassSurface 안)
                        item {
                            GlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                cornerRadius = 16f
                            ) {
                                // 달력 그리드만 표시
                                Box(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    if (isLoading) {
                                        // 로딩 상태 표시
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                color = calendarBlue,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    } else {
                                        CalendarGrid(
                                            yearMonth = selectedYearMonth,
                                            selectedDate = selectedDate,
                                            dailySummaries = dailySummaries,
                                            onDateSelected = { date ->
                                                viewModel.selectDate(date)
                                                // 해당 날짜의 거래 내역으로 스크롤
                                                dateIndexMap[date]?.let { index ->
                                                    coroutineScope.launch {
                                                        lazyListState.animateScrollToItem(index)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 에러 메시지 표시
                        if (error != null) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0x33FF0000)
                                    )
                                ) {
                                    Text(
                                        text = "데이터 로드 중 오류가 발생했습니다: ${error}",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        // 월간 요약
                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // API에서 totalSpendingAmount가 음수로 오는 경우 절대값으로 변환
                            val totalSpending = monthlyInfo?.totalSpendingAmount?.let { if (it < 0) -it else it } ?: monthlySummary.expense
                            val topCategory = monthlyInfo?.categoryName ?: "카페"
                            val monthlyDifference = monthlyInfo?.monthlyDifferenceAmount ?: 3000
                            
                            // 총 소비 금액
                            Text(
                                text = "${formatAmount(totalSpending)}원 소비",
                                color = calendarBlue,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // 최다 소비 카테고리
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    text = buildAnnotatedString {
                                        append("최다 소비 ")
                                        withStyle(style = SpanStyle(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )) {
                                            append(topCategory)
                                        }
                                    },
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 16.sp
                                )
                            }
                            
                            // 지난달 비교 정보 (더 소비했는지 절약했는지에 따라 색상 다르게 표시)
                            val (monthlyDiffText, monthlyDiffColor, monthlyDiffAmount) = if (monthlyDifference > 0) {
                                // 지난달보다 더 소비한 경우 (빨간색)
                                Triple("지난달보다 ", brightRed, formatAmount(monthlyDifference))
                            } else if (monthlyDifference < 0) {
                                // 지난달보다 절약한 경우 (초록색)
                                Triple("지난달보다 ", brightGreen, formatAmount(-monthlyDifference))
                            } else {
                                // 변동 없음
                                Triple("지난달과 동일한 소비", Color.White.copy(alpha = 0.7f), "")
                            }
                            
                            // 금액 부분만 굵게 표시
                            if (monthlyDifference != 0) {
                                Text(
                                    text = buildAnnotatedString {
                                        append(monthlyDiffText)
                                        withStyle(style = SpanStyle(
                                            fontWeight = FontWeight.Bold
                                        )) {
                                            append(monthlyDiffAmount + "원")
                                        }
                                        append(if (monthlyDifference > 0) " 더 소비중" else " 절약중")
                                    },
                                    color = monthlyDiffColor,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            } else {
                                Text(
                                    text = monthlyDiffText,
                                    color = monthlyDiffColor,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // 월별 일자별 데이터를 날짜로 변환하여 표시
                        monthlyData.forEach { dailyLog ->
                            try {
                                // 유효한 날짜인지 확인 (해당 월의 일수를 초과하지 않는지)
                                if (dailyLog.day <= selectedYearMonth.lengthOfMonth()) {
                                    // LocalDate로 변환
                                    val date = LocalDate.of(selectedYearMonth.year, selectedYearMonth.month, dailyLog.day)
                                    
                                    // 인덱스 맵에 저장 (기본 아이템 개수 + 인덱스)
                                    dateIndexMap[date] = 5 + monthlyData.indexOf(dailyLog)
                                    
                                    // 해당 날짜에 데이터가 있는 경우만 표시
                                    if (dailyLog.plus > 0 || dailyLog.minus < 0) {
                                        item {
                                            // 거래 내역 카드 (날짜와 거래 내역을 같은 GlassSurface에 표시)
                                            GlassSurface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
                                                cornerRadius = 12f
                                            ) {
                                                Column {
                                                    // 일자 헤더
                                                    Text(
                                                        text = "${date.dayOfMonth}일 ${date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)}요일",
                                                        color = Color.White,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                                    )
                                                    
                                                    // 헤더와 거래 내역 사이 구분선
                                                    Divider(
                                                        color = Color.White.copy(alpha = 0.2f),
                                                        thickness = 1.dp
                                                    )
                                                    
                                                    // 해당 날짜의 거래 내역 불러오기
                                                    val dayTransactions = viewModel.getTransactionsForDate(date)
                                                    
                                                    if (dayTransactions.isNotEmpty()) {
                                                        // 실제 거래 내역이 있는 경우
                                                        dayTransactions.forEachIndexed { index, transaction ->
                                                            // 거래 내역 표시
                                                            DailyTransactionItem(
                                                                category = transaction.categoryName ?: "기타",
                                                                place = transaction.merchantName ?: "",
                                                                amount = transaction.amount,
                                                                paymentMethod = transaction.cardName ?: "",
                                                                dateTime = transaction.date
                                                            )
                                                            
                                                            // 마지막 항목이 아니면 구분선 추가
                                                            if (index < dayTransactions.size - 1) {
                                                                Divider(
                                                                    color = Color.White.copy(alpha = 0.1f),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(horizontal = 16.dp)
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        // 거래 내역이 없지만 일별 요약 데이터는 있는 경우
                                                        // 수입이 있으면 표시
                                                        if (dailyLog.plus > 0) {
                                                            DailyTransactionItem(
                                                                category = "수입",
                                                                place = "입금",
                                                                amount = dailyLog.plus,
                                                                paymentMethod = "계좌이체",
                                                                dateTime = ""
                                                            )
                                                            
                                                            // 수입과 지출 사이 구분선
                                                            if (dailyLog.minus < 0) {
                                                                Divider(
                                                                    color = Color.White.copy(alpha = 0.1f),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(horizontal = 16.dp)
                                                                )
                                                            }
                                                        }
                                                        
                                                        // 지출이 있으면 표시 (minus는 음수로 오므로 그대로 전달)
                                                        if (dailyLog.minus < 0) {
                                                            DailyTransactionItem(
                                                                category = "지출",
                                                                place = "결제",
                                                                amount = dailyLog.minus,
                                                                paymentMethod = "카드",
                                                                dateTime = ""
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Log.w("CalendarScreen", "Invalid day for ${selectedYearMonth.month}: ${dailyLog.day}")
                                }
                            } catch (e: Exception) {
                                Log.e("CalendarScreen", "Error processing date: $e")
                            }
                        }
                        
                        // 하단 여백
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                    1 -> {
                        // 소비 리포트 내용
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 월 선택 및 화살표
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp, start = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 이전 월 버튼 (활성화 여부에 따라 스타일 변경)
                                    val canGoToPrevMonth = viewModel.canNavigateToPreviousReportMonth()
                                    Text(
                                        text = "<",
                                        color = if (canGoToPrevMonth) Color.White else Color.Gray,
                                        fontSize = 26.sp,
                                        modifier = Modifier
                                            .alpha(if (canGoToPrevMonth) 1f else 0.5f)
                                            .clickable(enabled = canGoToPrevMonth) {
                                                viewModel.navigateToPreviousReportMonth()
                                            }
                                            .padding(end = 12.dp)
                                    )
                                    
                                    // 선택된 리포트 월 표시
                                    Text(
                                        text = "${viewModel.selectedReportYearMonth.monthValue}월",
                                        color = Color.White,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    // 다음 월 버튼 (활성화 여부에 따라 스타일 변경)
                                    val canGoToNextMonth = viewModel.canNavigateToNextReportMonth()
                                    Text(
                                        text = ">",
                                        color = if (canGoToNextMonth) Color.White else Color.Gray,
                                        fontSize = 26.sp,
                                        modifier = Modifier
                                            .alpha(if (canGoToNextMonth) 1f else 0.5f)
                                            .clickable(enabled = canGoToNextMonth) {
                                                viewModel.navigateToNextReportMonth()
                                            }
                                            .padding(start = 12.dp)
                                    )
                                }

                                // 리포트 로딩 중 표시
                                if (isLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = calendarBlue,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                } else {
                                    // 리포트 데이터가 로드되면 로그에만 출력 (UI에는 표시하지 않음)
                                    Text(
                                        text = "${viewModel.selectedReportYearMonth.year}년 ${viewModel.selectedReportYearMonth.monthValue}월 소비 리포트",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                    
                                    // 리포트 데이터 표시
                                    if (reportData != null) {
                                        // 카드별 리포트 데이터
                                        val cardReports by viewModel.cardReports.collectAsState()
                                        LaunchedEffect(cardReports) {
                                            Log.d("CalendarScreen", "Card reports count: ${cardReports.size}")
                                            cardReports.forEach { card ->
                                                Log.d("CalendarScreen", "Card: ${card.name}, Total: ${card.totalAmount}원, Benefit: ${card.totalBenefit}원")
                                                card.categories.forEach { category ->
                                                    Log.d("CalendarScreen", "  ${category.category}: ${category.amount}원 소비, ${category.benefit}원 혜택 (${category.count}회)")
                                                }
                                            }
                                        }
                                        
                                        Text(
                                            text = "카드별 소비 및 혜택",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                                        )
                                        
                                        if (cardReports.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 24.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "카드 사용 내역이 없습니다",
                                                    color = Color.Gray,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        } else {
                                            // 카드별 리포트 리스트
                                            cardReports.forEach { card ->
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 8.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(0xFF2A2A2A)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp)
                                                    ) {
                                                        // 카드 이름 및 총 사용금액
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(bottom = 12.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = card.name,
                                                                color = Color.White,
                                                                fontSize = 16.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            
                                                            Column(
                                                                horizontalAlignment = Alignment.End
                                                            ) {
                                                                // API에서 금액이 0으로 올 경우 계산된 값 사용
                                                                val totalAmount = card.getCalculatedTotalAmount()
                                                                val totalBenefit = card.getCalculatedTotalBenefit()
                                                                
                                                                Text(
                                                                    text = "${formatAmount(totalAmount)}원",
                                                                    color = Color.White,
                                                                    fontSize = 16.sp,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                
                                                                Text(
                                                                    text = "혜택 ${formatAmount(totalBenefit)}원",
                                                                    color = Color(0xFF4CAF50),
                                                                    fontSize = 14.sp
                                                                )
                                                            }
                                                        }
                                                        
                                                        // 구분선
                                                        Divider(
                                                            color = Color(0xFF3A3A3A),
                                                            thickness = 1.dp,
                                                            modifier = Modifier.padding(bottom = 8.dp)
                                                        )
                                                        
                                                        // 카테고리별 내역 (상위 3개만 표시)
                                                        val topCategories = card.categories.sortedByDescending { 
                                                            // 금액이 음수로 올 수 있으므로 절대값으로 정렬
                                                            if (it.amount < 0) -it.amount else it.amount 
                                                        }.take(3)
                                                        
                                                        topCategories.forEach { category ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(vertical = 4.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(
                                                                    text = "${category.category} (${category.count}회)",
                                                                    color = Color.LightGray,
                                                                    fontSize = 14.sp
                                                                )
                                                                
                                                                Row {
                                                                    // amount가 음수로 올 수 있으므로 절대값으로 표시
                                                                    val displayAmount = if (category.amount < 0) -category.amount else category.amount
                                                                    
                                                                    Text(
                                                                        text = "${formatAmount(displayAmount)}원",
                                                                        color = Color.LightGray,
                                                                        fontSize = 14.sp
                                                                    )
                                                                    
                                                                    if (category.benefit > 0) {
                                                                        Text(
                                                                            text = " (${formatAmount(category.benefit)}원)",
                                                                            color = Color(0xFF4CAF50),
                                                                            fontSize = 14.sp
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // 카테고리별 리포트 데이터
                                        val categoryReports by viewModel.categoryReports.collectAsState()
                                        LaunchedEffect(categoryReports) {
                                            Log.d("CalendarScreen", "Category reports count: ${categoryReports.size}")
                                            categoryReports.forEach { category ->
                                                Log.d("CalendarScreen", "Category: ${category.category}, Amount: ${category.amount}원, Benefit: ${category.benefit}원")
                                            }
                                        }
                                        
                                        Text(
                                            text = "카테고리별 소비 및 혜택",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                                        )
                                        
                                        if (categoryReports.isEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 24.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "카테고리 사용 내역이 없습니다",
                                                    color = Color.Gray,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        } else {
                                            // 카테고리별 차트 (간단한 막대형)
                                            val maxAmount = categoryReports.maxOfOrNull { it.getAbsoluteAmount() } ?: 1
                                            
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp)
                                                    .background(
                                                        color = Color(0xFF2A2A2A),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(16.dp)
                                            ) {
                                                // 카테고리별 리포트 리스트
                                                categoryReports.sortedByDescending { it.getAbsoluteAmount() }.forEach { category ->
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 8.dp)
                                                    ) {
                                                        // 카테고리명 및 금액
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = category.category,
                                                                color = Color.White,
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                            
                                                            Row {
                                                                // amount가 음수로 올 수 있으므로 절대값으로 표시
                                                                val displayAmount = category.getAbsoluteAmount()
                                                                
                                                                Text(
                                                                    text = "${formatAmount(displayAmount)}원",
                                                                    color = Color.White,
                                                                    fontSize = 14.sp
                                                                )
                                                                
                                                                if (category.benefit > 0) {
                                                                    Text(
                                                                        text = " (${formatAmount(category.benefit)}원)",
                                                                        color = Color(0xFF4CAF50),
                                                                        fontSize = 14.sp
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        
                                                        // 막대 그래프
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(12.dp)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(Color(0xFF3A3A3A))
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxHeight()
                                                                    .fillMaxWidth(category.getAbsoluteAmount().toFloat() / maxAmount)
                                                                    .background(Color(0xFF4285F4))
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "리포트 데이터를 불러오는 중입니다.",
                                                color = Color.Gray,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 위로 가기 버튼
            AnimatedVisibility(
                visible = showScrollToTopButton,
                enter = fadeIn() + slideInVertically { it * 2 },
                exit = fadeOut() + slideOutVertically { it * 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                // 유리 효과가 적용된 둥근 버튼
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        .clickable {
                            coroutineScope.launch {
                                // 맨 위로 부드럽게 스크롤
                                lazyListState.animateScrollToItem(0)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // 화살표 위로 아이콘
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "맨 위로 가기",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// API 데이터에 대한 일별 거래 항목 표시
@Composable
fun DailyTransactionItem(
    category: String,
    place: String,
    amount: Int,
    paymentMethod: String,
    dateTime: String = "" // 시간 정보 추가
) {
    val isIncome = amount > 0
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 카테고리
            Text(
                text = category,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(100.dp) // 카테고리 영역의 너비 증가
            )
            
            // 오른쪽 영역: 금액과 장소/결제수단 (같은 시작점 유지)
            Column {
                // 금액과 시간 표시 (가로로 배치)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 금액
                    Text(
                        text = if (isIncome) "+${formatAmount(amount)}원"
                               else "-${formatAmount(-amount)}원",
                        color = if (isIncome) calendarBlue else brightRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // 시간 (있는 경우에만 표시)
                    if (dateTime.isNotBlank()) {
                        // 컴포저블 외부에서 미리 파싱
                        val timeStr = parseTimeFromDateTime(dateTime)
                        if (timeStr.isNotEmpty()) {
                            Text(
                                text = timeStr,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 상점 이름과 카드 이름을 함께 표시 (있는 경우에만)
                val placeText = if (place.isNotBlank() && place != " ") place else ""
                val cardText = if (paymentMethod.isNotBlank() && paymentMethod != " ") paymentMethod else ""
                
                // 둘 다 있으면 "상점명/카드명", 하나만 있으면 해당 값만
                val displayText = when {
                    placeText.isNotBlank() && cardText.isNotBlank() -> "$placeText/$cardText"
                    placeText.isNotBlank() -> placeText
                    cardText.isNotBlank() -> cardText
                    else -> ""
                }
                
                if (displayText.isNotBlank()) {
                    Text(
                        text = displayText,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// 컴포저블 외부에서 시간 파싱하는 함수
fun parseTimeFromDateTime(dateTimeString: String): String {
    return try {
        val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
        val localDateTime = LocalDateTime.parse(dateTimeString, dateTimeFormatter)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        localDateTime.format(timeFormatter)
    } catch (e: Exception) {
        Log.e("DailyTransactionItem", "Error parsing dateTime: $dateTimeString", e)
        ""
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    dailySummaries: Map<LocalDate, DailySummary>,
    onDateSelected: (LocalDate) -> Unit
) {
    // 해당 월의 날짜 목록 생성
    val daysInMonth = remember(yearMonth) {
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        
        // 첫 주의 시작일 (이전 달의 날짜 포함)
        val start = firstDay.minusDays(firstDay.dayOfWeek.value % 7L)
        
        // 마지막 주의 종료일 (다음 달의 날짜 포함)
        val daysToAdd = 7 - lastDay.dayOfWeek.value % 7
        val end = if (daysToAdd < 7) lastDay.plusDays(daysToAdd.toLong()) else lastDay
        
        // 날짜 목록 생성
        (0..end.toEpochDay() - start.toEpochDay())
            .map { start.plusDays(it) }
    }
    
    // 주의 개수 계산 (7일씩 표시)
    val numberOfWeeks = (daysInMonth.size + 6) / 7
    
    // Grid 대신 Column과 Row로 구성하여 모든 날짜가 한 번에 표시되도록 함
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 주 단위로 행 생성
        for (weekIndex in 0 until numberOfWeeks) {
            val startIndex = weekIndex * 7
            val endIndex = minOf(startIndex + 6, daysInMonth.size - 1)
            
            if (startIndex <= endIndex) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    for (dayIndex in startIndex..endIndex) {
                        val date = daysInMonth[dayIndex]
                        CalendarDay(
                            date = date,
                            yearMonth = yearMonth,
                            isSelected = date == selectedDate,
                            summary = dailySummaries[date],
                            onDateSelected = onDateSelected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // 마지막 주에서 빈 칸 채우기
                    if (endIndex - startIndex < 6) {
                        for (i in 0 until 6 - (endIndex - startIndex)) {
                            Box(modifier = Modifier.weight(1f)) {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDay(
    date: LocalDate,
    yearMonth: YearMonth,
    isSelected: Boolean,
    summary: DailySummary?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentMonth = date.month == yearMonth.month
    val isToday = date == LocalDate.now()
    
    // 디버깅 로그 추가
    if (isCurrentMonth) {
        if (summary != null) {
            Log.d("CalendarDay", "데이터 있음: $date, Income: ${summary.income}, Expense: ${summary.expense}")
        } else {
            Log.d("CalendarDay", "데이터 없음: $date")
        }
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSelected) Color(0x33FFFFFF)
                else Color.Transparent
            )
            .clickable(enabled = isCurrentMonth) {
                onDateSelected(date)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(2.dp)
        ) {
            // 날짜
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 18.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    !isCurrentMonth -> Color(0xFF555555) // 더 어두운 회색
                    date.dayOfWeek.value == 7 -> Color(0xFFFF5252) // 일요일
                    date.dayOfWeek.value == 6 -> Color(0xFF448AFF) // 토요일
                    else -> Color.White
                },
                textAlign = TextAlign.Center
            )
            
            // 수입/지출 표시 (현재 달의 날짜만)
            if (summary != null && isCurrentMonth) {
                // 수입이 있으면 표시 (+기호 포함)
                if (summary.income > 0) {
                    Text(
                        text = "+${formatAmount(summary.income)}",
                        fontSize = 12.sp,
                        color = calendarBlue,
                        textAlign = TextAlign.Center
                    )
                }
                
                // 지출이 있으면 표시 (expense는 양수로 변환됨)
                if (summary.expense > 0) {
                    Text(
                        text = "-${formatAmount(summary.expense)}",
                        fontSize = 12.sp,
                        color = brightRed,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// 금액 포맷팅 (예: 10000 -> 10,000)
fun formatAmount(amount: Int): String {
    return String.format("%,d", amount)
}

// 컴팩트한 금액 표시 (작은 공간용)
fun formatCompactAmount(amount: Int): String {
    return formatAmount(amount)
}

// 날짜 헤더 포맷팅 (예: 3월 15일 (수요일))
fun formatDateHeader(date: LocalDate): String {
    val month = date.monthValue
    val day = date.dayOfMonth
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
    
    return "${month}월 ${day}일 (${dayOfWeek})"
} 