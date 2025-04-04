package com.example.fe.data.network.api

import com.example.fe.data.model.myCard.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface MyCardApiService {

    // GET getMyCardInfo /api/cards/detail/{cardId}
    @GET("api/cards/detail/{cardId}")
    suspend fun getMyCardInfo(
        @Path("cardId") cardId: Int
    ): MyCardInfoResponse

    // GET getCardTransactionHistory /api/transactions/card-transaction
    @GET("api/transactions/card-transaction")
    suspend fun getCardTransactionHistory(
        @Header("Authorization") token: String,
        @Query("cardId") cardId: Int,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int,
        @Query("month") month: Int
    ): CardTransactionHistoryResponse

    // GET getMyCards /api/cards
    @GET("api/cards")
    suspend fun getMyCards(
        @Header("Authorization") token: String
    ): MyCardsResponse
}