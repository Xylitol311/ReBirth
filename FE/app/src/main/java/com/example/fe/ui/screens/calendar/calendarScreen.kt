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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fe.R
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import android.util.Log
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.PagerState
import com.example.fe.data.model.calendar.ReportData
import com.example.fe.data.model.calendar.CardReport
import com.example.fe.data.model.calendar.CategoryReport
import com.example.fe.data.model.calendar.CardCategoryReport
import com.example.fe.data.model.calendar.ConsumptionPattern
import android.os.Handler
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image

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
val brightRed = Color(0xFFFF00FF) // 분홍색으로 변경
val brightGreen = Color(0xFF00E1FF) // 하늘색으로 변경
val calendarBlue = Color(0xFF00E1FF) // 네온 블루
val glowingYellow = Color(0xFFFFC107) // 빛나는 노란색

// 소비 리포트 페이지 탭
enum class ReportPageType {
    OVERVIEW,
    CARD_DETAIL,
    CATEGORY_DETAIL
}

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
                                            Icon(
                                    imageVector = Icons.Filled.KeyboardArrowLeft,
                                                contentDescription = "이전 달",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable {
                                            viewModel.navigateToPreviousMonth()
                                        }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // 월 표시
                                        Text(
                                    text = "${selectedYearMonth.monthValue}월",
                                            color = Color.White,
                                    fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // 오른쪽 화살표 (>)
                                // 현재 월이면 비활성화된 스타일로 보여주기
                                val isCurrentMonth = selectedYearMonth == currentMonth
                                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowRight,
                                                    contentDescription = "다음 달",
                                    tint = if (isCurrentMonth) Color.Gray else Color.White,
                                    modifier = Modifier
                                        .alpha(if (isCurrentMonth) 0.5f else 1f)
                                        .size(42.dp)
                                        .clickable(enabled = !isCurrentMonth) {
                                            if (!isCurrentMonth) {
                                                viewModel.navigateToNextMonth()
                                            }
                                        }
                                )
                                
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
                                cornerRadius = 16f,
                                showBorder = false
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

                        // 거래 내역 리스트를 감싸는 GlassSurface
                        item {
                            if (monthlyData.isNotEmpty()) {
                                GlassSurface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    cornerRadius = 24f,
                                    showBorder = false
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        monthlyData.forEach { dailyLog ->
                                            // try-catch 밖에서 데이터 준비
                                            val date = try {
                                                if (dailyLog.day <= selectedYearMonth.lengthOfMonth()) {
                                                    LocalDate.of(selectedYearMonth.year, selectedYearMonth.month, dailyLog.day)
                                                } else {
                                                    Log.w("CalendarScreen", "Invalid day for ${selectedYearMonth.month}: ${dailyLog.day}")
                                                    null
                                                }
                                            } catch (e: Exception) {
                                                Log.e("CalendarScreen", "Error processing date: $e")
                                                null
                                            }

                                            // null이 아닌 경우에만 UI 표시
                                            date?.let { validDate ->
                                                if (dailyLog.plus > 0 || dailyLog.minus < 0) {
                                                    // 일자 헤더
                                                    Text(
                                                        text = "${validDate.dayOfMonth}일 ${validDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)}요일",
                                                        color = Color.White,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                                    )
                                                    
                                                    // 헤더와 거래 내역 사이 구분선
                                                    HorizontalDivider(
                                                        color = Color.White.copy(alpha = 0.2f),
                                                        thickness = 1.dp,
                                                        modifier = Modifier.padding(vertical = 8.dp)
                                                    )
                                                    
                                                    // 해당 날짜의 거래 내역 불러오기
                                                    val dayTransactions = viewModel.getTransactionsForDate(validDate)
                                                    
                                                    if (dayTransactions.isNotEmpty()) {
                                                        dayTransactions.forEachIndexed { index, transaction ->
                                                            DailyTransactionItem(
                                                                category = transaction.categoryName ?: "기타",
                                                                place = transaction.merchantName ?: "",
                                                                amount = transaction.amount,
                                                                paymentMethod = transaction.cardName ?: "",
                                                                dateTime = transaction.date
                                                            )
                                                            
                                                            if (index < dayTransactions.size - 1) {
                                                                HorizontalDivider(
                                                                    color = Color.White.copy(alpha = 0.1f),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(horizontal = 16.dp)
                                                                )
                                                            }
                                                        }
                                                    } else {
                                                        if (dailyLog.plus > 0) {
                                                            DailyTransactionItem(
                                                                category = "수입",
                                                                place = "입금",
                                                                amount = dailyLog.plus,
                                                                paymentMethod = "계좌이체",
                                                                dateTime = ""
                                                            )
                                                            
                                                            if (dailyLog.minus < 0) {
                                                                HorizontalDivider(
                                                                    color = Color.White.copy(alpha = 0.1f),
                                                                    thickness = 0.5.dp,
                                                                    modifier = Modifier.padding(horizontal = 16.dp)
                                                                )
                                                            }
                                                        }
                                                        
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
                                                    
                                                    // 날짜 사이의 구분선 (마지막 날짜가 아닌 경우에만)
                                                    if (dailyLog != monthlyData.last()) {
                                                        HorizontalDivider(
                                                            color = Color.White.copy(alpha = 0.2f),
                                                            thickness = 1.dp,
                                                            modifier = Modifier.padding(vertical = 16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
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
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowLeft,
                                        contentDescription = "이전 월",
                                        tint = if (canGoToPrevMonth) Color.White else Color.Gray,
                                        modifier = Modifier
                                            .alpha(if (canGoToPrevMonth) 1f else 0.5f)
                                            .size(42.dp)
                                            .clickable(enabled = canGoToPrevMonth) {
                                                viewModel.navigateToPreviousReportMonth()
                                            }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // 선택된 리포트 월 표시
                                    Text(
                                        text = "${viewModel.selectedReportYearMonth.monthValue}월",
                                        color = Color.White,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // 다음 월 버튼 (활성화 여부에 따라 스타일 변경)
                                    val canGoToNextMonth = viewModel.canNavigateToNextReportMonth()
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowRight,
                                        contentDescription = "다음 월",
                                        tint = if (canGoToNextMonth) Color.White else Color.Gray,
                                        modifier = Modifier
                                            .alpha(if (canGoToNextMonth) 1f else 0.5f)
                                            .size(42.dp)
                                            .clickable(enabled = canGoToNextMonth) {
                                                viewModel.navigateToNextReportMonth()
                                            }
                                    )
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    // 연도 표시 (오른쪽 끝)
                                Text(
                                        text = "${viewModel.selectedReportYearMonth.year}",
                                    color = Color.Gray,
                                        fontSize = 16.sp
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
                                    // 페이저로 변경된 리포트 UI
                                    ReportPager(
                                        reportData = reportData,
                                        cardReports = viewModel.cardReports.collectAsState().value,
                                        categoryReports = viewModel.categoryReports.collectAsState().value,
                                        modifier = Modifier.height(730.dp)
                                    )
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
                        color = if (isIncome) Color(0xFF00E1FF) else Color(0xFFFF00FF), // 색상 변경
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
                        color = Color(0xFF00E1FF), // 하늘색으로 변경
                        textAlign = TextAlign.Center
                    )
                }
                
                // 지출이 있으면 표시 (expense는 양수로 변환됨)
                if (summary.expense > 0) {
                    Text(
                        text = "-${formatAmount(summary.expense)}",
                        fontSize = 12.sp,
                        color = Color(0xFFFF00FF), // 분홍색으로 변경
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

@Composable
fun ReportPager(
    reportData: ReportData?,
    cardReports: List<CardReport>,
    categoryReports: List<CategoryReport>,
    modifier: Modifier = Modifier
) {
    // 현재 선택된 년월 정보 가져오기
    val viewModel: CalendarViewModel = viewModel()
    val selectedMonth = viewModel.selectedReportYearMonth.monthValue

    // 테스트 데이터 생성 (12, 1, 2월용)
    val testData = when (selectedMonth) {
        12 -> ReportData(
            totalSpendingAmount = -850000,
            preTotalSpendingAmount = -720000,
            totalBenefitAmount = 25000,
            totalGroupBenefitAverage = 0,
            groupName = null,
            reportDescription = "",
            consumptionPatterns = null
        )
        1 -> ReportData(
            totalSpendingAmount = -720000,
            preTotalSpendingAmount = -850000,
            totalBenefitAmount = 21000,
            totalGroupBenefitAverage = 0,
            groupName = null,
            reportDescription = "",
            consumptionPatterns = null
        )
        2 -> ReportData(
            totalSpendingAmount = -920000,
            preTotalSpendingAmount = -720000,
            totalBenefitAmount = 28000,
            totalGroupBenefitAverage = 0,
            groupName = null,
            reportDescription = "",
            consumptionPatterns = null
        )
        else -> null
    }

    // 데이터 유무 확인 (실제 데이터 또는 테스트 데이터)
    val effectiveReportData = testData ?: reportData
    val hasComparisonData = when (selectedMonth) {
        12, 1, 2 -> true  // 테스트 데이터가 있는 월
        else -> effectiveReportData != null && 
                (effectiveReportData.totalSpendingAmount != 0 || 
                 effectiveReportData.preTotalSpendingAmount != 0)
    }
    val hasConsumptionPattern = effectiveReportData?.consumptionPatterns != null
    
    // 페이지 개수 계산
    val pageCount = if (hasConsumptionPattern) {
        calculatePageCountWithPattern(hasComparisonData, cardReports, categoryReports)
    } else {
        calculatePageCountWithoutPattern(hasComparisonData, cardReports, categoryReports)
    }
    
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val currentPage = pagerState.currentPage
    
    Column(modifier = modifier.fillMaxWidth()) {
        // 페이지 인디케이터
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x66000000))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${currentPage + 1}/${pageCount}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 16.dp
        ) { page ->
            if (effectiveReportData != null && hasConsumptionPattern) {
                ReportPageWithPattern(page, effectiveReportData, hasComparisonData, cardReports, categoryReports)
            } else {
                ReportPageWithoutPattern(page, effectiveReportData, hasComparisonData, cardReports, categoryReports)
            }
        }
    }
}

// 소비 패턴이 있을 때 페이지 수 계산
fun calculatePageCountWithPattern(
    hasComparisonData: Boolean,
    cardReports: List<CardReport>, 
    categoryReports: List<CategoryReport>
): Int {
    // 기본 페이지 수 (총 소비/혜택 + 소비 유형)
    var count = 2
    
    // 월별 비교 페이지가 있는 경우 추가
    if (hasComparisonData) {
        count++
    }
    
    // 카드 페이지가 있는 경우 추가
    if (cardReports.isNotEmpty()) {
        count++
    }
    
    // 카테고리 페이지가 있는 경우 추가
    if (categoryReports.isNotEmpty()) {
        count++
    }
    
    return count
}

// 소비 패턴이 없을 때 페이지 수 계산
fun calculatePageCountWithoutPattern(
    hasComparisonData: Boolean,
    cardReports: List<CardReport>, 
    categoryReports: List<CategoryReport>
): Int {
    // 기본 페이지 수 (총 소비/혜택)
    var count = 1
    
    // 월별 비교 페이지가 있는 경우 추가
    if (hasComparisonData) {
        count++
    }
    
    // 카드 페이지가 있는 경우 추가
    if (cardReports.isNotEmpty()) {
        count++
    }
    
    // 카테고리 페이지가 있는 경우 추가
    if (categoryReports.isNotEmpty()) {
        count++
    }
    
    return count
}

// 소비 패턴이 있을 때 페이지 구성
@Composable
fun ReportPageWithPattern(
    page: Int,
    reportData: ReportData,
    hasComparisonData: Boolean,
    cardReports: List<CardReport>,
    categoryReports: List<CategoryReport>
) {
    when (page) {
        0 -> OverviewReportPage(reportData)
        1 -> ConsumptionTypePage(reportData)
        2 -> if (hasComparisonData) MonthlyComparisonPage(reportData) else {
            if (cardReports.isNotEmpty()) {
                CardReportPage(cardReports)
            } else if (categoryReports.isNotEmpty()) {
                CategoryReportPage(categoryReports)
            }
        }
        3 -> if (cardReports.isNotEmpty()) {
            CardReportPage(cardReports)
        } else if (categoryReports.isNotEmpty()) {
            CategoryReportPage(categoryReports)
        }
        4 -> if (categoryReports.isNotEmpty()) {
            CategoryReportPage(categoryReports)
        }
    }
}

// 소비 패턴이 없을 때 페이지 구성
@Composable
fun ReportPageWithoutPattern(
    page: Int,
    reportData: ReportData?,
    hasComparisonData: Boolean,
    cardReports: List<CardReport>,
    categoryReports: List<CategoryReport>
) {
    when (page) {
        0 -> OverviewReportPage(reportData)
        1 -> if (hasComparisonData && reportData != null) MonthlyComparisonPage(reportData) else {
            if (cardReports.isNotEmpty()) {
                CardReportPage(cardReports)
            } else if (categoryReports.isNotEmpty()) {
                CategoryReportPage(categoryReports)
            }
        }
        2 -> if (cardReports.isNotEmpty()) {
            CardReportPage(cardReports)
        } else if (categoryReports.isNotEmpty()) {
            CategoryReportPage(categoryReports)
        }
        3 -> if (categoryReports.isNotEmpty()) {
            CategoryReportPage(categoryReports)
        }
    }
}

// 빈 리포트 페이지
@Composable
fun EmptyReportPage(message: String = "리포트 데이터가 없습니다") {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .padding(vertical = 8.dp),
        cornerRadius = 24f
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 소비 유형 페이지 
@Composable
fun ConsumptionTypePage(reportData: ReportData) {
    // 현재 년월 정보 가져오기
    val viewModel: CalendarViewModel = viewModel()
    val selectedYearMonth = viewModel.selectedReportYearMonth
    
    // consumptionPatterns가 null이면 예외 발생할 수 있으므로 안전하게 처리
    val consumptionPattern = reportData.consumptionPatterns
    if (consumptionPattern == null) {
        EmptyReportPage("소비 패턴 정보가 없습니다")
        return
    }
    
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .padding(vertical = 8.dp),
        cornerRadius = 24f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 제목 - "YYYY년 MM월의 당신은..."
            Text(
                text = "${selectedYearMonth.year}년 ${selectedYearMonth.monthValue}월의 당신은...",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 소비 유형 이름 (큰 글씨로 중앙에 표시)
            Text(
                text = consumptionPattern.patternName,
                color = calendarBlue,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 이미지 (earth.png 사용)
            Image(
                painter = painterResource(id = R.drawable.earth),
                contentDescription = "소비 유형 이미지",
                modifier = Modifier
                    .size(180.dp)
                    .padding(vertical = 16.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 소비 패턴 설명 분리
            val descriptions = consumptionPattern.description.split("\n")
            
            if (descriptions.isNotEmpty()) {
                Text(
                    text = descriptions.getOrElse(0) { "" },
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            if (descriptions.size > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = descriptions.getOrElse(1) { "" },
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun OverviewReportPage(reportData: ReportData?) {
    // 현재 년월 정보 가져오기
    val viewModel: CalendarViewModel = viewModel()
    val selectedYearMonth = viewModel.selectedReportYearMonth
    
    // 현재 달인지 확인
    val currentYearMonth = YearMonth.now()
    val isCurrentMonth = selectedYearMonth.year == currentYearMonth.year && 
                        selectedYearMonth.monthValue == currentYearMonth.monthValue
    
    GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
            .height(600.dp)
            .padding(vertical = 8.dp),
        cornerRadius = 24f
        ) {
        Column(
                modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // 제목을 "YYYY년 MM월" 형식으로 변경
            Text(
                text = "${selectedYearMonth.year}년 ${selectedYearMonth.monthValue}월",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            if (reportData == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "리포트 데이터가 없습니다",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            } else {
                // 총 소비 금액 (음수로 오면 양수로 변환)
                val totalSpending = if (reportData.totalSpendingAmount < 0) 
                    -reportData.totalSpendingAmount else reportData.totalSpendingAmount
                
                // 비율 계산 - 전체 범위에서 사용할 수 있도록 여기서 정의
                val benefitRatio = if (totalSpending > 0) {
                    (reportData.totalBenefitAmount.toFloat() / totalSpending) * 100f
                } else {
                    0f
                }
                
                // 혜택이 높은지 낮은지 상대적인 평가
                val benefitQuality = when {
                    benefitRatio >= 5.0f -> "매우 높은"
                    benefitRatio >= 3.0f -> "높은"
                    benefitRatio >= 1.0f -> "보통의"
                    else -> "낮은"
                }
                
                // 개요 레이아웃 (큰 금액 표시)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "총 소비 금액",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (totalSpending > 0) "-${formatAmount(totalSpending)}원" else "0원",
                        color = brightRed,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "혜택 금액",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "+${formatAmount(reportData.totalBenefitAmount)}원",
                        color = calendarBlue,
                        fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
                Spacer(modifier = Modifier.height(24.dp))
            
                // 금액/혜택 비율 그래프 추가
            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                        text = "소비 대비 혜택 비율",
                    color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // 그래프 컨테이너
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FFFFFF))
                    ) {
                        // 혜택 부분 (최대 100%까지)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(minOf(benefitRatio / 100f, 1f))
                                .background(calendarBlue)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 비율 텍스트 표시 (소비금액이 0인 경우 처리)
                    Text(
                        text = if (totalSpending > 0) String.format("%.1f%%", benefitRatio) else "소비 없음",
                        color = calendarBlue,
                    fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                )
                
                    // 추가 설명 (소비금액이 0인 경우 처리)
                    if (totalSpending > 0) {
                Text(
                            text = "100원 소비 시 ${String.format("%.1f", benefitRatio)}원 혜택",
                    color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            text = "아직 소비 내역이 없습니다",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 요약 정보 (소비 패턴 대신 간단한 설명 추가)
            Text(
                    text = "이번 달 혜택",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (totalSpending > 0) {
                    Text(
                        text = "이번 달은 소비 대비 $benefitQuality 혜택을 ${if(isCurrentMonth) "받고 있습니다" else "받았습니다"}.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 18.sp,
                        lineHeight = 26.sp
                    )
                    
                    // 평가 기준을 한 줄로 표시
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "혜택 기준: 매우 높음 5%+, 높음 3%+, 보통 1%+, 낮음 1% 미만",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                } else {
                    Text(
                        text = "아직 소비 기록이 없어 혜택 정보를 분석할 수 없습니다.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                }
                
                // 소비 패턴 정보 표시
                reportData.consumptionPatterns?.let { pattern ->
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    Text(
                        text = "소비 패턴",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = pattern.patternName,
                        color = calendarBlue,
                        fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = pattern.description,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CardReportPage(cardReports: List<CardReport>) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .padding(vertical = 8.dp),
        cornerRadius = 24f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "카드별 소비 및 혜택",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // LazyColumn 대신 Column 사용하여 스크롤 방지
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 최대 3개 카드만 표시
                cardReports.take(3).forEach { card ->
                    CardReportItemView(card)
                }
                
                // 추가 카드가 있는 경우 메시지 표시
                if (cardReports.size > 3) {
                    Text(
                        text = "외 ${cardReports.size - 3}개 카드 사용",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CardReportItemView(card: CardReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x66333333)
        ),
        shape = RoundedCornerShape(16.dp)
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
                    fontSize = 20.sp,
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "혜택 ${formatAmount(totalBenefit)}원",
                        color = Color(0xFF4CAF50),
                        fontSize = 16.sp
                    )
                }
            }
            
            // 구분선
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // 카테고리별 내역
            card.categories.forEach { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${category.category} (${category.count}회)",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    
                    Row {
                        // amount가 음수로 올 수 있으므로 절대값으로 표시
                        val displayAmount = if (category.amount < 0) -category.amount else category.amount
                        
                        Text(
                            text = "${formatAmount(displayAmount)}원",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        
                        if (category.benefit > 0) {
                            Text(
                                text = " (${formatAmount(category.benefit)}원)",
                                color = Color(0xFF4CAF50),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryReportPage(categoryReports: List<CategoryReport>) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .padding(vertical = 8.dp),
        cornerRadius = 24f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "카테고리별 소비 및 혜택",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (categoryReports.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "카테고리 사용 내역이 없습니다",
                        color = Color.Gray,
                        fontSize = 18.sp
                    )
                }
            } else {
                // 카테고리별 차트 (간단한 막대형)
                val maxAmount = categoryReports.maxOfOrNull { it.getAbsoluteAmount() } ?: 1
                
                // LazyColumn 대신 Column 사용하여 스크롤 방지
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 상위 5개 카테고리만 표시
                    categoryReports
                        .sortedByDescending { it.getAbsoluteAmount() }
                        .take(5)
                        .forEach { category ->
                            CategoryReportItemView(category, maxAmount)
                        }
                    
                    // 추가 카테고리가 있는 경우 메시지 표시
                    if (categoryReports.size > 5) {
                        Text(
                            text = "외 ${categoryReports.size - 5}개 카테고리 사용",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryReportItemView(category: CategoryReport, maxAmount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Row {
                // amount가 음수로 올 수 있으므로 절대값으로 표시
                val displayAmount = category.getAbsoluteAmount()
                
                Text(
                    text = "${formatAmount(displayAmount)}원",
                    color = Color.White,
                    fontSize = 16.sp
                )
                
                if (category.benefit > 0) {
                    Text(
                        text = " (${formatAmount(category.benefit)}원)",
                        color = Color(0xFF4CAF50),
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 막대 그래프
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(category.getAbsoluteAmount().toFloat() / maxAmount)
                    .background(calendarBlue)
            )
        }
    }
}

// 전월 대비 소비 비교 페이지
@Composable
fun MonthlyComparisonPage(reportData: ReportData) {
    // reportData가 null이거나 데이터가 없으면 표시하지 않음
    if (reportData.totalSpendingAmount == 0 && reportData.preTotalSpendingAmount == 0) {
        return
    }
    
    // 현재 선택된 년월 정보 가져오기
    val viewModel: CalendarViewModel = viewModel()
    val selectedYearMonth = viewModel.selectedReportYearMonth
    
    // 현재 월과 이전 월 계산
    val currentMonth = selectedYearMonth.monthValue
    val previousMonth = if (currentMonth > 1) currentMonth - 1 else 12

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .padding(vertical = 8.dp),
        cornerRadius = 24f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // 타이틀
            Text(
                text = "월별 소비 비교",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 소비 금액 (음수로 오면 양수로 변환)
            val currentMonthSpending = if (reportData.totalSpendingAmount < 0) 
                -reportData.totalSpendingAmount else reportData.totalSpendingAmount
            val previousMonthSpending = if (reportData.preTotalSpendingAmount < 0) 
                -reportData.preTotalSpendingAmount else reportData.preTotalSpendingAmount
            
            // 최대값 계산 (그래프 스케일링을 위해)
            val maxValue = maxOf(currentMonthSpending, previousMonthSpending).toFloat()
            
            // 그래프 높이 계산 (최대값을 기준으로 스케일링)
            val currentHeight = if (maxValue > 0) (currentMonthSpending.toFloat() / maxValue) * 200 else 0f
            val previousHeight = if (maxValue > 0) (previousMonthSpending.toFloat() / maxValue) * 200 else 0f
            
            // 그래프 컨테이너
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 이전 달 바
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = previousMonth.toString() + "월",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(previousHeight.dp)
                            .background(
                                color = Color.Gray,
                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 소비금액 표시
                    Text(
                        text = formatAmount(previousMonthSpending),
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // 현재 달 바
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentMonth.toString() + "월",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 현재 달 소비액을 원 배경과 함께 표시
                    Box(
                        contentAlignment = Alignment.TopCenter,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        // 바 그래프
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(currentHeight.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            calendarBlue,  // 위쪽 색상
                                            Color(0xFF5D9CEC)   // 아래쪽 색상
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                        )
                        
                        // 현재 달 소비액 원형 배경
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .offset(y = (-20).dp)
                                .size(80.dp)
                                .background(
                                    color = Color(0x33FFFFFF),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = calendarBlue,
                                    shape = CircleShape
                                )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = formatAmount(currentMonthSpending),
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 비교 텍스트
            val diff = currentMonthSpending - previousMonthSpending
            val diffPercentage = if (previousMonthSpending > 0) {
                diff.toFloat() / previousMonthSpending.toFloat() * 100
            } else if (currentMonthSpending > 0) {
                100f  // 이전달이 0이고 현재달이 있으면 100% 증가
            } else {
                0f  // 두 달 모두 0이면 변화 없음
            }
            
            val comparisonText = when {
                previousMonthSpending == 0 && currentMonthSpending > 0 -> "이전 달 대비 소비가 발생했습니다."
                diff > 0 -> "지난 달보다 ${formatAmount(diff)}원 (${String.format("%.1f", diffPercentage)}%) 더 소비했습니다."
                diff < 0 -> "지난 달보다 ${formatAmount(-diff)}원 (${String.format("%.1f", -diffPercentage)}%) 덜 소비했습니다."
                else -> "지난 달과 소비가 동일합니다."
            }
            
            Text(
                text = comparisonText,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 