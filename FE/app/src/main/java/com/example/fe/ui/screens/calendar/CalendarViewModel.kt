package com.example.fe.ui.screens.calendar

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fe.config.AppConfig
import com.example.fe.data.model.calendar.DailyLogData
import com.example.fe.data.model.calendar.TransactionData
import com.example.fe.data.model.calendar.MonthlyInfoData
import com.example.fe.data.model.calendar.ReportData
import com.example.fe.data.model.calendar.CardReport
import com.example.fe.data.model.calendar.CategoryReport
import com.example.fe.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 캘린더 화면의 상태를 관리하는 ViewModel
 */
class CalendarViewModel : ViewModel() {
    private val TAG = "CalendarViewModel"
    private val repository = CalendarRepository()
    
    // 현재 선택된 연월 (달력 탭용)
    var selectedYearMonth by mutableStateOf(YearMonth.now())
        private set
    
    // 선택된 리포트 연월 (리포트 탭용)
    var selectedReportYearMonth by mutableStateOf(getPreviousMonth(YearMonth.now()))
        private set
    
    // 현재 선택된 날짜
    var selectedDate by mutableStateOf(LocalDate.now())
        private set
    
    // 선택된 탭 인덱스 (0: 가계부, 1: 소비 리포트)
    var selectedTabIndex by mutableStateOf(0)
        private set
    
    // API 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // API 에러 상태
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 일별 가계부 데이터
    private val _monthlyData = MutableStateFlow<List<DailyLogData>>(emptyList())
    val monthlyData: StateFlow<List<DailyLogData>> = _monthlyData.asStateFlow()
    
    // 일일 거래 기록 데이터
    private val _transactions = MutableStateFlow<List<TransactionData>>(emptyList())
    val transactions: StateFlow<List<TransactionData>> = _transactions.asStateFlow()
    
    // 월간 거래 현황 데이터
    private val _monthlyInfo = MutableStateFlow<MonthlyInfoData?>(null)
    val monthlyInfo: StateFlow<MonthlyInfoData?> = _monthlyInfo.asStateFlow()
    
    // 소비 리포트 데이터
    private val _reportData = MutableStateFlow<ReportData?>(null)
    val reportData: StateFlow<ReportData?> = _reportData.asStateFlow()
    
    // 카드별 소비 및 혜택 데이터
    private val _cardReports = MutableStateFlow<List<CardReport>>(emptyList())
    val cardReports: StateFlow<List<CardReport>> = _cardReports.asStateFlow()
    
    // 카테고리별 소비 및 혜택 데이터
    private val _categoryReports = MutableStateFlow<List<CategoryReport>>(emptyList())
    val categoryReports: StateFlow<List<CategoryReport>> = _categoryReports.asStateFlow()
    
    // 디버그 모드 여부
    private val useDebugMode = AppConfig.App.DEBUG_MODE
    
    // 리포트 탭 스크롤 오프셋 - 배경 이동을 위한 값
    private val _reportScrollOffset = MutableStateFlow(0f)
    val reportScrollOffset: StateFlow<Float> = _reportScrollOffset
    
    init {
        // 초기 데이터 로드
        loadMonthlyData(selectedYearMonth)
        loadMonthlyTransactions(selectedYearMonth)
        loadMonthlyInfo(selectedYearMonth)
    }
    
    /**
     * 리포트 탭에서 이전 월로 이동 가능한지 여부
     * 이전 제한을 제거하고 항상 이동 가능하도록 변경
     */
    fun canNavigateToPreviousReportMonth(): Boolean {
        // 제한 없이 항상 이전 월로 이동 가능
        return true
    }
    
    /**
     * 리포트 탭에서 다음 월로 이동 가능한지 여부
     * 이전 제한을 제거하고 현재 월 이전까지 이동 가능하도록 변경
     */
    fun canNavigateToNextReportMonth(): Boolean {
        // 현재 월 이전까지만 이동 가능 (미래 데이터는 없으므로)
        return selectedReportYearMonth.isBefore(YearMonth.now())
    }
    
    /**
     * 특정 연월의 이전 월 반환
     */
    private fun getPreviousMonth(yearMonth: YearMonth): YearMonth {
        return yearMonth.minusMonths(1)
    }
    
