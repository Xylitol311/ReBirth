package com.example.fe.data.network.api

import com.example.fe.data.model.cardRecommend.Top3ForAllResponse
import com.example.fe.data.model.cardRecommend.Top3ForCategoryResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CardRecommendApiService {
    // 추천 TOP 3 카드 조회
    @GET("api/recommend/top3")
    suspend fun getTop3ForAll(
        @Query("userId") userId: Int = 1 // 기본값 1 설정
    ): Top3ForAllResponse
    
    // 카테고리별 추천 카드 조회
    @GET("api/recommend/category")
    suspend fun getTop3ForCategory(
        @Query("userId") userId: Int = 1 // 기본값 1 설정
    ): Top3ForCategoryResponse
} 