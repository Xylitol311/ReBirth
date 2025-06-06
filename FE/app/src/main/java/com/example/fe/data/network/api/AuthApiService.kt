package com.example.fe.data.network.api

import com.example.fe.data.model.auth.ApiResponseDTO
import com.example.fe.data.model.auth.PatternNumbersRequest
import com.example.fe.data.model.auth.ReportWithPatternDTO
import com.example.fe.data.model.auth.SendSmsRequest
import com.example.fe.data.model.auth.SignupRequest
import com.example.fe.data.model.auth.VerifySmsRequest
import com.example.fe.data.model.auth.userLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
interface AuthApiService {

    // 1차 회원가입
    @POST("api/auth/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<ApiResponseDTO<Unit>>

    // 패턴 등록 - 토큰은 이제 Interceptor에서 자동 추가됨
    @POST("api/auth/registpattern")
    suspend fun registPattern(
        @Body patternNumbers: PatternNumbersRequest
    ): Response<ApiResponseDTO<Unit>>


    @GET("api/report")
    suspend fun getReportWithPattern(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<ApiResponseDTO<ReportWithPatternDTO>>


    @POST("api/user/mydata/all")
    suspend fun loadAllMyData(): Response<ApiResponseDTO<Unit>>

    @POST("api/report/frommydata")
    suspend fun generateReportFromMyData(
    ): Response<ApiResponseDTO<Unit>>

    @POST("api/report/afterstart")
    suspend fun createReportAfterStart(
    ): Response<ApiResponseDTO<Unit>>


    // 로그인
    @POST("api/auth/login")
    suspend fun login(
        @Body request: userLoginRequest
    ): Response<ApiResponseDTO<Unit>>

    // sms 요청하기
    @POST("api/auth/sms")
    suspend fun sendSMS(
        @Body request: SendSmsRequest
    ): Response<ApiResponseDTO<String>>

    //sms 일치여부 확인하기
    // sms 요청하기
    @POST("api/auth/sms/verify")
    suspend fun verifySMS(
        @Body request: VerifySmsRequest
    ): Response<ApiResponseDTO<String>>



}
