package com.example.fe.ui.screens.calendar

import android.R.color.white
import android.annotation.SuppressLint
import android.content.Context
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
import com.example.fe.data.model.calendar.TransactionData
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fe.ui.components.categoryIcons.getCategoryIcon
import com.example.fe.ui.screens.calendar.white
import java.text.NumberFormat
import kotlin.math.abs

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



// 폰트 사이즈 정의
val big = 24
val title = 20
val middle = 19
val small = 17
val verysmall = 14


// 색상 정의
val white = Color(0xFFFFFFFF)
val brightRed = Color(0xD2FF7777)
val brightGreen = Color(0xFF69F0AE)
val calendarlightBlue = Color(0xFF72909A)
val calendarBlue = Color(0xFF00BCD4) // 네온 블루
val calenderGray = Color(0xFFA9A6A6) // 빛나는 노란색

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
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModelFactory(LocalContext.current))
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
            starCount = 50,
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
                
                // 탭 인덱스에 따라 다른 내용 표시
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
                                        .size(30.dp)
                                        .clickable {
                                            viewModel.navigateToPreviousMonth()
                                        }
                                )

                                
                                Spacer(modifier = Modifier.width(10.dp))
                                
                                // 월 표시
                                Text(
                                    text = "${selectedYearMonth.monthValue}월",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Light
                                )
                                        
                                Spacer(modifier = Modifier.width(10.dp))
                                
                                // 오른쪽 화살표 (>)
                                // 현재 월이면 비활성화된 스타일로 보여주기
                                val isCurrentMonth = selectedYearMonth == currentMonth
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowRight,
                                    contentDescription = "다음 달",
                                    tint = if (isCurrentMonth) Color.Gray else Color.White,
                                    modifier = Modifier
                                        .alpha(if (isCurrentMonth) 0.5f else 1f)
                                        .size(30.dp)
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
                                    fontSize = small.sp
                                )

                            }

                        }

                        // 당월 소비 현황
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            monthlyInfo?.let { info ->
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
//                                        Text(
//                                            text = "당월 소비 현황",
//                                            color = Color.White,
//                                            fontSize = 20.sp,
//                                            fontWeight = FontWeight.Bold,
//                                            modifier = Modifier.padding(bottom = 16.dp)
//                                        )
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(
                                                    style = SpanStyle(
                                                        color = calendarBlue,
                                                        fontSize = title.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                ) {
                                                    append("${formatAmount(abs(info.totalSpendingAmount))}원 ")
                                                }
                                                withStyle(
                                                    style = SpanStyle(
                                                        color = Color.White,
                                                        fontSize =title.sp,
                                                        fontWeight = FontWeight.Light
                                                    )
                                                ) {
                                                    append("소비")
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(
                                                    style = SpanStyle(
                                                        color = Color.White,
                                                        fontSize = title.sp,
                                                        fontWeight = FontWeight.Light
                                                    )
                                                ) {
                                                    append("최다 소비 ")
                                                }
                                                withStyle(
                                                    style = SpanStyle(
                                                        color = Color.White,
                                                        fontSize = title.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                ) {
                                                    append("${info.categoryName}")
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))


                                        Text(


                                            text = buildAnnotatedString {
                                                withStyle(
                                                    style = SpanStyle(
                                                        color = Color.White,
                                                        fontSize = title.sp,
                                                        fontWeight = FontWeight.Light
                                                    )
                                                ) {
                                                    append("지난달보다 ")
                                                }
                                                withStyle(
                                                    style = SpanStyle(
                                                        color = Color.White,
                                                        fontSize = title.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                ) {
                                                    append("${formatAmount(abs(info.monthlyDifferenceAmount))}원")
                                                }
                                                withStyle(
                                                    style = SpanStyle(
                                                        color = Color.White,
                                                        fontSize = title.sp,
                                                        fontWeight = FontWeight.Light
                                                    )
                                                ) {
                                                    if (info.monthlyDifferenceAmount > 0) {
                                                        append(" 더 소비 중")
                                                    } else {
                                                        append(" 더 절약 중")
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        
                        // 요일 헤더 (GlassSurface 바깥)
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
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
                                            color = calenderGray,
                                            fontSize = 15.sp,
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
                                    .padding(vertical = 10.dp),
                                cornerRadius = 10f
                            ) {
                                // 달력 그리드만 표시
                                Box(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    if (isLoading) {
                                        // 로딩 상태 표시
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

                        // 당월 소비 현황
//                        item {
//                            Spacer(modifier = Modifier.height(20.dp))
//                            monthlyInfo?.let { info ->
//                                Box(
//                                    modifier = Modifier.fillMaxWidth()
//                                ) {
//                                    Column(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(16.dp)
//                                    ) {
////                                        Text(
////                                            text = "당월 소비 현황",
////                                            color = Color.White,
////                                            fontSize = 20.sp,
////                                            fontWeight = FontWeight.Bold,
////                                            modifier = Modifier.padding(bottom = 16.dp)
////                                        )
//                                        Text(
//                                            text = buildAnnotatedString {
//                                                withStyle(
//                                                    style = SpanStyle(
//                                                        color = calendarBlue,
//                                                        fontSize = 18.sp,
//                                                        fontWeight = FontWeight.ExtraBold
//                                                    )
//                                                ) {
//                                                    append("${formatAmount(abs(info.totalSpendingAmount))}원 ")
//                                                }
//                                                withStyle(
//                                                    style = SpanStyle(
//                                                        color = Color.White,
//                                                        fontSize = 18.sp,
//                                                        fontWeight = FontWeight.Light
//                                                    )
//                                                ) {
//                                                    append("소비")
//                                                }
//                                            }
//                                        )
//                                        Spacer(modifier = Modifier.height(8.dp))
//
//                                        Text(
//                                            text = buildAnnotatedString {
//                                                withStyle(
//                                                    style = SpanStyle(
//                                                        color = Color.White,
//                                                        fontSize = 18.sp,
//                                                        fontWeight = FontWeight.Light
//                                                    )
//                                                ) {
//                                                    append("최다 소비 ")
//                                                }
//                                                withStyle(
//                                                    style = SpanStyle(
//                                                        color = Color.White,
//                                                        fontSize = 18.sp,
//                                                        fontWeight = FontWeight.ExtraBold
//                                                    )
//                                                ) {
//                                                    append("${info.categoryName}")
//                                                }
//                                            }
//                                        )
//                                        Spacer(modifier = Modifier.height(8.dp))
//
//
//                                        Text(
//
//
//                                            text = buildAnnotatedString {
//                                                withStyle(
//                                                    style = SpanStyle(
//                                                        color = Color.White,
//                                                        fontSize = 18.sp,
//                                                        fontWeight = FontWeight.Light
//                                                    )
//                                                ) {
//                                                    append("지난달보다 ")
//                                                }
//                                                withStyle(
//                                                    style = SpanStyle(
//                                                        color = Color.White,
//                                                        fontSize = 18.sp,
//                                                        fontWeight = FontWeight.ExtraBold
//                                                    )
//                                                ) {
//                                                    append("${formatAmount(abs(info.monthlyDifferenceAmount))}원")
//                                                }
//                                                withStyle(
//                                                    style = SpanStyle(
//                                                        color = Color.White,
//                                                        fontSize = 20.sp,
//                                                        fontWeight = FontWeight.Light
//                                                    )
//                                                ) {
//                                                    if (info.monthlyDifferenceAmount > 0) {
//                                                        append(" 더 소비 중")
//                                                    } else {
//                                                        append(" 더 절약 중")
//                                                    }
//                                                }
//                                            }
//                                        )
//                                    }
//                                }
//                            }
//                        }

                        // 월별 소비 기록
                        item {
                            Spacer(modifier = Modifier.height(5.dp))
                            GlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                cornerRadius = 10f
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(30.dp)
                                ) {
//                                    Text(
//                                        text = "월별 소비 기록",
//                                        color = Color.White,
//                                        fontSize = 20.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        modifier = Modifier.padding(bottom = 16.dp)
//                                    )

                                    if (transactions.isEmpty()) {
                                        Text(
                                            text = "거래 내역이 없습니다",
                                            color = Color.Gray,
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(vertical = 16.dp)
                                        )
                                    } else {
                                        var currentDate: String? = null
                                        transactions.forEach { transaction ->
                                            val transactionDate = transaction.date.substring(0, 10)
                                            
                                            if (currentDate != transactionDate) {
                                                if (currentDate != null) {
                                                    Divider(
                                                        color = Color.White.copy(alpha = 0.2f),
                                                        modifier = Modifier.padding(vertical = 10.dp)
                                                    )
                                                }
                                                currentDate = transactionDate
                                                
                                                val date = LocalDate.parse(transactionDate)

                                                Text(
                                                    text = "${date.monthValue}월 ${date.dayOfMonth}일",
                                                    color = Color.White,
                                                    fontSize = small.sp,
                                                    modifier = Modifier.padding(vertical = 10.dp)
                                                )
                                            }
                                            
                                            DailyTransactionItem(transaction = transaction)
                                        }
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
                                        containerColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = "데이터 로드 중 오류가 발생했습니다: ${error}",
                                        color = Color.White,
                                        fontSize = small.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                        
                        // 하단 여백
                        item {
                            Spacer(modifier = Modifier.height(40.dp))
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
                                    // 왼쪽 화살표 (<)
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowLeft,
                                        contentDescription = "이전 달",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clickable {
                                                viewModel.navigateToPreviousMonth()
                                            }
                                    )


                                    Spacer(modifier = Modifier.width(10.dp))

                                    // 월 표시
                                    Text(
                                        text = "${selectedYearMonth.monthValue}월",
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Light
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // 오른쪽 화살표 (>)
                                    // 현재 월이면 비활성화된 스타일로 보여주기
                                    val isCurrentMonth = selectedYearMonth == currentMonth
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowRight,
                                        contentDescription = "다음 달",
                                        tint = if (isCurrentMonth) Color.Gray else Color.White,
                                        modifier = Modifier
                                            .alpha(if (isCurrentMonth) 0.5f else 1f)
                                            .size(30.dp)
                                            .clickable(enabled = !isCurrentMonth) {
                                                if (!isCurrentMonth) {
                                                    viewModel.navigateToNextMonth()
                                                }
                                            }
                                    )


                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    // 연도 표시 (오른쪽 끝)
                                Text(
                                    text = "${viewModel.selectedReportYearMonth.year}",
                                    color = Color.Gray,
                                        fontSize =small.sp
                                    )

                                }
                                val isCurrentMonth = selectedYearMonth == currentMonth

                                // 현재 달인 경우 "말일에 리포트 생성 예정입니다" 메시지만 표시
                                if (isCurrentMonth) {
                                    GlassSurface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .padding(vertical = 8.dp),
                                        cornerRadius = 15f
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "열심히 소비 패턴을 분석하고 있습니다",
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "다음달 1일에 만나요",
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }else {
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
            }}
            
            // 위로 가기 버튼
            AnimatedVisibility(
                visible = showScrollToTopButton,
                enter = fadeIn() + slideInVertically { it * 2 },
                exit = fadeOut() + slideOutVertically { it * 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                // 내용은 동일하게 유지
            }
        }
    }
}

// API 데이터에 대한 일별 거래 항목 표시
@Composable
fun DailyTransactionItem(
    transaction: TransactionData
) {
    val category = transaction.categoryName ?: "기타"
    val place = transaction.merchantName ?: ""
    val amount = transaction.amount
    val paymentMethod = transaction.cardName ?: ""
    val dateTime = transaction.date

    val isIncome = amount > 0
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 카테고리
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(Color(0xFF00BCD4), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = getCategoryIcon(category)),
                    contentDescription = category,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category,
                color = Color.White,
                fontSize = small.sp,
                fontWeight = FontWeight.Light,
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
                        color = if (isIncome) calendarBlue else  brightRed,
                        fontSize = small.sp,
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
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 상점 이름과 카드 이름을 함께 표시 (있는 경우에만)
                val placeText = if (place.isNotBlank() && place != " "&& place !="입금") place else ""
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
                        fontSize = verysmall.sp
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

        // 마지막 주의 종료일 (다음 달의 날짜 포함하되 정확히 해당 월 마지막 주까지만)
        val end = if (lastDay.dayOfWeek.value == 7) {
            // 마지막 날이 토요일인 경우, 다음 주를 포함하지 않음
            lastDay
        } else {
            // 마지막 주의 토요일까지만 포함
            lastDay.plusDays((7 - lastDay.dayOfWeek.value).toLong())
        }

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
                        .padding(vertical = 5.dp)
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

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                if (isSelected) Color(0x33FFFFFF)
                else Color.Transparent
            )
            .clickable(enabled = isCurrentMonth) {
                onDateSelected(date)
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize() // 전체적으로 약간 패딩
        ) {
            // 날짜 표시 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(17.dp), // 날짜 영역 살짝 줄임
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = small.sp,
                    fontWeight = if (isToday || isSelected) FontWeight.Light else FontWeight.Normal,
                    color = if (!isCurrentMonth) calendarlightBlue else Color.White
                )
            }

            // 수입/지출 요약 정보 표시 (더 넉넉한 영역 확보)
            if (isCurrentMonth && summary != null) {
                Spacer(modifier = Modifier.height(2.dp))

                // 수입
                if (summary.income > 0) {
                    val incomeText = "+${formatAmount(summary.income)}"
                    val incomeFontSize = when {
                        incomeText.length >= 13 -> (verysmall - 6).sp // 예: +1,000,000
                        incomeText.length >= 10 -> (verysmall - 5).sp
                        incomeText.length >= 8 -> (verysmall - 4).sp
                        else -> (verysmall - 3).sp
                    }

                    Text(
                        text = incomeText,
                        fontSize = incomeFontSize,
                        fontWeight = FontWeight.Light,
                        color = calendarBlue,
                        overflow = TextOverflow.Clip,
                        maxLines = 1, //
                        softWrap = false, //
                        modifier = Modifier
                            .padding(horizontal = 1.dp)
                            .fillMaxWidth()
                    )
                }

                // 지출
                if (summary.expense > 0) {

                    val expenseText = "-${formatAmount(summary.expense)}"
                    val expenseFontSize = when {
                        expenseText.length >= 13 -> (verysmall - 6).sp
                        expenseText.length >= 10 -> (verysmall - 5).sp
                        expenseText.length >= 8 -> (verysmall - 4).sp
                        else -> (verysmall - 3).sp
                    }

                    Text(
                        text = expenseText,
                        fontSize = expenseFontSize,
                        fontWeight = FontWeight.Light,
                        color = brightRed,
                        overflow = TextOverflow.Clip,
                        maxLines = 1, //
                        softWrap = false, //
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

// 큰 금액도 깨지지 않도록 축약형으로 표시하는 함수
fun formatAmountCompact(amount: Int): String {
    return when {
        amount >= 100_000_000 -> String.format("%.1f억", amount / 100_000_000.0)
        amount >= 10_000_000 -> String.format("%.1f천만", amount / 10_000_000.0)
        amount >= 1_000_000 -> String.format("%.1f백만", amount / 1_000_000.0)
        amount >= 100_000 -> String.format("%.1f십만", amount / 100_000.0)
        amount >= 10_000 -> String.format("%d만", amount / 10_000)
        else -> NumberFormat.getNumberInstance().format(amount)
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

    // 스크롤 상태 추적을 위한 LazyListState 추가
    val listState = rememberLazyListState()

    // 스크롤 오프셋 계산 및 콜백 호출
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex * 1000f + listState.firstVisibleItemScrollOffset
        }.collect { offset ->
            // 스크롤 오프셋 전달
            viewModel.updateReportScrollOffset(offset)
        }
    }

    // 데이터 유무 확인 - 테스트 데이터 없이 실제 데이터만 사용
    val hasComparisonData = reportData != null &&
            (reportData.totalSpendingAmount != 0 ||
                    reportData.preTotalSpendingAmount != 0)

    val hasMyGroupComparisonData = reportData != null

    val hasConsumptionPattern = reportData?.consumptionPatterns != null

    // 수직 스크롤 가능한 Column으로 변경 (LazyListState 설정)
    LazyColumn(
        state = listState, // 스크롤 상태 추적을 위한 state 설정
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 소비 패턴 페이지 (있는 경우에만 표시) - 첫 번째로 이동
        if (hasConsumptionPattern && reportData != null) {
            item {
                ConsumptionTypePage(reportData)
            }
        }

        // 2. 개요 페이지 (항상 표시) - 두 번째로 이동
        item {
            OverviewReportPage(reportData)
        }

        // 3. 월별 비교 페이지 (비교 데이터가 있는 경우에만 표시)
        if (hasComparisonData && reportData != null) {
            item {
                MonthlyComparisonPage(reportData)
            }
        }

        // 3-2. 내 소비 그룹 비교 페이지 (비교 데이터가 있는 경우에만 표시)
        if (hasMyGroupComparisonData && reportData != null) {
            item {
                GroupBenefitComparisonPage(reportData)
            }
        }

        // 4. 카드별 리포트 페이지 (카드 데이터가 있는 경우에만 표시)
    if (cardReports.isNotEmpty()) {
            item {
                CardReportPage(cardReports)
            }
        }

        // 5. 카테고리별 리포트 페이지 (카테고리 데이터가 있는 경우에만 표시)
        if (categoryReports.isNotEmpty()) {
            item {
                CategoryReportPage(categoryReports)
            }
        }

        // 하단 여백
        item {
            Spacer(modifier = Modifier.height(32.dp))
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
                .wrapContentHeight(align = Alignment.Top)
                .padding(vertical = 8.dp),
        cornerRadius = 15f
        ) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
        ) {
            if (reportData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "리포트 데이터가 없습니다",
                        color = Color.Gray,
                        fontSize = title.sp
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
                
                // 혜택이 높은지 낮은지 상대적인 평가 (계산을 위해 유지)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "총 소비 금액",
                        color = Color.White,
                        fontSize = middle.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (totalSpending > 0) "${formatAmount(totalSpending)}원" else "0원",
                        color = brightRed,
                        fontSize = big.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "혜택 금액",
                        color = Color.White,
                        fontSize = middle.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${formatAmount(reportData.totalBenefitAmount)}원",
                        color = calendarBlue,
                        fontSize = big.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            

//
//                // 금액/혜택 비율 그래프 추가
//            Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(
//                        text = "소비 대비 혜택 비율",
//                    color = Color.White,
//                        fontSize = middle.sp,
//                        modifier = Modifier.padding(bottom = 12.dp)
//                    )
//
//                    // 그래프 컨테이너
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(24.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                            .background(Color(0x33FFFFFF))
//                    ) {
//                        // 혜택 부분 (최대 100%까지)
//                        Box(
//                            modifier = Modifier
//                                .fillMaxHeight()
//                                .fillMaxWidth(minOf(benefitRatio / 100f, 1f))
//                                .background(calendarBlue)
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // 비율 텍스트 표시 (소비금액이 0인 경우 처리)
//                    Text(
//                        text = if (totalSpending > 0) String.format("%.1f%%", benefitRatio) else "소비 없음",
//                        color = calendarBlue,
//                    fontSize = small.sp,
//                        fontWeight = FontWeight.Bold
//                )
//
//                    // 추가 설명 (소비금액이 0인 경우 처리)
//                    if (totalSpending > 0) {
//                Text(
//                            text = "100원 소비 시 ${String.format("%.1f", benefitRatio)}원 혜택",
//                    color = Color.White.copy(alpha = 0.7f),
//                            fontSize = small.sp,
//                            modifier = Modifier.padding(top = 4.dp)
//                        )
//                    } else {
//                        Text(
//                            text = "아직 소비 내역이 없습니다",
//                            color = Color.White.copy(alpha = 0.7f),
//                            fontSize = title.sp,
//                            modifier = Modifier.padding(top = 4.dp)
//                        )
//                    }
//                }
                
                // 하단 여백
            }
        }
    }
}

@Composable
fun CardReportPage(cardReports: List<CardReport>) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .padding(vertical = 8.dp), // heightIn(min = 400.dp) 제거
        cornerRadius = 15f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(15.dp))
            // 타이틀
            Text(
                text = "카드별 소비 및 혜택",
                color = Color.White,
                fontSize = title.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(5.dp))


            if (cardReports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp), // 높이 줄임 (200dp → 120dp)
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "카드 사용 내역이 없습니다",
                        color = Color.Gray,
                        fontSize = title.sp
                    )
                }
            } else {
                // 카드 리스트
            Column(
                    modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 최대 3개 카드만 표시
                cardReports.take(cardReports.size).forEach { card ->
                    CardReportItemView(card)
                }
                
                // 추가 카드가 있는 경우 메시지 표시
//                if (cardReports.size > 3) {
//                    Text(
//                        text = "외 ${cardReports.size - 3}개 카드 사용",
//                        color = Color.Gray,
//                        fontSize = 16.sp,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier.fillMaxWidth()
//                    )
//                    }
                }
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun CardReportItemView(card: CardReport) {
    // 카드 데이터 계산
    val totalAmount = card.getCalculatedTotalAmount()
    val totalBenefit = card.getCalculatedTotalBenefit()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color(0x33000000),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
        // 카드 이름 (중앙 정렬)

            val imageUrl = card.cardImg

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "카드 이미지",
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp, bottom = 4.dp),
                contentScale = ContentScale.Fit
            )

        Text(
            text = card.name,
            color = Color.White,
            fontSize = middle.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // 총 소비 행
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                text = "총소비",
                    color = Color.White,
                fontSize = middle.sp
            )
                    
            Text(
                text = "${formatAmount(totalAmount)}원",
                color = Color.White,
                fontSize = middle.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 받은 혜택 행
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
                    Text(
                text = "받은혜택",
                color = calendarBlue,
                fontSize = middle.sp
            )

            Text(
                text = "${formatAmount(totalBenefit)}원",
                color = calendarBlue,
                fontSize = middle.sp,
                fontWeight = FontWeight.Bold
            )
            }
            
            // 구분선
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.2f),
                thickness = 1.dp,
            modifier = Modifier.padding(vertical = 12.dp)
            )
            
            // 카테고리별 내역
            card.categories.forEach { category ->
            // amount가 음수로 올 수 있으므로 절대값으로 표시
            val displayAmount = if (category.amount < 0) -category.amount else category.amount

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                // 카테고리명과 이용 횟수
                    Text(
                    text = "${category.category}(${category.count}회)",
                        color = Color.White,
                    fontSize = small.sp
                    )
                    
                // 소비금액과 혜택금액 (한 줄에 표시)
                        Text(
                    text = buildAnnotatedString {
                        // 소비 금액 부분 (흰색으로 명시적 지정)
                        withStyle(SpanStyle(color = Color.White)) {
                            append("${formatAmount(displayAmount)}원")
                        }

                        // 혜택 금액 부분 (하늘색)
                        withStyle(SpanStyle(color = calendarBlue)) {
                            append("(${formatAmount(category.benefit)}원)")
                        }
                    },
                    fontSize = small.sp,
                    // 기본 텍스트 색상도 흰색으로 명시
                    color = Color.White
                )
            }
        }
    }
}
@Composable
fun CategoryReportPage(categoryReports: List<CategoryReport>) {
    // 금액이 큰 순서대로 카테고리 정렬
    val sortedCategoryReports = categoryReports.sortedByDescending { it.getAbsoluteAmount() }

    // 상위 5개 카테고리와 "그 외" 카테고리로 분리
    val topCategories = sortedCategoryReports.take(5)
    val otherCategories = sortedCategoryReports.drop(5)

    // "그 외" 카테고리 생성 (나머지 카테고리들의 합계)
    val otherCategory = if (otherCategories.isNotEmpty()) {
        val totalAmount = otherCategories.sumOf { it.getAbsoluteAmount() }
        val totalBenefit = otherCategories.sumOf { it.benefit }
        // CategoryReport 클래스의 생성자에 맞게 수정해야 할 수 있습니다
        CategoryReport(
            category = "그 외",
            amount = totalAmount,
            benefit = totalBenefit
        )
    } else null

    // 표시할 최종 카테고리 리스트 (상위 5개 + 그 외)
    val displayCategories = if (otherCategory != null) {
        topCategories + otherCategory
    } else {
        topCategories
    }

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .padding(vertical = 8.dp),
        cornerRadius = 15f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 타이틀
            Text(
                text = "카테고리별 소비",
                color = Color.White,
                fontSize = title.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            if (displayCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "카테고리 사용 내역이 없습니다",
                        color = Color.Gray,
                        fontSize = title.sp
                    )
                }
            } else {
                // 카테고리별 색상 정의 (5개 주요 카테고리 + 그 외)
                val categoryColors = listOf(
                    Color(0xFF2196F3), // 파란색
                    Color(0xFF74C2FF), // 연한 파란색
                    Color(0xFF00E676), // 초록색
                    Color(0xBA00FFF7), // 청록색
                    Color(0xFFFF93BA), // 분홍색
                    Color(0xFFAAAAAA)  // 회색 (그 외 카테고리용)
                )

                // 카테고리별 금액 데이터 준비
                val categoryAmounts = displayCategories.map {
                    it.getAbsoluteAmount().toFloat()
                }

                // 도넛 그래프와 범례 영역
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 도넛 그래프
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        AccountBookGraph(
                            modifier = Modifier.fillMaxSize(),
                            colors = categoryColors.take(displayCategories.size),
                            data = categoryAmounts,
                            graphHeight = 180
                        )
                    }

                    // 범례
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 32.dp)
                    ) {
                        displayCategories.forEachIndexed { index, category ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                // 색상 표시
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            color = categoryColors[index],
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                // 카테고리 이름
                                Text(
                                    text = category.category,
                                    color = Color.White,
                                    fontSize = middle.sp
                                )
                            }
                        }
                    }
                }

                // 카테고리별 상세 내역
                displayCategories.forEachIndexed { index, category ->
                    // 카테고리 행
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 카테고리 이름
                        Text(
                            text = category.category,
                            color = Color.White,
                            fontSize = middle.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // 금액 및 혜택
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            // 금액
                            Text(
                                text = "${formatAmount(category.getAbsoluteAmount())}원",
                                color = Color.White,
                                fontSize = middle.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // 혜택 금액
                            Text(
                                text = "${formatAmount(category.benefit)}원 혜택",
                                color = calendarBlue,
                                fontSize = small.sp
                            )
                        }
                    }

                    // 마지막 항목이 아니면 구분선 추가
                    if (index < displayCategories.size - 1) {
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

// 소비 구간 대비 소비 비교 페이지
@Composable
fun GroupBenefitComparisonPage(reportData: ReportData) {
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
            .wrapContentHeight(align = Alignment.Top)
            .heightIn(min = 400.dp)
            .padding(vertical = 8.dp),
        cornerRadius = 15f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(15.dp))
            // 타이틀 - 중앙정렬로 변경 및 크기 증가
            Text(
                text = "나의 소비그룹 혜택 비교",
                color = Color.White,
                fontSize = title.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            // 혜택 금액 (음수로 오면 양수로 변환)
            val myMonthBenefit = if (reportData.totalBenefitAmount < 0)
                -reportData.totalBenefitAmount else reportData.totalBenefitAmount
            val otherAverageBenefit = if (reportData.totalGroupBenefitAverage < 0)
                -reportData.totalGroupBenefitAverage else reportData.totalGroupBenefitAverage

            // 금액 차이 계산
            val diff = myMonthBenefit - otherAverageBenefit
            val absDiff = kotlin.math.abs(diff)
            val isLargeDiff = absDiff >= 10000 // 10만원 이상 차이
            val isSameAmount = myMonthBenefit == otherAverageBenefit

            // 그래프 높이 계산 (차이에 따라 다르게 적용)
            val baseHeight = 120f // 기본 높이

            // 그래프 높이 로직 수정:
            // 1. 동일한 경우: 두 막대 모두 동일한 중간 높이
            // 2. 10만원 이상 차이: 큰 쪽은 훨씬 크게, 작은 쪽은 작게
            // 3. 10만원 미만 차이: 크기 차이를 적게
            val (prevHeight, currHeight) = when {
                // 두 금액이 같을 경우 (동일한 중간 높이)
                isSameAmount -> Pair(baseHeight, baseHeight)

                // 두 금액 모두 0인 경우
                otherAverageBenefit == 0 && myMonthBenefit == 0 -> Pair(0f, 0f)

                // 현재 달 금액이 0인 경우
                myMonthBenefit == 0 -> Pair(baseHeight, 0f)

                // 이전 달 금액이 0인 경우
                otherAverageBenefit == 0 -> Pair(0f, baseHeight)

                // 10만원 이상 차이가 날 경우
                isLargeDiff -> {
                    if (myMonthBenefit>otherAverageBenefit) {
                        // 현재 달이 더 큰 경우
                        val ratio = (myMonthBenefit.toFloat() / otherAverageBenefit.toFloat())
                        val scale = if (ratio > 3f) 3f else ratio // 비율 제한
                        Pair(baseHeight / scale, baseHeight * 1.8f)
                    } else {
                        // 이전 달이 더 큰 경우
                        val ratio = (otherAverageBenefit.toFloat() / myMonthBenefit.toFloat())
                        val scale = if (ratio > 3f) 3f else ratio // 비율 제한
                        Pair(baseHeight * 1.8f, baseHeight / scale)
                    }
                }

                // 10만원 미만 차이가 날 경우 (완만한 차이)
                else -> {
                    if (myMonthBenefit > otherAverageBenefit) {
                        val diff = 1f + (absDiff.toFloat() / 10000f * 0.5f) // 최대 1.5배 차이
                        Pair(baseHeight, baseHeight * diff)
                    } else {
                        val diff = 1f + (absDiff.toFloat() / 10000f * 0.5f) // 최대 1.5배 차이
                        Pair(baseHeight * diff, baseHeight)
                    }
                }
            }

            // 비교 텍스트 계산 (미리 계산하지만 표시는 아래에서)
            val diffPercentage = if (otherAverageBenefit > 0) {
                diff.toFloat() / otherAverageBenefit.toFloat() * 100
            } else if (myMonthBenefit > 0) {
                100f  // 이전달이 0이고 현재달이 있으면 100% 증가
            } else {
                0f  // 두 달 모두 0이면 변화 없음
            }

            // 그래프 컨테이너
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 그래프 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 이전 달 컬럼
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(100.dp)
                    ) {
                        // 금액 텍스트 (바로 그래프 위에 - 한 줄로 표시)
                        Text(
                            text = "${formatAmount(otherAverageBenefit)}원",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 막대 그래프
                        if (otherAverageBenefit > 0) {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(prevHeight.dp)
                                    .background(
                                        color = Color.Gray,
                                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                    )
                            )
                        }
                    }

                    // 현재 달 컬럼
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(100.dp)
                    ) {
                        // 금액 텍스트 (바로 그래프 위에 - 한 줄로 표시)
                        Text(
                            text = "${formatAmount(myMonthBenefit)}원",
                            fontSize = 18.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 막대 그래프
                        if (myMonthBenefit > 0) {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(currHeight.dp)
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
                        }
                    }
                }

                // 구분선
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.4f))
                )

                // 선과 월 표시 사이 간격 추가
                Spacer(modifier = Modifier.height(15.dp))

                // 월 표시 행
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 이전 월 텍스트
                    Text(
                        text = "${reportData.groupName} 소비 그룹",
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.width(100.dp),
                        textAlign = TextAlign.Center
                    )

                    // 현재 월 텍스트
                    Text(
                        text = "나의 혜택",
                        fontSize = 20.sp,
                        color = calendarBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(100.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 이전 달 대비 소비 비교 - 2줄로 분리하여 하단에 표시
            if (diff != 0) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 첫 번째 줄: "이전 달 대비" (흰색)
                    Text(
                        text = "내 소비 구간 그룹 평균 대비",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Light
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 두 번째 줄: 증가/감소 텍스트 (색상 변경)
                    Text(
                        text = if (diff > 0)
                            "혜택을 ${formatAmount(diff)}원 더 받았습니다"
                        else
                            "헤택을 ${formatAmount(-diff)}원 덜 받았습니다",
                        fontSize = 20.sp,
                        color = calendarBlue,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                // 동일한 경우
                Text(
                    text = "이전 달과 소비가 동일합니다",
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 하단 여백
            Spacer(modifier = Modifier.height(8.dp))
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
            .wrapContentHeight(align = Alignment.Top)
            .heightIn(min = 400.dp)
            .padding(vertical = 8.dp),
        cornerRadius = 15f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(15.dp))
            // 타이틀 - 중앙정렬로 변경 및 크기 증가
            Text(
                text = "월별 소비 비교",
                color = Color.White,
                fontSize = title.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(15.dp))
            
            // 소비 금액 (음수로 오면 양수로 변환)
            val currentMonthSpending = if (reportData.totalSpendingAmount < 0) 
                -reportData.totalSpendingAmount else reportData.totalSpendingAmount
            val previousMonthSpending = if (reportData.preTotalSpendingAmount < 0) 
                -reportData.preTotalSpendingAmount else reportData.preTotalSpendingAmount
            
            // 금액 차이 계산
            val diff = currentMonthSpending - previousMonthSpending
            val absDiff = kotlin.math.abs(diff)
            val isLargeDiff = absDiff >= 100000 // 10만원 이상 차이
            val isSameAmount = currentMonthSpending == previousMonthSpending

            // 그래프 높이 계산 (차이에 따라 다르게 적용)
            val baseHeight = 120f // 기본 높이

            // 그래프 높이 로직 수정:
            // 1. 동일한 경우: 두 막대 모두 동일한 중간 높이
            // 2. 10만원 이상 차이: 큰 쪽은 훨씬 크게, 작은 쪽은 작게
            // 3. 10만원 미만 차이: 크기 차이를 적게
            val (prevHeight, currHeight) = when {
                // 두 금액이 같을 경우 (동일한 중간 높이)
                isSameAmount -> Pair(baseHeight, baseHeight)

                // 두 금액 모두 0인 경우
                currentMonthSpending == 0 && previousMonthSpending == 0 -> Pair(0f, 0f)

                // 현재 달 금액이 0인 경우
                currentMonthSpending == 0 -> Pair(baseHeight, 0f)

                // 이전 달 금액이 0인 경우
                previousMonthSpending == 0 -> Pair(0f, baseHeight)

                // 10만원 이상 차이가 날 경우
                isLargeDiff -> {
                    if (currentMonthSpending > previousMonthSpending) {
                        // 현재 달이 더 큰 경우
                        val ratio = (currentMonthSpending.toFloat() / previousMonthSpending.toFloat())
                        val scale = if (ratio > 3f) 3f else ratio // 비율 제한
                        Pair(baseHeight / scale, baseHeight * 1.8f)
                    } else {
                        // 이전 달이 더 큰 경우
                        val ratio = (previousMonthSpending.toFloat() / currentMonthSpending.toFloat())
                        val scale = if (ratio > 3f) 3f else ratio // 비율 제한
                        Pair(baseHeight * 1.8f, baseHeight / scale)
                    }
                }

                // 10만원 미만 차이가 날 경우 (완만한 차이)
                else -> {
                    if (currentMonthSpending > previousMonthSpending) {
                        val diff = 1f + (absDiff.toFloat() / 100000f * 0.5f) // 최대 1.5배 차이
                        Pair(baseHeight, baseHeight * diff)
                    } else {
                        val diff = 1f + (absDiff.toFloat() / 100000f * 0.5f) // 최대 1.5배 차이
                        Pair(baseHeight * diff, baseHeight)
                    }
                }
            }

            // 비교 텍스트 계산 (미리 계산하지만 표시는 아래에서)
            val diffPercentage = if (previousMonthSpending > 0) {
                diff.toFloat() / previousMonthSpending.toFloat() * 100
            } else if (currentMonthSpending > 0) {
                100f  // 이전달이 0이고 현재달이 있으면 100% 증가
            } else {
                0f  // 두 달 모두 0이면 변화 없음
            }
            
            // 그래프 컨테이너
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 그래프 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                    // 이전 달 컬럼
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(100.dp)
                ) {
                        // 금액 텍스트 (바로 그래프 위에 - 한 줄로 표시)
                    Text(
                            text = "${formatAmount(previousMonthSpending)}원",
                            fontSize = 18.sp,
                        color = Color.White,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                        // 막대 그래프
                        if (previousMonthSpending > 0) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(prevHeight.dp)
                            .background(
                                color = Color.Gray,
                                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                    )
                        }
                    }

                    // 현재 달 컬럼
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(100.dp)
                ) {
                        // 금액 텍스트 (바로 그래프 위에 - 한 줄로 표시)
                    Text(
                            text = "${formatAmount(currentMonthSpending)}원",
                            fontSize = 18.sp,
                        color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 막대 그래프
                        if (currentMonthSpending > 0) {
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(currHeight.dp)
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
                        }
                    }
                }
                        
                // 구분선
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.4f))
                )

                // 선과 월 표시 사이 간격 추가
                Spacer(modifier = Modifier.height(15.dp))

                // 월 표시 행
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 이전 월 텍스트
                    Text(
                        text = "${previousMonth}월",
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.width(100.dp),
                        textAlign = TextAlign.Center
                    )

                    // 현재 월 텍스트
                    Text(
                        text = "${currentMonth}월",
                        fontSize = 20.sp,
                                    color = calendarBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(100.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 이전 달 대비 소비 비교 - 2줄로 분리하여 하단에 표시
            if (diff != 0) {
                            Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                            ) {
                    // 첫 번째 줄: "이전 달 대비" (흰색)
                                Text(
                        text = "이전 달 대비",
                        fontSize = 20.sp,
                                    color = Color.White,
                        fontWeight = FontWeight.Light
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 두 번째 줄: 증가/감소 텍스트 (색상 변경)
                    Text(
                        text = if (diff > 0)
                            "소비가 ${formatAmount(diff)}원 증가했습니다"
                        else
                            "소비가 ${formatAmount(-diff)}원 감소했습니다",
                        fontSize = 20.sp,
                        color = calendarBlue,
                        fontWeight = FontWeight.ExtraBold
                                )
                            }
            } else {
                // 동일한 경우
                Text(
                    text = "이전 달과 소비가 동일합니다",
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 하단 여백
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// 빈 리포트 페이지
@Composable
fun EmptyReportPage(message: String = "리포트 데이터가 없습니다") {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .heightIn(min = 300.dp)
            .padding(vertical = 8.dp),
        cornerRadius = 15f
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
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
class CalendarViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
// 소비 유형 페이지
@Composable
fun ConsumptionTypePage(reportData: ReportData) {
    // 현재 년월 정보 가져오기
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModelFactory(LocalContext.current))
    val selectedYearMonth = viewModel.selectedReportYearMonth
    val userName by viewModel.userName.collectAsState()

    // consumptionPatterns가 null이면 예외 발생할 수 있으므로 안전하게 처리
    val consumptionPattern = reportData.consumptionPatterns
    if (consumptionPattern == null) {
        EmptyReportPage("소비 패턴 정보가 없습니다")
        return
    }

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top)
            .heightIn(min = 500.dp)
            .padding(vertical = 8.dp),
        cornerRadius = 15f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 제목 - "YYYY년 MM월"
            Text(
                text = "${selectedYearMonth.year}년 ${selectedYearMonth.monthValue}월",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // 사용자 이름 + 유형 (예: "박도하님의 유형은")
            Text(
                text = "${userName}님의 유형은,",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            // 소비 유형 이름 (큰 글씨로 중앙에 표시)
            Text(
                text = "${consumptionPattern.patternName}형 입니다",
                color = calendarBlue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 이미지 (earth.png 사용)


            val imageUrl = consumptionPattern.imgUrl

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "소비 유형 이미지",
                modifier = Modifier
                    .size(180.dp)
                    .padding(vertical = 4.dp),
                contentScale = ContentScale.Fit
            )
//            Image(
//                painter = painterResource(id = R.drawable.earth),
//                contentDescription = "소비 유형 이미지",
//                modifier = Modifier
//                    .size(180.dp)
//                    .padding(vertical = 4.dp),
//                contentScale = ContentScale.Fit
//            )

            Spacer(modifier = Modifier.height(10.dp))

            // reportDescription 표시
            Text(
                text = reportData.reportDescription,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 구분선 추가
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // 유형별 수치 제목
            Text(
                text = "유형별 수치",
                color = Color.White,
                fontSize = middle.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 외향성 게이지
            GaugeBar(
                label = "외향성",
                value = reportData.extrovert,
                maxValue = 100,
                color = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 안정성 게이지
            GaugeBar(
                label = "안정성",
                value = reportData.variation,
                maxValue = 100,
                color = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 저축성 게이지
            GaugeBar(
                label = "저축성",
                value = reportData.overConsumption,
                maxValue = 100,
                color = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
fun GaugeBar(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 라벨
        Text(
            text = label,
            color = Color.White,
            fontSize = small.sp,
            modifier = Modifier.width(60.dp),
            fontWeight = FontWeight.Light,
        )

        // 게이지 바
        Box(
            modifier = Modifier
                .weight(0.9f)
                .height(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value.toFloat() / maxValue)
                    .background(color)
            )
        }

        // 수치 표시
        Text(
            text = "$value/$maxValue",
            color = Color.White,
            fontSize = verysmall.sp,
            modifier = Modifier
                .width(60.dp)
                .padding(start = 8.dp)
        )
    }
}

// 도넛형 그래프를 그리는 함수
@Composable
fun AccountBookGraph(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    data: List<Float>,
    graphHeight: Int
) {
    val total = data.sum().takeIf { it > 0 } ?: 1f // 0으로 나누기 방지
    val angles = data.map { it / total * 360f }

    // Canvas를 사용하여 그래프를 그리고 `graphHeight.dp`를 픽셀 단위로 변환하여 그래프의 높이를 설정
    Canvas(modifier = modifier.height(graphHeight.dp)) {
        // 그래프의 선 두께를 지정
        val strokeWidth = graphHeight.dp.toPx() / 4
        // 원형 그래프의 반지름 설정
        val radius = (graphHeight.dp.toPx() - strokeWidth) / 2
        // 그래프의 중심 좌표
        val centerX = size.width / 2f
        val centerY = radius + strokeWidth / 2

        if (angles.isNotEmpty()) {
            var startAngle = -90f // 12시 방향을 0도로 시작

            // 리스트를 순회하면서 각 데이터 항목에 대한 원호를 그린다
            angles.forEachIndexed { index, angle ->
                val color = if (index < colors.size) colors[index] else Color.Gray

                drawArc(
                    color = color, // 그래프 부분의 색상
                    startAngle = startAngle, // 원호의 시작 각도
                    sweepAngle = angle, // 원호의 중심각
                    useCenter = false, // 원호만 그림 (부채꼴 아님)
                    style = Stroke(width = strokeWidth), // 선 두께 설정
                    topLeft = Offset(centerX - radius, centerY - radius), // 왼쪽 상단 좌표
                    size = Size(radius * 2, radius * 2) // 원호를 그릴 사각형의 크기
                )

                // 다음 항목의 시작 각도 업데이트
                startAngle += angle
            }
        } else {
            // 데이터가 없는 경우 회색 원 표시
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidth)
            )
        }
    }
}
