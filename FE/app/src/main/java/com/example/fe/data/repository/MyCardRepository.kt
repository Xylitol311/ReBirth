package com.example.fe.data.repository

import android.util.Log
import com.example.fe.config.AppConfig
import com.example.fe.data.model.myCard.CardTransactionHistoryResponse
import com.example.fe.data.model.myCard.MyCardInfoResponse
import com.example.fe.data.model.myCard.MyCardsResponse
import com.example.fe.data.network.NetworkClient
import com.example.fe.data.network.api.CardTransactionHistoryRequest
import com.example.fe.data.network.api.MyCardApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

/**
 * 카드 관련 데이터를 관리하는 리포지토리
 */
class MyCardRepository {
    private val TAG = "MyCardRepository"
    private val apiService = NetworkClient.myCardApiService

    /**
     * 사용자의 모든 카드 목록을 가져옵니다.
     * @param token 사용자 인증 토큰
     * @return 카드 목록 응답
     */
    suspend fun getMyCards(): Result<MyCardsResponse> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getMyCards 호출")

        try {
            Log.d(TAG, "API 호출: getMyCards")
            val response = apiService.getMyCards()

            // 응답 데이터 로깅 추가
            Log.d(TAG, "API 응답 받음: success=${response.success}, message=${response.message}")
            Log.d(TAG, "API 응답 데이터: data=${response.data}")

            // 데이터가 리스트인 경우 각 항목 로깅
            response.data?.forEachIndexed { index, card ->
                Log.d(TAG, "카드[$index]: id=${card.cardId}, name=${card.cardName}, imgUrl=${card.cardImgUrl}")
                Log.d(TAG, "카드[$index] 금액: totalSpending=${card.totalSpending}, maxSpending=${card.maxSpending}")
                Log.d(TAG, "카드[$index] 혜택: receivedBenefit=${card.receivedBenefitAmount}, maxBenefit=${card.maxBenefitAmount}")
            }

            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "getMyCards 오류: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 특정 카드의 상세 정보를 가져옵니다.
     * @param cardId 카드 ID
     * @param year 조회할 연도
     * @param month 조회할 월
     * @return 카드 상세 정보 응답
     */
    suspend fun getMyCardInfo(
        cardId: Int,
        year: Int,
        month: Int
    ): Result<MyCardInfoResponse> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getMyCardInfo 호출: cardId=$cardId, year=$year, month=$month")

        try {
            Log.d(TAG, "API 호출: getMyCardInfo(cardId=$cardId, year=$year, month=$month)")
            val response = apiService.getMyCardInfo(cardId, year, month)
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "getMyCardInfo 오류: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 특정 카드의 거래 내역을 가져옵니다.
     * @param token 사용자 인증 토큰 (현재 사용되지 않음)
     * @param cardId 카드 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @param month 조회할 월 (1-12)
     * @param year 조회할 연도
     * @return 거래 내역 응답
     */
    suspend fun getCardTransactionHistory(
        token: String,
        cardId: Int,
        page: Int,
        pageSize: Int,
        month: Int,
        year: Int = Calendar.getInstance().get(Calendar.YEAR) // 기본값으로 현재 연도 사용
    ): Result<CardTransactionHistoryResponse> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getCardTransactionHistory 호출: cardId=$cardId, page=$page, month=$month, year=$year")

        try {
            Log.d(TAG, "API 호출: getCardTransactionHistory(cardId=$cardId, page=$page, month=$month, year=$year)")
            // 요청 객체 생성
            val request = CardTransactionHistoryRequest(
                cardId = cardId,
                page = page,
                pageSize = pageSize,
                month = month,
                year = year
            )
            val response = apiService.getCardTransactionHistory(request)
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "getCardTransactionHistory 오류: ${e.message}")
            Result.failure(e)
        }
    }
}