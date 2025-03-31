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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.ui.components.backgrounds.GlassSurface
import com.example.fe.ui.components.backgrounds.StarryBackground
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

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

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onScrollOffsetChange: (Float) -> Unit = {}
) {
    var scrollOffset by remember { mutableStateOf(0f) }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // 위로 가기 버튼 표시 여부 (스크롤 위치에 따라 결정)
    val showScrollToTopButton by remember {
        derivedStateOf {
            // 스크롤 위치가 일정 이상이면 버튼 표시
            lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 200
        }
    }
    
    // 현재 선택된 연월
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    
    // 현재 선택된 날짜
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    // 날짜별 위치 인덱스를 저장하는 맵
    val dateIndexMap = remember { mutableMapOf<LocalDate, Int>() }
    
    // 샘플 거래 데이터
    val transactionItems = remember {
        listOf(
            TransactionItem(1, LocalDate.now(), "카페", "스타벅스", -5000, "신한카드"),
            TransactionItem(2, LocalDate.now(), "식사", "김밥천국", -8000, "현금"),
            TransactionItem(3, LocalDate.now().minusDays(1), "교통", "버스", -1500, "카카오페이"),
            TransactionItem(4, LocalDate.now().minusDays(2), "마트", "이마트", -26500, "현대카드"),
            TransactionItem(5, LocalDate.now().minusDays(3), "월급", "회사", 3000000, "계좌이체"),
            TransactionItem(6, LocalDate.now().minusDays(5), "쇼핑", "무신사", -32000, "토스"),
            TransactionItem(7, LocalDate.now().minusDays(7), "문화", "CGV", -15000, "우리카드"),
            TransactionItem(8, LocalDate.now().minusDays(10), "교육", "인터넷 강의", -300000, "계좌이체"),
            TransactionItem(9, LocalDate.now().minusDays(13), "통신", "SKT", -55000, "자동이체"),
            TransactionItem(10, LocalDate.now().plusDays(1), "식사", "본죽", -10000, "신한카드"),
            TransactionItem(11, LocalDate.now().plusDays(1), "카페", "투썸플레이스", -6000, "토스"),
            TransactionItem(12, LocalDate.now().plusDays(2), "쇼핑", "올리브영", -12000, "현대카드"),
            TransactionItem(13, LocalDate.now().plusDays(3), "용돈", "부모님", 200000, "계좌이체")
        )
    }
    
    // 날짜별 데이터 그룹화
    val transactionsByDate = remember(transactionItems, currentYearMonth) {
        transactionItems
            .filter { it.date.year == currentYearMonth.year && it.date.monthValue == currentYearMonth.monthValue }
            .groupBy { it.date }
    }
    
    // 날짜별 수입/지출 합계 계산
    val dailySummaries = remember(transactionsByDate) {
        transactionsByDate.mapValues { (_, transactions) ->
            DailySummary(
                income = transactions.filter { it.amount > 0 }.sumOf { it.amount },
                expense = transactions.filter { it.amount < 0 }.sumOf { -it.amount }
            )
        }
    }
    
    // 이번 달 전체 수입/지출 합계
    val monthlySummary = remember(dailySummaries) {
        DailySummary(
            income = dailySummaries.values.sumOf { it.income },
            expense = dailySummaries.values.sumOf { it.expense }
        )
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
    
    // 배경과 콘텐츠를 함께 배치
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 배경 (스크롤에 따라 움직임)
        StarryBackground(
            scrollOffset = scrollOffset,
            starCount = 150,
            modifier = Modifier.fillMaxSize()
        ) {
            // 빈 Box - 배경만 표시
        }
        
        // 실제 스크롤 가능한 콘텐츠
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            state = lazyListState
        ) {
            // 상단 타이틀
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "가계부",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Button(
                        onClick = { /* 리포트 화면으로 이동 */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3F51B5)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "월별 소비 리포트 보러 가기",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 달력 영역
            item {
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    cornerRadius = 16f
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // 연월 표시 및 이전/다음 버튼
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    currentYearMonth = currentYearMonth.minusMonths(1)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "이전 달",
                                    tint = Color.White
                                )
                            }
                            
                            Text(
                                text = "${currentYearMonth.monthValue}월 ${currentYearMonth.year}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            IconButton(
                                onClick = {
                                    currentYearMonth = currentYearMonth.plusMonths(1)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "다음 달",
                                    tint = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // 요일 헤더
                        val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
                        Row(
                            modifier = Modifier.fillMaxWidth()
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
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // 달력 그리드
                        CalendarGrid(
                            yearMonth = currentYearMonth,
                            selectedDate = selectedDate,
                            dailySummaries = dailySummaries,
                            onDateSelected = { date ->
                                selectedDate = date
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
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 월간 요약
            item {
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    cornerRadius = 16f
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "${monthlySummary.expense}원 소비",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "최다 소비 카테고리: 카페",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        
                        Text(
                            text = "지난달보다 ${3000}원 더 소비중",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        
                        Text(
                            text = "13일 수요일",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 일자별 거래 내역
            transactionsByDate.keys.sorted().forEachIndexed { index, date ->
                // 인덱스 맵에 저장
                dateIndexMap[date] = index + 3 // 상단 아이템 3개 고려 (제목, 달력, 월간 요약)
                
                item {
                    // 일자 헤더
                    Text(
                        text = formatDateHeader(date),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // 해당 일자의 거래 내역
                    transactionsByDate[date]?.forEach { transaction ->
                        TransactionListItem(transaction = transaction)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(80.dp))
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
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0x33FFFFFF)
                else Color.Transparent
            )
            .border(
                width = if (isToday) 1.dp else 0.dp,
                color = if (isToday) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                if (isCurrentMonth) onDateSelected(date)
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
                fontSize = 18.sp, // 글자 크기 증가
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    !isCurrentMonth -> Color.Gray
                    date.dayOfWeek.value == 7 -> Color(0xFFFF5252) // 일요일
                    date.dayOfWeek.value == 6 -> Color(0xFF448AFF) // 토요일
                    else -> Color.White
                },
                textAlign = TextAlign.Center
            )
            
            // 수입/지출 표시
            if (summary != null && isCurrentMonth) {
                if (summary.income > 0) {
                    Text(
                        text = "+${formatAmount(summary.income)}",
                        fontSize = 12.sp, // 글자 크기 증가
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                }
                
                if (summary.expense > 0) {
                    Text(
                        text = "-${formatAmount(summary.expense)}",
                        fontSize = 12.sp, // 글자 크기 증가
                        color = Color(0xFFFF5252),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(transaction: TransactionItem) {
    val isIncome = transaction.amount > 0
    
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        cornerRadius = 12f
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 카테고리 아이콘
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) Color(0x334CAF50) else Color(0x33FF5252)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.category.take(1),
                    color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 거래 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.place,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = transaction.paymentMethod,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
            
            // 금액
            Text(
                text = if (isIncome) "+${formatAmount(transaction.amount)}원"
                      else "-${formatAmount(-transaction.amount)}원",
                color = if (isIncome) Color(0xFF4CAF50) else Color(0xFFFF5252),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 금액 포맷팅 (예: 10000 -> 10,000)
fun formatAmount(amount: Int): String {
    return String.format("%,d", amount)
}

// 날짜 헤더 포맷팅 (예: 3월 15일 (수요일))
fun formatDateHeader(date: LocalDate): String {
    val month = date.monthValue
    val day = date.dayOfMonth
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
    
    return "${month}월 ${day}일 (${dayOfWeek})"
} 