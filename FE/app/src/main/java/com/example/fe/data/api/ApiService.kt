package com.example.fe.data.api

import com.example.fe.data.model.ApiResponse
import com.example.fe.data.model.cardRecommend.CardInfoApi
import com.example.fe.data.model.cardRecommend.Top3ForAllResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("cards/search")
    suspend fun searchCards(@Query("keyword") keyword: String): Response<ApiResponse<List<CardInfoApi>>>

    @GET("cards/top3-for-all")
    suspend fun getTop3ForAll(): Response<Top3ForAllResponse>
} 