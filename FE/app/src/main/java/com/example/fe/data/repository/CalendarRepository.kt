package com.example.fe.data.repository

import android.util.Log
import com.example.fe.config.AppConfig
import com.example.fe.data.model.calendar.DailyLogData
import com.example.fe.data.model.calendar.TransactionData
import com.example.fe.data.model.calendar.MonthlyInfoData
import com.example.fe.data.model.calendar.ReportData
import com.example.fe.data.model.calendar.CardReport
import com.example.fe.data.model.calendar.CategoryReport
import com.example.fe.data.network.api.CalendarApiService
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.time.YearMonth
import java.util.concurrent.TimeUnit

/**
 * 캘린더 관련 데이터를 처리하는 리포지토리 클래스
 */
class CalendarRepository {
    private val TAG = "CalendarRepository"
    
    // 로깅 인터셉터 생성
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // OkHttpClient 설정
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.Server.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val calendarApiService = retrofit.create(CalendarApiService::class.java)
    
    /**
     * 특정 연월의 가계부 내역을 조회
     * @param yearMonth 조회할 연월
     * @return 일별 가계부 내역 리스트 또는 실패 시 예외
     */
    suspend fun getMonthlyLog(yearMonth: YearMonth): Result<List<DailyLogData>> {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        // 유저 아이디를 1로 고정
        val userId = "1"
        
        Log.d(TAG, "Getting monthly log for $year-$month with userId=$userId")
        return try {
            // 요청 파라미터를 맵으로 구성
            val params = mapOf(
                "year" to year,
                "month" to month
            )
            
            // 인증 토큰 - 실제 토큰으로 교체 필요
            // 주의: 이는 가정한 것이며, 실제 서버가 어떤 인증 방식을 사용하는지에 따라 달라질 수 있음
            val authToken = "Bearer YOUR_AUTH_TOKEN_HERE"
            
            Log.d(TAG, "Request URL: ${AppConfig.Server.BASE_URL}api/budgetlog/log?year=$year&month=$month&userId=$userId")
            Log.d(TAG, "Request params as QueryMap: $params with userId=$userId and auth token: $authToken")
            
            // GET 요청 시도
            val response = try {
                calendarApiService.getMonthlyLog(params, userId, authToken)
            } catch (e: Exception) {
                Log.e(TAG, "GET request failed, trying POST: ${e.message}")
                
                // GET 요청 실패 시 POST 요청 시도
                val jsonParams = JsonObject().apply {
                    addProperty("year", year)
                    addProperty("month", month)
                }
                val requestBody = jsonParams.toString().toRequestBody("application/json".toMediaTypeOrNull())
                
                try {
                    calendarApiService.postMonthlyLog(requestBody, userId, authToken)
                } catch (e2: Exception) {
                    Log.e(TAG, "POST request also failed: ${e2.message}")
                    throw e2
                }
            }
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "Response successful: $apiResponse")
                if (apiResponse.success) {
                    // 해당 월의 실제 일수를 확인
                    val daysInMonth = yearMonth.lengthOfMonth()
                    
                    // 유효한 날짜만 필터링
                    val filteredData = apiResponse.data.filter { dailyLog ->
                        val isValidDay = dailyLog.day in 1..daysInMonth
                        if (!isValidDay) {
                            Log.w(TAG, "Invalid day filtered out: ${dailyLog.day} for month ${yearMonth.month} (max: $daysInMonth)")
                        }
                        isValidDay
                    }
                    
                    Log.d(TAG, "Successfully fetched monthly log: ${filteredData.size} days (filtered from ${apiResponse.data.size})")
                    Result.success(filteredData)
                } else {
                    Log.e(TAG, "API error: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                // 실패 시 응답 본문과 코드를 함께 로깅
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get monthly log: code=${response.code()}, message=$errorBody")
                Log.e(TAG, "Request URL was: ${AppConfig.Server.BASE_URL}api/budgetlog/log?year=$year&month=$month&userId=$userId")
                Result.failure(Exception("Failed to get monthly log: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting monthly log: ${e.message}", e)
            // 실패시 더미 데이터 반환
            Log.d(TAG, "Falling back to dummy data due to error")
            Result.success(getDummyMonthlyLog(yearMonth))
        }
    }
    
    /**
     * 특정 연월의 일일 거래 기록을 조회
     * @param yearMonth 조회할 연월
     * @return 일일 거래 기록 리스트 또는 실패 시 예외
     */
    suspend fun getMonthlyTransactions(yearMonth: YearMonth): Result<List<TransactionData>> {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        // 유저 아이디를 1로 고정
        val userId = "1"
        
        Log.d(TAG, "Getting monthly transactions for $year-$month with userId=$userId")
        return try {
            // 요청 파라미터를 맵으로 구성
            val params = mapOf(
                "year" to year,
                "month" to month
            )
            
            // 인증 토큰 - 실제 토큰으로 교체 필요
            val authToken = "Bearer YOUR_AUTH_TOKEN_HERE"
            
            Log.d(TAG, "Request URL: ${AppConfig.Server.BASE_URL}api/budgetlog/transaction?year=$year&month=$month&userId=$userId")
            
            // API 호출
            val response = calendarApiService.getMonthlyTransactions(params, userId, authToken)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "Response successful: $apiResponse")
                if (apiResponse.success) {
                    Log.d(TAG, "Successfully fetched monthly transactions: ${apiResponse.data.size} transactions")
                    Result.success(apiResponse.data)
                } else {
                    Log.e(TAG, "API error: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                // 실패 시 응답 본문과 코드를 함께 로깅
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get monthly transactions: code=${response.code()}, message=$errorBody")
                Result.failure(Exception("Failed to get monthly transactions: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting monthly transactions: ${e.message}", e)
            // 실패 시 빈 리스트 반환
            Result.failure(e)
        }
    }
    
    /**
     * 특정 연월의 거래 현황을 조회
     * @param yearMonth 조회할 연월
     * @return 월간 거래 현황 데이터 또는 실패 시 예외
     */
    suspend fun getMonthlyLogInfo(yearMonth: YearMonth): Result<MonthlyInfoData> {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        // 유저 아이디를 1로 고정
        val userId = "1"
        
        Log.d(TAG, "Getting monthly info for $year-$month with userId=$userId")
        return try {
            // 요청 파라미터를 맵으로 구성
            val params = mapOf(
                "year" to year,
                "month" to month
            )
            
            // 인증 토큰 - 실제 토큰으로 교체 필요
            val authToken = "Bearer YOUR_AUTH_TOKEN_HERE"
            
            Log.d(TAG, "Request URL: ${AppConfig.Server.BASE_URL}api/budgetlog/info?year=$year&month=$month&userId=$userId")
            
            // API 호출
            val response = calendarApiService.getMonthlyLogInfo(params, userId, authToken)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "Response successful: $apiResponse")
                if (apiResponse.success) {
                    Log.d(TAG, "Successfully fetched monthly info: ${apiResponse.data}")
                    Result.success(apiResponse.data)
                } else {
                    Log.e(TAG, "API error: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                // 실패 시 응답 본문과 코드를 함께 로깅
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get monthly info: code=${response.code()}, message=$errorBody")
                Result.failure(Exception("Failed to get monthly info: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting monthly info: ${e.message}", e)
            // 실패 시 더미 데이터 반환
            Result.failure(e)
        }
    }
    
    /**
     * 특정 연월의 소비 리포트를 조회
     * @param yearMonth 조회할 연월
     * @return 소비 리포트 데이터 또는 실패 시 예외
     */
    suspend fun getReportWithPattern(yearMonth: YearMonth): Result<ReportData> {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        // 유저 아이디를 1로 고정
        val userId = "1"
        
        Log.d(TAG, "Getting report with pattern for $year-$month with userId=$userId")
        return try {
            // 요청 파라미터를 맵으로 구성
            val params = mapOf(
                "year" to year,
                "month" to month
            )
            
            // 인증 토큰 - 실제 토큰으로 교체 필요
            val authToken = "Bearer YOUR_AUTH_TOKEN_HERE"
            
            Log.d(TAG, "Request URL: ${AppConfig.Server.BASE_URL}api/report?year=$year&month=$month&userId=$userId")
            
            // API 호출
            val response = calendarApiService.getReportWithPattern(params, userId, authToken)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "Response successful: $apiResponse")
                if (apiResponse.success) {
                    val reportData = apiResponse.data
                    Log.d(TAG, "Successfully fetched report data: $reportData")
                    // 객체 필드에 직접 접근하지 않고 전체 객체를 로그로 출력
                    Result.success(reportData)
                } else {
                    Log.e(TAG, "API error: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                // 실패 시 응답 본문과 코드를 함께 로깅
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get report: code=${response.code()}, message=$errorBody")
                Result.failure(Exception("Failed to get report: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting report: ${e.message}", e)
            // 실패 시 예외 반환
            Result.failure(e)
        }
    }
    
    /**
     * 테스트용 더미 데이터 생성
     * 실제 API 연동에 문제가 있을 경우 사용
     */
    fun getDummyMonthlyLog(yearMonth: YearMonth): List<DailyLogData> {
        val daysInMonth = yearMonth.lengthOfMonth()
        Log.d(TAG, "Creating dummy data for ${yearMonth.month} with $daysInMonth days")
        
        return (1..daysInMonth).map { day ->
            // 날짜에 따라 다양한 더미 데이터 생성
            val plus = if (day % 7 == 0) 300000 else if (day % 3 == 0) 50000 else 0
            val minus = if (day % 2 == 0) (day * 2000) else (day * 1000)
            
            DailyLogData(day, plus, minus)
        }
    }

    /**
     * 특정 연월의 카드별 소비 및 혜택 조회
     * @param yearMonth 조회할 연월
     * @return 카드별 소비 및 혜택 데이터 리스트 또는 실패 시 예외
     */
    suspend fun getReportCards(yearMonth: YearMonth): Result<List<CardReport>> {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        // 유저 아이디를 1로 고정
        val userId = "1"
        
        Log.d(TAG, "Getting report cards for $year-$month with userId=$userId")
        return try {
            // 요청 파라미터를 맵으로 구성
            val params = mapOf(
                "year" to year,
                "month" to month
            )
            
            // 인증 토큰 - 실제 토큰으로 교체 필요
            val authToken = "Bearer YOUR_AUTH_TOKEN_HERE"
            
            Log.d(TAG, "Request URL: ${AppConfig.Server.BASE_URL}api/report/card?year=$year&month=$month&userId=$userId")
            
            // API 호출
            val response = calendarApiService.getReportCards(params, userId, authToken)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "Response successful: $apiResponse")
                if (apiResponse.success) {
                    val cardReports = apiResponse.data
                    Log.d(TAG, "Successfully fetched card reports: ${cardReports.size} cards")
                    
                    // 자세한 카드 정보 로깅
                    cardReports.forEach { card ->
                        // totalAmount가 0인 경우 카테고리에서 직접 계산한 값을 로그로 출력
                        val calculatedAmount = card.getCalculatedTotalAmount()
                        val calculatedBenefit = card.getCalculatedTotalBenefit()
                        
                        Log.d(TAG, "Card: ${card.name}, Total Amount: ${calculatedAmount}, Total Benefit: ${calculatedBenefit}")
                        card.categories.forEach { category ->
                            // 카테고리별 금액은 음수로 오므로 로그에 표시할 때만 절대값 사용
                            val displayAmount = if (category.amount < 0) -category.amount else category.amount
                            Log.d(TAG, "  ${category.category}: ${displayAmount}원 지출, ${category.benefit}원 혜택 (${category.count}회)")
                        }
                    }
                    
                    Result.success(cardReports)
                } else {
                    Log.e(TAG, "API error: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                // 실패 시 응답 본문과 코드를 함께 로깅
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get card reports: code=${response.code()}, message=$errorBody")
                Result.failure(Exception("Failed to get card reports: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting card reports: ${e.message}", e)
            // 실패 시 예외 반환
            Result.failure(e)
        }
    }

    /**
     * 특정 연월의 카테고리별 소비 및 혜택 조회
     * @param yearMonth 조회할 연월
     * @return 카테고리별 소비 및 혜택 데이터 리스트 또는 실패 시 예외
     */
    suspend fun getReportCategories(yearMonth: YearMonth): Result<List<CategoryReport>> {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        // 유저 아이디를 1로 고정
        val userId = "1"
        
        Log.d(TAG, "Getting report categories for $year-$month with userId=$userId")
        return try {
            // 요청 파라미터를 맵으로 구성
            val params = mapOf(
                "year" to year,
                "month" to month
            )
            
            // 인증 토큰 - 실제 토큰으로 교체 필요
            val authToken = "Bearer YOUR_AUTH_TOKEN_HERE"
            
            Log.d(TAG, "Request URL: ${AppConfig.Server.BASE_URL}api/report/category?year=$year&month=$month&userId=$userId")
            
            // API 호출
            val response = calendarApiService.getReportCategories(params, userId, authToken)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "Response successful: $apiResponse")
                if (apiResponse.success) {
                    val categoryReports = apiResponse.data
                    Log.d(TAG, "Successfully fetched category reports: ${categoryReports.size} categories")
                    
                    // 자세한 카테고리 정보 로깅
                    categoryReports.forEach { category ->
                        // amount가 음수로 올 수 있으므로 로그에 표시할 때는 절대값 사용
                        val displayAmount = if (category.amount < 0) -category.amount else category.amount
                        Log.d(TAG, "Category: ${category.category}, Amount: ${displayAmount}원, Benefit: ${category.benefit}원")
                    }
                    
                    Result.success(categoryReports)
                } else {
                    Log.e(TAG, "API error: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                // 실패 시 응답 본문과 코드를 함께 로깅
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get category reports: code=${response.code()}, message=$errorBody")
                Result.failure(Exception("Failed to get category reports: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category reports: ${e.message}", e)
            // 실패 시 예외 반환
            Result.failure(e)
        }
    }
} 