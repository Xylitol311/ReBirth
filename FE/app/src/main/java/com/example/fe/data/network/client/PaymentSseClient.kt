package com.example.fe.data.network.client

import android.util.Log
import com.example.fe.config.AppConfig
import com.example.fe.data.model.payment.PaymentEvent
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
import java.util.UUID

class PaymentSseClient {
    private val TAG = "PaymentSseClient"
    private var eventSource: EventSource? = null
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(AppConfig.Timeout.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(AppConfig.Timeout.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    // SSE 연결 및 이벤트 수신을 Flow로 제공
    fun connectToPaymentEvents(): Flow<PaymentEvent> = callbackFlow {
        // 고유한 UUID 생성
        val sessionUuid = UUID.randomUUID().toString()

        // UUID를 쿼리 파라미터로 사용
        val sseUrl = "${AppConfig.Server.BASE_URL}${AppConfig.Server.Endpoints.PAYMENT_EVENTS}?sessionId=$sessionUuid"
        Log.d(TAG, "Connecting to SSE URL with UUID: $sseUrl")
        
        val request = Request.Builder()
            .url(sseUrl)
            .header("Accept", "text/event-stream")
            .build()
        
        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d(TAG, "SSE connection opened successfully")
                trySend(PaymentEvent("연결되었습니다"))
            }
            
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                Log.d(TAG, "SSE event received: id=$id, type=$type, data=$data")
                
                // 이벤트 타입에 따라 다른 처리
                val eventType = type ?: "unknown"
                Log.d(TAG, "Processing event type: $eventType")
                
                // eventType과 eventId를 포함하여 PaymentEvent 객체 생성
                val paymentEvent = PaymentEvent(
                    message = data,
                    eventType = eventType,
                    eventId = id
                )
                Log.d(TAG, "Created PaymentEvent: $paymentEvent")
                
                val result = trySend(paymentEvent)
                Log.d(TAG, "Event send result: $result")
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
                
                // 응답 본문 로깅 (안전하게)
                response?.body?.let {
                    try {
                        val bodyString = it.string()
                        Log.e(TAG, "Response body: $bodyString")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to read response body", e)
                    }
                }
                
                Log.e(TAG, "Stack trace:", t)

                // 타임아웃 오류인 경우 특별 처리
                val isTimeout = t?.message?.contains("timeout") == true

                trySend(PaymentEvent("연결 오류: ${t?.message}"))

                // 타임아웃이 아닌 경우에만 Flow 종료
                if (!isTimeout) {
                    channel.close(t)
                } else {
                    // 타임아웃인 경우 기존 eventSource 정리만 하고 Flow는 유지
                    this@PaymentSseClient.eventSource?.cancel()
                    this@PaymentSseClient.eventSource = null
                    Log.d(TAG, "타임아웃 발생, eventSource만 정리 (Flow 유지)")
                }
            }
        }
        
        try {
            eventSource = EventSources.createFactory(client).newEventSource(request, listener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create EventSource", e)
            // 타임아웃 오류인 경우 특별 처리
            val isTimeout = e.message?.contains("timeout") == true

            trySend(PaymentEvent("EventSource 생성 실패: ${e.message}"))
            // 타임아웃이 아닌 경우에만 Flow 종료
            if (!isTimeout) {
                channel.close(e)
            }
        }
        
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
