package com.example.fe.data.repository

import android.util.Log
import com.example.fe.config.AppConfig
import com.example.fe.data.model.myCard.CardTransactionHistoryResponse
import com.example.fe.data.model.myCard.MyCardInfoResponse
import com.example.fe.data.model.myCard.MyCardsResponse
import com.example.fe.data.network.api.CardTransactionHistoryRequest
import com.example.fe.data.network.api.MyCardApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

/**
 * 카드 관련 데이터를 관리하는 리포지토리
 */
class MyCardRepository {
    private val TAG = "MyCardRepository"
    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.Server.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    // 캐시 저장소
    private val cacheStore = ConcurrentHashMap<String, Any>()

    // 캐시 만료 시간 (밀리초)
    private val CACHE_EXPIRY_TIME = 5 * 60 * 1000L // 5분
    private val cacheTimestamps = ConcurrentHashMap<String, Long>()
    private val apiService = retrofit.create(MyCardApiService::class.java)
    /**
     * 사용자의 모든 카드 목록을 가져옵니다.
     * @param token 사용자 인증 토큰
     * @param forceRefresh 강제로 새로고침할지 여부
     * @return 카드 목록 응답
     */
    suspend fun getMyCards(token: String, forceRefresh: Boolean = false): Result<MyCardsResponse> {
        Log.d(TAG, "getMyCards 호출: forceRefresh=$forceRefresh")
        val cacheKey = generateCacheKey("my_cards", token)

        return fetchData(
            cacheKey = cacheKey,
            forceRefresh = forceRefresh,
            fetch = {
                Log.d(TAG, "API 호출: getMyCards")
                val response = apiService.getMyCards(token)
                // 응답 데이터 로깅 추가
                Log.d(TAG, "API 응답 받음: success=${response.success}, message=${response.message}")
                Log.d(TAG, "API 응답 데이터: data=${response.data}")

                // 데이터가 리스트인 경우 각 항목 로깅
                response.data?.forEachIndexed { index, card ->
                    Log.d(TAG, "카드[$index]: id=${card.cardId}, name=${card.cardName}, imgUrl=${card.cardImgUrl}")
                    Log.d(TAG, "카드[$index] 금액: totalSpending=${card.totalSpending}, maxSpending=${card.maxSpending}")
                    Log.d(TAG, "카드[$index] 혜택: receivedBenefit=${card.receivedBenefitAmount}, maxBenefit=${card.maxBenefitAmount}")
                }

                response
            },
            createErrorResponse = { message ->
                Log.e(TAG, "getMyCards 오류: $message")
                MyCardsResponse(success = false, message = message, data = emptyList())
            }
        )
    }

    /**
     * 특정 카드의 상세 정보를 가져옵니다.
     * @param cardId 카드 ID
     * @param year 조회할 연도
     * @param month 조회할 월
     * @param forceRefresh 강제로 새로고침할지 여부
     * @return 카드 상세 정보 응답
     */
    suspend fun getMyCardInfo(
        cardId: Int,
        year: Int,
        month: Int,
        forceRefresh: Boolean = false
    ): Result<MyCardInfoResponse> {
        Log.d(TAG, "getMyCardInfo 호출: cardId=$cardId, year=$year, month=$month, forceRefresh=$forceRefresh")
        val cacheKey = generateCacheKey("card_info", "$cardId-$year-$month")

        return fetchData(
            cacheKey = cacheKey,
            forceRefresh = forceRefresh,
            fetch = {
                Log.d(TAG, "API 호출: getMyCardInfo(cardId=$cardId, year=$year, month=$month)")
                apiService.getMyCardInfo(cardId, year, month)
            },
            createErrorResponse = { message ->
                Log.e(TAG, "getMyCardInfo 오류: $message")
                MyCardInfoResponse(success = false, message = message, data = null)
            }
        )
    }

