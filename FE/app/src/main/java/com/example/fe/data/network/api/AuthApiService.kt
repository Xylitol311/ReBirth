package com.example.fe.data.network.api

import com.example.fe.data.model.auth.ApiResponseDTO
import com.example.fe.data.model.auth.SignupRequest
import com.example.fe.data.model.auth.registPatternRequest
import com.example.fe.data.model.auth.userLoginRequest
import com.example.fe.data.model.payment.ApiResponse
import com.example.fe.data.model.payment.PaymentResult
import com.example.fe.data.model.payment.QRPaymentResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {

    //1차 회원가입
    @POST("api/auth/signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<ApiResponseDTO<Unit>>

    //패턴 등록 시
    @POST("api/auth/registpattern")
    suspend fun registPattern(
        @Header("Authorization") token: String, // JWT 토큰
        @Body request: registPatternRequest // 요청 바디
    ): Response<ApiResponseDTO<Unit>> // Unit은 Void에 해당

    //로그인
    @POST("api/auth/login")
    suspend fun login(
        @Body request: userLoginRequest // 요청 바디
    ): Response<ApiResponseDTO<Unit>> // Unit은 Void에 해당

}