    /**
     * 주어진 달력 월에 적합한 리포트 월 결정
     * 모든 제한을 제거하고 항상 달력과 동일한 월을 리포트로 사용
     * 단, 미래 월은 현재 월을 리포트로 사용
     */
    private fun determineReportMonthForCalendarMonth(calendarMonth: YearMonth): YearMonth {
        val currentYearMonth = YearMonth.now()
        
        return when {
            // 미래 월의 달력이면 현재 월 리포트 반환 (미래 데이터는 없으므로)
            calendarMonth.isAfter(currentYearMonth) -> currentYearMonth
            
            // 현재 월 포함 과거 월은 그대로 동일한 월의 리포트 반환
            else -> calendarMonth
        }
    }
    
    /**
     * 탭 선택 변경
     */
    fun selectTab(tabIndex: Int) {
        // 이전 탭 인덱스 저장
        val previousTabIndex = selectedTabIndex
        
        selectedTabIndex = tabIndex
        
        if (tabIndex == 1) {
            // 가계부 -> 소비 리포트: 현재 달력 월에 맞는 리포트 결정 및 로드
            val reportMonth = determineReportMonthForCalendarMonth(selectedYearMonth)
            selectedReportYearMonth = reportMonth
            
            // 모든 달에 대해 리포트 로드 (현재 달 포함)
            loadReportWithPattern(reportMonth)
            
            // 카드 및 카테고리 리포트는 항상 로드
            loadReportCards(reportMonth)
            loadReportCategories(reportMonth)
            
            Log.d(TAG, "선택된 리포트 월: $selectedReportYearMonth (달력 월: $selectedYearMonth)")
        } else if (previousTabIndex == 1 && tabIndex == 0) {
            // 소비 리포트 -> 가계부: 리포트 월에 맞는 달력으로 변경
            selectYearMonth(selectedReportYearMonth)
            
            Log.d(TAG, "리포트 월에 맞춰 달력 월 변경: $selectedYearMonth")
        }
    }
    
    /**
     * 리포트 탭에서 이전 월로 이동
     */
    fun navigateToPreviousReportMonth() {
        if (canNavigateToPreviousReportMonth()) {
            val newReportMonth = selectedReportYearMonth.minusMonths(1)
            selectedReportYearMonth = newReportMonth
            
            // 모든 달에 대해 리포트 로드 (현재 달 포함)
            loadReportWithPattern(newReportMonth)
            
            // 카드 및 카테고리 리포트는 항상 로드
            loadReportCards(newReportMonth)
            loadReportCategories(newReportMonth)
            
            Log.d(TAG, "이전 리포트 월로 이동: $selectedReportYearMonth")
        }
    }
    
    /**
     * 리포트 탭에서 다음 월로 이동
     */
    fun navigateToNextReportMonth() {
        if (canNavigateToNextReportMonth()) {
            val newReportMonth = selectedReportYearMonth.plusMonths(1)
            selectedReportYearMonth = newReportMonth
            
            // 모든 달에 대해 리포트 로드 (현재 달 포함)
            loadReportWithPattern(newReportMonth)
            
            // 카드 및 카테고리 리포트는 항상 로드
            loadReportCards(newReportMonth)
            loadReportCategories(newReportMonth)
            
            Log.d(TAG, "다음 리포트 월로 이동: $selectedReportYearMonth")
        }
    }
    
    /**
     * 연월 변경 시 새로운 데이터 로드
     */
    fun selectYearMonth(yearMonth: YearMonth) {
        if (yearMonth != selectedYearMonth) {
            selectedYearMonth = yearMonth
            loadMonthlyData(yearMonth)
            loadMonthlyTransactions(yearMonth)
            loadMonthlyInfo(yearMonth)
            
            // 소비 리포트 탭이 선택된 경우, 리포트 데이터도 다시 로드
            if (selectedTabIndex == 1) {
                val reportMonth = determineReportMonthForCalendarMonth(yearMonth)
                selectedReportYearMonth = reportMonth
                
                // 모든 달에 대해 리포트 로드 (현재 달 포함)
                loadReportWithPattern(reportMonth)
                
                // 카드 및 카테고리 리포트는 항상 로드
                loadReportCards(reportMonth)
                loadReportCategories(reportMonth)
                
                Log.d(TAG, "달력 월 변경에 따른 리포트 월 조정: $selectedReportYearMonth (달력 월: $selectedYearMonth)")
            }
        }
    }
    
