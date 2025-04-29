package com.example.fe.data.network.api

import com.example.fe.data.model.myCard.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface MyCardApiService {

    // 수정된 코드
    @GET("api/cards/detail/{cardId}/{year}/{month}")
    suspend fun getMyCardInfo(
        @Path("cardId") cardId: Int,
        @Path("year") year: Int,
        @Path("month") month: Int
    ): MyCardInfoResponse

    // POST getCardTransactionHistory /api/card/history
    @POST("api/card/history")
    suspend fun getCardTransactionHistory(
        @Body request: CardTransactionHistoryRequest
    ): CardTransactionHistoryResponse

    // GET getMyCards /api/cards
    @GET("api/cards")
    suspend fun getMyCards(
    ): MyCardsResponse
}

// 거래 내역 요청 모델 추가
data class CardTransactionHistoryRequest(
    val cardId: Int,
    val page: Int,
    val pageSize: Int,
    val month: Int,
    val year: Int
)