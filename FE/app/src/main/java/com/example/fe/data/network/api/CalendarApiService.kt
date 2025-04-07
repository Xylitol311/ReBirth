package com.example.fe.data.network.api

import com.example.fe.data.model.calendar.MonthlyLogResponse
import com.example.fe.data.model.calendar.TransactionResponse
import com.example.fe.data.model.calendar.MonthlyInfoResponse
import com.example.fe.data.model.calendar.ReportResponse
import com.example.fe.data.model.calendar.ReportCardsResponse
import com.example.fe.data.model.calendar.ReportCategoriesResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * 캘린더 화면에서 사용하는 API 요청을 정의한 인터페이스
 */
interface CalendarApiService {
    
    /**
     * 월별 가계부 내역 조회 API (GET 방식)
     * @param params 조회할 연도와 월 정보를 담은 맵
     * @param userId 사용자 ID
     * @param authToken 인증 토큰 (선택적)
     * @return 월별 가계부 내역 응답
     */
    @GET("api/budgetlog/log")
    suspend fun getMonthlyLog(
        @QueryMap params: Map<String, Int>,
        @Query("userId") userId: String,
        @Header("Authorization") authToken: String? = null
    ): Response<MonthlyLogResponse>
    
    /**
     * 월별 가계부 내역 조회 API (POST 방식)
     * @param body 조회할 연도와 월 정보를 담은 JSON 본문
     * @param userId 사용자 ID
     * @param authToken 인증 토큰 (선택적)
     * @return 월별 가계부 내역 응답
     */
    @POST("api/budgetlog/log")
    suspend fun postMonthlyLog(
        @Body body: RequestBody,
        @Query("userId") userId: String,
        @Header("Authorization") authToken: String? = null
    ): Response<MonthlyLogResponse>
    
    /**
     * 월별 일일 거래 기록 조회 API
     * @param params 조회할 연도와 월 정보를 담은 맵
     * @param userId 사용자 ID
     * @param authToken 인증 토큰 (선택적)
     * @return 일일 거래 기록 응답
     */
    @GET("api/budgetlog/transaction")
    suspend fun getMonthlyTransactions(
        @QueryMap params: Map<String, Int>,
        @Query("userId") userId: String,
        @Header("Authorization") authToken: String? = null
    ): Response<TransactionResponse>
    
    /**
     * 월간 거래 현황 조회 API
     * @param params 조회할 연도와 월 정보를 담은 맵
     * @param userId 사용자 ID
     * @param authToken 인증 토큰 (선택적)
     * @return 월간 거래 현황 응답
     */
    @GET("api/budgetlog/info")
    suspend fun getMonthlyLogInfo(
        @QueryMap params: Map<String, Int>,
        @Query("userId") userId: String,
        @Header("Authorization") authToken: String? = null
    ): Response<MonthlyInfoResponse>
    
    /**
     * 소비 리포트 조회 API
     * @param params 조회할 연도와 월 정보를 담은 맵
     * @param userId 사용자 ID
     * @param authToken 인증 토큰 (선택적)
     * @return 소비 리포트 응답
     */
    @GET("api/report")
    suspend fun getReportWithPattern(
        @QueryMap params: Map<String, Int>,
        @Query("userId") userId: String,
        @Header("Authorization") authToken: String? = null
    ): Response<ReportResponse>
    
    /**
     * 카드별 소비 및 혜택 조회 API
     * @param params 조회할 연도와 월 정보를 담은 맵
     * @param userId 사용자 ID
     * @param authToken 인증 토큰 (선택적)
     * @return 카드별 소비 및 혜택 응답
     */
    @GET("api/report/card")
    suspend fun getReportCards(
        @QueryMap params: Map<String, Int>,
        @Query("userId") userId: String,
        @Header("Authorization") authToken: String? = null
    ): Response<ReportCardsResponse>
    
    /**
     * 카테고리별 소비 및 혜택 조회 API
     * @param params 조회할 연도와 월 정보를 담은 맵
     * @param userId 사용자 ID
     * @param authToken 인증 토큰 (선택적)
     * @return 카테고리별 소비 및 혜택 응답
     */
    @GET("api/report/category")
    suspend fun getReportCategories(
        @QueryMap params: Map<String, Int>,
        @Query("userId") userId: String,
        @Header("Authorization") authToken: String? = null
    ): Response<ReportCategoriesResponse>
} 