    /**
     * 날짜 선택
     */
    fun selectDate(date: LocalDate) {
        selectedDate = date
    }
    
    /**
     * 현재 연월 기준으로 이전 달로 이동
     */
    fun navigateToPreviousMonth() {
        selectYearMonth(selectedYearMonth.minusMonths(1))
    }
    
    /**
     * 현재 연월 기준으로 다음 달로 이동
     * 미래 제한을 제거하고 자유롭게 이동 가능하도록 변경
     */
    fun navigateToNextMonth() {
        // 제한 없이 항상 다음 달로 이동 가능
        selectYearMonth(selectedYearMonth.plusMonths(1))
    }
    
    /**
     * 월별 가계부 데이터 로드
     */
    private fun loadMonthlyData(yearMonth: YearMonth) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                if (useDebugMode) {
                    // 디버그 모드일 경우 더미 데이터 사용
                    Log.d(TAG, "Using dummy data for $yearMonth")
                    _monthlyData.value = repository.getDummyMonthlyLog(yearMonth)
                } else {
                    // 실제 API 호출
                    Log.d(TAG, "Loading data for $yearMonth")
                    repository.getMonthlyLog(yearMonth).fold(
                        onSuccess = { data ->
                            _monthlyData.value = data
                            Log.d(TAG, "Successfully loaded ${data.size} days of data")
                        },
                        onFailure = { e ->
                            _error.value = e.message ?: "Unknown error occurred"
                            Log.e(TAG, "Error loading monthly data: ${e.message}", e)
                            // 에러 발생 시 더미 데이터로 대체
                            _monthlyData.value = repository.getDummyMonthlyLog(yearMonth)
                        }
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                Log.e(TAG, "Unexpected error: ${e.message}", e)
                // 예외 발생 시 더미 데이터로 대체
                _monthlyData.value = repository.getDummyMonthlyLog(yearMonth)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 월별 일일 거래 기록 로드
     */
    private fun loadMonthlyTransactions(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading transactions for $yearMonth")
                repository.getMonthlyTransactions(yearMonth).fold(
                    onSuccess = { data ->
                        _transactions.value = data
                        Log.d(TAG, "Successfully loaded ${data.size} transactions")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Error loading transactions: ${e.message}", e)
                        _transactions.value = emptyList()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading transactions: ${e.message}", e)
                _transactions.value = emptyList()
            }
        }
    }
    
    /**
     * 월간 거래 현황 로드
     */
    private fun loadMonthlyInfo(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading monthly info for $yearMonth")
                repository.getMonthlyLogInfo(yearMonth).fold(
                    onSuccess = { data ->
                        _monthlyInfo.value = data
                        Log.d(TAG, "Successfully loaded monthly info: $data")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Error loading monthly info: ${e.message}", e)
                        _monthlyInfo.value = null
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading monthly info: ${e.message}", e)
                _monthlyInfo.value = null
            }
        }
    }
    
    /**
     * 소비 리포트 로드
     */
    private fun loadReportWithPattern(yearMonth: YearMonth) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentYearMonth = YearMonth.now()
                val isCurrentMonth = yearMonth.year == currentYearMonth.year && 
                                    yearMonth.monthValue == currentYearMonth.monthValue
                
                Log.d(TAG, "Loading report for $yearMonth (Current month: ${isCurrentMonth})")
                repository.getReportWithPattern(yearMonth).fold(
                    onSuccess = { data ->
                        _reportData.value = data
                        Log.d(TAG, "Successfully loaded report data for $yearMonth (Current month: ${isCurrentMonth})")
                        Log.d(TAG, "Report data: Total spending: ${data.totalSpendingAmount}, Benefits: ${data.totalBenefitAmount}")
                        if (isCurrentMonth) {
                            Log.d(TAG, "현재 달 리포트 데이터 로드 성공! 데이터: $data")
                        }
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Error loading report for $yearMonth: ${e.message}", e)
                        _error.value = e.message ?: "리포트를 불러오는 중 오류가 발생했습니다."
                        _reportData.value = null
                        if (isCurrentMonth) {
                            Log.e(TAG, "현재 달 리포트 데이터 로드 실패! 오류: ${e.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading report: ${e.message}", e)
                _error.value = e.message ?: "예상치 못한 오류가 발생했습니다."
                _reportData.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 카드별 소비 및 혜택 로드
     */
    private fun loadReportCards(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading card reports for $yearMonth")
                repository.getReportCards(yearMonth).fold(
                    onSuccess = { data ->
                        _cardReports.value = data
                        Log.d(TAG, "Successfully loaded card reports: ${data.size} cards")
                        data.forEach { card ->
                            // 계산된 총액과 혜택 금액 사용 (API 응답값이 0인 경우 대비)
                            val calculatedAmount = card.getCalculatedTotalAmount()
                            val calculatedBenefit = card.getCalculatedTotalBenefit()
                            
                            Log.d(TAG, "Card: ${card.name}, Total: ${calculatedAmount}원, Benefit: ${calculatedBenefit}원")
                            card.categories.forEach { category ->
                                // 카테고리별 금액은 음수로 오므로 로그에 표시할 때만 절대값 사용
                                val displayAmount = if (category.amount < 0) -category.amount else category.amount
                                Log.d(TAG, "  ${category.category}: ${displayAmount}원 소비, ${category.benefit}원 혜택 (${category.count}회)")
                            }
                        }
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Error loading card reports: ${e.message}", e)
                        _cardReports.value = emptyList()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading card reports: ${e.message}", e)
                _cardReports.value = emptyList()
            }
        }
    }
    
    /**
     * 카테고리별 소비 및 혜택 로드
     */
    private fun loadReportCategories(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading category reports for $yearMonth")
                repository.getReportCategories(yearMonth).fold(
                    onSuccess = { data ->
                        _categoryReports.value = data
                        Log.d(TAG, "Successfully loaded category reports: ${data.size} categories")
                        data.forEach { category ->
                            // amount가 음수로 오는 경우 절대값으로 변환하여 로그 표시
                            val displayAmount = category.getAbsoluteAmount()
                            Log.d(TAG, "Category: ${category.category}, Amount: ${displayAmount}원, Benefit: ${category.benefit}원")
                        }
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Error loading category reports: ${e.message}", e)
                        _categoryReports.value = emptyList()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error loading category reports: ${e.message}", e)
                _categoryReports.value = emptyList()
            }
        }
    }
    
    /**
     * 현재 로드된 데이터로부터 일별 요약 정보 생성
     */
    fun getDailySummaries(): Map<LocalDate, DailySummary> {
        val data = _monthlyData.value
        Log.d(TAG, "Creating daily summaries from ${data.size} records for ${selectedYearMonth.month} ${selectedYearMonth.year}")
        
        if (data.isEmpty()) {
            Log.d(TAG, "No data available for this month. 데이터가 없습니다.")
            return emptyMap()
        }
        
        val result = data.associate { dailyLog ->
            val date = LocalDate.of(selectedYearMonth.year, selectedYearMonth.month, dailyLog.day)
            // API에서는 minus 값이 음수로 오므로 절대값으로 변환
            val expense = if (dailyLog.minus < 0) -dailyLog.minus else dailyLog.minus
            
            val summary = DailySummary(income = dailyLog.plus, expense = expense)
            Log.d(TAG, "Day ${dailyLog.day}: plus=${dailyLog.plus}, minus=${dailyLog.minus} -> income=${summary.income}, expense=${summary.expense}")
            
            date to summary
        }
        
        Log.d(TAG, "Created ${result.size} daily summaries with dates: ${result.keys.joinToString { it.toString() }}")
        return result
    }
    
    /**
     * 이번 달 전체 수입/지출 합계 계산
     */
    fun getMonthlySummary(): DailySummary {
        val data = _monthlyData.value
        val totalIncome = data.sumOf { it.plus }
        // API에서는 minus가 음수이므로 절대값으로 변환하여 합산
        val totalExpense = data.sumOf { if (it.minus < 0) -it.minus else it.minus }
        return DailySummary(income = totalIncome, expense = totalExpense)
    }
    
    /**
     * 특정 날짜의 거래 기록 조회
     */
    fun getTransactionsForDate(date: LocalDate): List<TransactionData> {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        
        return transactions.value.filter { transaction ->
            try {
                val transactionDateTime = LocalDateTime.parse(transaction.date, formatter)
                transactionDateTime.toLocalDate() == date
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing transaction date: ${transaction.date}", e)
                false
            }
        }
    }
    
    // 리포트 스크롤 오프셋 업데이트
    fun updateReportScrollOffset(offset: Float) {
        _reportScrollOffset.value = offset
    }
} 