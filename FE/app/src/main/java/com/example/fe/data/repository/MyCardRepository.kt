package com.example.fe.data.repository


import com.example.fe.data.model.myCard.CardTransactionHistoryResponse
import com.example.fe.data.model.myCard.MyCardInfoResponse
import com.example.fe.data.model.myCard.MyCardsResponse
import com.example.fe.data.network.api.MyCardApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 카드 관련 데이터를 관리하는 리포지토리
 */
@Singleton
class MyCardRepository @Inject constructor(
    private val apiService: MyCardApiService
) {
    // 캐시 저장소
    private val myCardsCache = ConcurrentHashMap<String, MyCardsResponse>()
    private val cardInfoCache = ConcurrentHashMap<Int, MyCardInfoResponse>()
    private val transactionCache = ConcurrentHashMap<String, CardTransactionHistoryResponse>()
    
    // 캐시 만료 시간 (밀리초)
    private val CACHE_EXPIRY_TIME = 5 * 60 * 1000L // 5분
    private val cacheTimestamps = ConcurrentHashMap<String, Long>()
    
    /**
     * 사용자의 모든 카드 목록을 가져옵니다.
     * @param token 사용자 인증 토큰
     * @param forceRefresh 강제로 새로고침할지 여부
     * @return 카드 목록 응답
     */
    suspend fun getMyCards(token: String, forceRefresh: Boolean = false): Result<MyCardsResponse> = withContext(Dispatchers.IO) {
        val cacheKey = "my_cards_$token"
        
        // 캐시가 유효하고 강제 새로고침이 아닌 경우 캐시된 데이터 반환
        if (!forceRefresh && isCacheValid(cacheKey) && myCardsCache.containsKey(cacheKey)) {
            return@withContext Result.success(myCardsCache[cacheKey]!!)
        }
        
        try {
            val response = apiService.getMyCards(token).execute()
            if (response.isSuccessful) {
                val data = response.body() ?: createErrorResponse("응답 데이터가 없습니다.")
                if (data.success) {
                    // 캐시 업데이트
                    myCardsCache[cacheKey] = data
                    updateCacheTimestamp(cacheKey)
                }
                Result.success(data)
            } else {
                Result.failure(Exception("카드 목록을 가져오는데 실패했습니다: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            // 네트워크 오류 시 캐시된 데이터가 있으면 반환
            if (myCardsCache.containsKey(cacheKey)) {
                Result.success(myCardsCache[cacheKey]!!)
            } else {
                Result.failure(Exception("카드 목록을 가져오는데 실패했습니다", e))
            }
        }
    }

    /**
     * 특정 카드의 상세 정보를 가져옵니다.
     * @param cardId 카드 ID
     * @param forceRefresh 강제로 새로고침할지 여부
     * @return 카드 상세 정보 응답
     */
    suspend fun getMyCardInfo(cardId: Int, forceRefresh: Boolean = false): Result<MyCardInfoResponse> = withContext(Dispatchers.IO) {
        val cacheKey = "card_info_$cardId"
        
        // 캐시가 유효하고 강제 새로고침이 아닌 경우 캐시된 데이터 반환
        if (!forceRefresh && isCacheValid(cacheKey) && cardInfoCache.containsKey(cardId)) {
            return@withContext Result.success(cardInfoCache[cardId]!!)
        }
        
        try {
            val response = apiService.getMyCardInfo(cardId).execute()
            if (response.isSuccessful) {
                val data = response.body() ?: createCardInfoErrorResponse("응답 데이터가 없습니다.")
                if (data.success) {
                    // 캐시 업데이트
                    cardInfoCache[cardId] = data
                    updateCacheTimestamp(cacheKey)
                }
                Result.success(data)
            } else {
                Result.failure(Exception("카드 정보를 가져오는데 실패했습니다: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            // 네트워크 오류 시 캐시된 데이터가 있으면 반환
            if (cardInfoCache.containsKey(cardId)) {
                Result.success(cardInfoCache[cardId]!!)
            } else {
                Result.failure(Exception("카드 정보를 가져오는데 실패했습니다", e))
            }
        }
    }

    /**
     * 특정 카드의 거래 내역을 가져옵니다.
     * @param token 사용자 인증 토큰
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
        forceRefresh: Boolean = false
    ): Result<CardTransactionHistoryResponse> = withContext(Dispatchers.IO) {
        val cacheKey = "transactions_${cardId}_${month}_${page}_${pageSize}"
        
        // 캐시가 유효하고 강제 새로고침이 아닌 경우 캐시된 데이터 반환
        if (!forceRefresh && isCacheValid(cacheKey) && transactionCache.containsKey(cacheKey)) {
            return@withContext Result.success(transactionCache[cacheKey]!!)
        }
        
        try {
            val response = apiService.getCardTransactionHistory(token, cardId, page, pageSize, month).execute()
            if (response.isSuccessful) {
                val data = response.body() ?: createTransactionErrorResponse("응답 데이터가 없습니다.")
                if (data.success) {
                    // 캐시 업데이트
                    transactionCache[cacheKey] = data
                    updateCacheTimestamp(cacheKey)
                }
                Result.success(data)
            } else {
                Result.failure(Exception("거래 내역을 가져오는데 실패했습니다: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            // 네트워크 오류 시 캐시된 데이터가 있으면 반환
            if (transactionCache.containsKey(cacheKey)) {
                Result.success(transactionCache[cacheKey]!!)
            } else {
                Result.failure(Exception("거래 내역을 가져오는데 실패했습니다", e))
            }
        }
    }
    
    /**
     * 모든 캐시를 지웁니다.
     */
    fun clearAllCache() {
        myCardsCache.clear()
        cardInfoCache.clear()
        transactionCache.clear()
        cacheTimestamps.clear()
    }
    
    /**
     * 특정 카드의 캐시를 지웁니다.
     */
    fun clearCardCache(cardId: Int) {
        cardInfoCache.remove(cardId)
        cacheTimestamps.remove("card_info_$cardId")
        
        // 해당 카드의 거래 내역 캐시도 지우기
        val keysToRemove = transactionCache.keys().toList().filter { it.contains("transactions_${cardId}_") }
        keysToRemove.forEach { key ->
            transactionCache.remove(key)
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

    // 에러 응답 생성 헬퍼 메서드
    private fun createErrorResponse(message: String): MyCardsResponse {
        return MyCardsResponse(success = false, message = message, data = null)
    }

    private fun createCardInfoErrorResponse(message: String): MyCardInfoResponse {
        return MyCardInfoResponse(success = false, message = message, data = null)
    }

    private fun createTransactionErrorResponse(message: String): CardTransactionHistoryResponse {
        return CardTransactionHistoryResponse(success = false, message = message, data = null)
    }
}