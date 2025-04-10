package com.example.fe.data.repository

import android.util.Log
import com.example.fe.data.model.calendar.*
import com.example.fe.data.network.NetworkClient
import com.example.fe.data.network.request.MonthlyLogRequest
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.YearMonth

/**
 * 캘린더 관련 데이터를 처리하는 리포지토리 클래스
 */
class CalendarRepository {
    private val TAG = "CalendarRepository"
    private val calendarApiService = NetworkClient.calendarApiService
    
    /**
     * 특정 연월의 가계부 내역을 조회
     * @param yearMonth 조회할 연월
     * @return 일별 가계부 내역 리스트 또는 실패 시 예외
     */
    suspend fun getMonthlyLog(yearMonth: YearMonth): Result<List<DailyLogData>> {
        val year = yearMonth.year
        val month = yearMonth.monthValue
        
        return try {
            Log.d(TAG, "Getting monthly log for $year-$month")
            Log.d(TAG, "Request URL: api/budgetlog/log?year=$year&month=$month")
            
            val response = calendarApiService.getMonthlyLog(year, month)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data received"))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Failed to get monthly log: ${response.code()}, $errorBody")
                Result.failure(Exception("Failed to get monthly log: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting monthly log: ${e.message}")
            Result.failure(e)
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
        
        Log.d(TAG, "Getting monthly transactions for $year-$month")
        return try {
            Log.d(TAG, "Request URL: api/budgetlog/transaction?year=$year&month=$month")
            
            val response = calendarApiService.getMonthlyTransactions(year, month)
            
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
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get monthly transactions: code=${response.code()}, message=$errorBody")
                Result.failure(Exception("Failed to get monthly transactions: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting monthly transactions: ${e.message}", e)
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
        
        Log.d(TAG, "Getting monthly info for $year-$month")
        return try {
            Log.d(TAG, "Request URL: api/budgetlog/info?year=$year&month=$month")
            
            val response = calendarApiService.getMonthlyLogInfo(year, month)
            
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
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get monthly info: code=${response.code()}, message=$errorBody")
                Result.failure(Exception("Failed to get monthly info: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting monthly info: ${e.message}", e)
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
        
        Log.d(TAG, "Getting report with pattern for $year-$month")
        return try {
            Log.d(TAG, "Request URL: api/report?year=$year&month=$month")
            
            val response = calendarApiService.getReportWithPattern(year, month)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "Response successful: $apiResponse")
                if (apiResponse.success) {
                    val reportData = apiResponse.data
                    Log.d(TAG, "Successfully fetched report data: $reportData")
                    Result.success(reportData)
                } else {
                    Log.e(TAG, "API error: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get report: code=${response.code()}, message=$errorBody")
                Result.failure(Exception("Failed to get report: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting report: ${e.message}", e)
            Result.failure(e)
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
        
        Log.d(TAG, "Getting report cards for $year-$month")
        return try {
            val response = calendarApiService.getReportCards(year, month)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "Response successful: $apiResponse")
                if (apiResponse.success) {
                    val cardReports = apiResponse.data
                    Log.d(TAG, "Successfully fetched card reports: ${cardReports.size} cards")
                    Result.success(cardReports)
                } else {
                    Log.e(TAG, "API error: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get card reports: code=${response.code()}, message=$errorBody")
                Result.failure(Exception("Failed to get card reports: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting card reports: ${e.message}", e)
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
        
        Log.d(TAG, "Getting report categories for $year-$month")
        return try {
            val response = calendarApiService.getReportCategories(year, month)
            
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                Log.d(TAG, "Response successful: $apiResponse")
                if (apiResponse.success) {
                    val categoryReports = apiResponse.data
                    Log.d(TAG, "Successfully fetched category reports: ${categoryReports.size} categories")
                    Result.success(categoryReports)
                } else {
                    Log.e(TAG, "API error: ${apiResponse.message}")
                    Result.failure(Exception("API error: ${apiResponse.message}"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e(TAG, "Failed to get category reports: code=${response.code()}, message=$errorBody")
                Result.failure(Exception("Failed to get category reports: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category reports: ${e.message}", e)
            Result.failure(e)
        }
    }
} 