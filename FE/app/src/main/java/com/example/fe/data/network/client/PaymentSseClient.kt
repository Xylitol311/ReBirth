package com.example.fe.data.network.client

import android.util.Log
import com.example.fe.config.AppConfig
import com.example.fe.data.model.payment.PaymentEvent
import com.example.fe.data.model.payment.PaymentStatus
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class PaymentSseClient {
    private val TAG = "PaymentSseClient"
    private val gson = Gson()
    private var eventSource: EventSource? = null
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(AppConfig.Timeout.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(AppConfig.Timeout.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(AppConfig.Timeout.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor { chain ->
            val request = chain.request()
            Log.e(TAG, "Request: ${request.method} ${request.url}")
            Log.e(TAG, "Headers: ${request.headers}")
            
            try {
                val response = chain.proceed(request)
                Log.e(TAG, "Response: ${response.code} ${response.message}")
                return@addInterceptor response
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                throw e
            }
        }
        .build()
    
    // SSE 연결 및 이벤트 수신을 Flow로 제공
    fun connectToPaymentEvents(userId: String): Flow<PaymentEvent> = callbackFlow {
        // 토큰 요청 API를 SSE 연결로 사용
        val sseUrl = "${AppConfig.Server.BASE_URL}${AppConfig.Server.Endpoints.PAYMENT_TOKEN}?userId=$userId"
        Log.e(TAG, "Connecting to SSE URL: $sseUrl")
        
        val request = Request.Builder()
            .url(sseUrl)
            .header("Accept", "text/event-stream")
            .build()
        
        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d(TAG, "SSE connection opened")
                // 연결 성공 이벤트 전송
                trySend(PaymentEvent(PaymentStatus.READY, "연결되었습니다"))
            }
            
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                Log.e(TAG, "SSE event received: id=$id, type=$type, data=$data")
                try {
                    // JSON 데이터를 PaymentEvent 객체로 변환
                    val paymentEvent = gson.fromJson(data, PaymentEvent::class.java)
                    trySend(paymentEvent)
                    
                    // 결제 완료 또는 실패, 취소, 만료 시 연결 종료
                    if (paymentEvent.status in listOf(
                            PaymentStatus.COMPLETED,
                            PaymentStatus.FAILED,
                            PaymentStatus.CANCELLED,
                            PaymentStatus.EXPIRED
                        )) {
                        eventSource.cancel()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing event data", e)
                    trySend(PaymentEvent(PaymentStatus.FAILED, "데이터 처리 오류"))
                }
            }
            
            override fun onClosed(eventSource: EventSource) {
                Log.d(TAG, "SSE connection closed")
                channel.close()
            }
            
            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                Log.e(TAG, "SSE connection failure: ${t?.message}")
                Log.e(TAG, "Response code: ${response?.code}, message: ${response?.message}")
                Log.e(TAG, "Response body: ${response?.body?.string()}")
                Log.e(TAG, "Stack trace:", t)
                
                trySend(PaymentEvent(PaymentStatus.FAILED, "연결 오류: ${t?.message}"))
                channel.close(t)
            }
        }
        
        eventSource = EventSources.createFactory(client).newEventSource(request, listener)
        this@PaymentSseClient.eventSource = eventSource

        // Flow가 취소되면 SSE 연결도 종료
        awaitClose {
            eventSource?.cancel()
            Log.d(TAG, "SSE connection cancelled")
        }
    }
    
    // SSE 연결 종료
    fun disconnect() {
        eventSource?.cancel()
        eventSource = null
        Log.d(TAG, "SSE connection manually disconnected")
    }
} 