package com.example.fe.data.network

import android.util.Log
import com.example.fe.config.AppConfig
import com.example.fe.data.network.Interceptor.AuthInterceptor
import com.example.fe.data.network.Interceptor.TokenProvider
import com.example.fe.data.network.api.AuthApiService
import com.example.fe.data.network.api.HomeApiService
import com.example.fe.data.network.api.MyCardApiService
import com.example.fe.data.network.api.PaymentApiService
import com.example.fe.data.network.api.SummaryService
import com.example.fe.data.network.api.CalendarApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// 네트워크 클라이언트 싱글톤 객체
object NetworkClient {
    // 토큰 제공자 저장 - public으로 변경
    lateinit var tokenProvider: TokenProvider

    // 문자열 토큰 대신 TokenProvider 인터페이스를 받도록 변경
    fun init(tokenProvider: TokenProvider) {
        this.tokenProvider = tokenProvider

        Log.d("AuthRetrofit", "TokenProvider initialized")

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃
            .readTimeout(60, TimeUnit.SECONDS)    // 서버로부터 응답을 기다리는 시간
            .addInterceptor(AuthInterceptor(tokenProvider))
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(AppConfig.Server.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        authApiService = retrofit.create(AuthApiService::class.java)
        paymentApiService = retrofit.create(PaymentApiService::class.java)
        myCardApiService = retrofit.create(MyCardApiService::class.java)
        homeApiService = retrofit.create(HomeApiService::class.java)
        summaryService = retrofit.create(SummaryService::class.java)
        calendarApiService = retrofit.create(CalendarApiService::class.java)
    }
    private lateinit var retrofit: Retrofit
    lateinit var authApiService: AuthApiService
    lateinit var paymentApiService: PaymentApiService
    lateinit var myCardApiService: MyCardApiService
    lateinit var homeApiService: HomeApiService
    lateinit var summaryService: SummaryService
    lateinit var calendarApiService: CalendarApiService
}

