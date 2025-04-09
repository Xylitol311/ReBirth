package com.example.fe.data.network.api

import com.example.fe.data.model.calendar.*
import com.example.fe.data.network.request.MonthlyLogRequest
import com.example.fe.data.network.response.ApiResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * 캘린더 화면에서 사용하는 API 요청을 정의한 인터페이스
 */
interface CalendarApiService {
    
    /**
     * 월별 가계부 내역 조회 API (GET 방식)
     */
    @GET("api/budgetlog/log")
    suspend fun getMonthlyLog(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<ApiResponse<List<DailyLogData>>>
    
    /**
     * 월별 가계부 내역 조회 API (POST 방식)
     */
    @POST("api/budgetlog/log")
    suspend fun postMonthlyLog(
        @Body request: MonthlyLogRequest
    ): Response<ApiResponse<List<DailyLogData>>>
    
    /**
     * 월별 일일 거래 기록 조회 API
     */
    @GET("api/budgetlog/transaction")
    suspend fun getMonthlyTransactions(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<ApiResponse<List<TransactionData>>>
    
    /**
     * 월간 거래 현황 조회 API
     */
    @GET("api/budgetlog/info")
    suspend fun getMonthlyLogInfo(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<ApiResponse<MonthlyInfoData>>
    
    /**
     * 소비 리포트 조회 API
     */
    @GET("api/report")
    suspend fun getReportWithPattern(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<ApiResponse<ReportData>>
    
    /**
     * 카드별 소비 및 혜택 조회 API
     */
    @GET("api/report/card")
    suspend fun getReportCards(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<ApiResponse<List<CardReport>>>
    
    /**
     * 카테고리별 소비 및 혜택 조회 API
     */
    @GET("api/report/category")
    suspend fun getReportCategories(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<ApiResponse<List<CategoryReport>>>
} 