    /**
     * 특정 카드의 거래 내역을 가져옵니다.
     * @param token 사용자 인증 토큰 (현재 사용되지 않음)
     * @param cardId 카드 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param month 조회할 월 (1-12)
     * @param forceRefresh 강제로 새로고침할지 여부
     * @return 거래 내역 응답
     */
    suspend fun getCardTransactionHistory(
        token: String,
        cardId: Int,
        page: Int,
        pageSize: Int,
        month: Int,
        forceRefresh: Boolean = false,
        year: Int = Calendar.getInstance().get(Calendar.YEAR) // 기본값으로 현재 연도 사용
    ): Result<CardTransactionHistoryResponse> {
        Log.d(TAG, "getCardTransactionHistory 호출: cardId=$cardId, page=$page, month=$month, year=$year")
        val cacheKey = generateCacheKey("transactions", cardId.toString(), month.toString(), year.toString(), page.toString(), pageSize.toString())

        return fetchData(
            cacheKey = cacheKey,
            forceRefresh = forceRefresh,
            fetch = {
                Log.d(TAG, "API 호출: getCardTransactionHistory(cardId=$cardId, page=$page, month=$month, year=$year)")
                // 요청 객체 생성
                val request = CardTransactionHistoryRequest(
                    cardId = cardId,
                    page = page,
                    pageSize = pageSize,
                    month = month,
                    year = year
                )
                apiService.getCardTransactionHistory(request)
            },
            createErrorResponse = { message ->
                Log.e(TAG, "getCardTransactionHistory 오류: $message")
                CardTransactionHistoryResponse(success = false, message = message, data = null)
            }
        )
    }
    /**
     * 모든 캐시를 지웁니다.
     */
    fun clearAllCache() {
        cacheStore.clear()
        cacheTimestamps.clear()
    }

    /**
     * 특정 카드의 캐시를 지웁니다.
     */
    fun clearCardCache(cardId: Int) {
        val cardInfoKey = generateCacheKey("card_info", cardId.toString())
        cacheStore.remove(cardInfoKey)
        cacheTimestamps.remove(cardInfoKey)

        // 해당 카드의 거래 내역 캐시도 지우기
        val keysToRemove = cacheStore.keys().toList().filter {
            it.startsWith(generateCacheKey("transactions", cardId.toString()))
        }
        keysToRemove.forEach { key ->
            cacheStore.remove(key)
            cacheTimestamps.remove(key)
        }
    }

    /**
     * 만료된 캐시를 지웁니다.
     */
    fun clearExpiredCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = cacheTimestamps.entries
            .filter { (currentTime - it.value) >= CACHE_EXPIRY_TIME }
            .map { it.key }

        expiredKeys.forEach { key ->
            cacheStore.remove(key)
            cacheTimestamps.remove(key)
        }
    }

    /**
     * 캐시가 유효한지 확인합니다.
     */
    private fun isCacheValid(key: String): Boolean {
        val timestamp = cacheTimestamps[key] ?: return false
        val currentTime = System.currentTimeMillis()
        return (currentTime - timestamp) < CACHE_EXPIRY_TIME
    }

    /**
     * 캐시 타임스탬프를 업데이트합니다.
     */
    private fun updateCacheTimestamp(key: String) {
        cacheTimestamps[key] = System.currentTimeMillis()
    }

    /**
     * 캐시 키를 생성합니다.
     */
    private fun generateCacheKey(prefix: String, vararg components: String): String {
        return listOf(prefix, *components).joinToString("_")
    }

    /**
     * 데이터를 가져오는 일반화된 메서드
     */
    private suspend inline fun <T> fetchData(
        cacheKey: String,
        forceRefresh: Boolean,
        crossinline fetch: suspend () -> T,
        crossinline createErrorResponse: (String) -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        // 캐시가 유효하고 강제 새로고침이 아닌 경우 캐시된 데이터 반환
        if (!forceRefresh && isCacheValid(cacheKey) && cacheStore.containsKey(cacheKey)) {
            @Suppress("UNCHECKED_CAST")
            return@withContext Result.success(cacheStore[cacheKey] as T)
        }

        try {
            val response = fetch()

            // 응답 성공 여부 확인 (success 필드가 있다고 가정)
            val isSuccess = when (response) {
                is MyCardsResponse -> response.success
                is MyCardInfoResponse -> response.success
                is CardTransactionHistoryResponse -> response.success
                else -> true
            }

            if (isSuccess) {
                // 캐시 업데이트
                cacheStore[cacheKey] = response!!
                updateCacheTimestamp(cacheKey)
            }

            Result.success(response)
        } catch (e: Exception) {
            // 네트워크 오류 시 캐시된 데이터가 있으면 반환
            if (cacheStore.containsKey(cacheKey)) {
                @Suppress("UNCHECKED_CAST")
                Result.success(cacheStore[cacheKey] as T)
            } else {
                Result.failure(Exception("데이터를 가져오는데 실패했습니다", e))
            }
        }
    }
}