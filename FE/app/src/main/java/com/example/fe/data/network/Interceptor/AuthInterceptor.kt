package com.example.fe.data.network.Interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

// 토큰 제공자 인터페이스
interface TokenProvider {
    fun getToken(): String
}
// 개선된 인터셉터
class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // 매 요청마다 최신 토큰을 가져옴
        val token = tokenProvider.getToken()
        Log.d("AuthInterceptor", "TokenProvider in Interceptor: $token")

        val requestBuilder = chain.request().newBuilder()

        if (token.isNotEmpty()) {
            val authHeader = "$token"
            requestBuilder.addHeader("Authorization", authHeader)
            Log.d("AuthInterceptor", "Authorization Header: $authHeader")
        }

        val request = requestBuilder.build()
        Log.d("AuthInterceptor", "Request URL: ${request.url}")
        Log.d("AuthInterceptor", "Request Headers:\n${request.headers.get("Authorization")}")

        return chain.proceed(request)
    }
}