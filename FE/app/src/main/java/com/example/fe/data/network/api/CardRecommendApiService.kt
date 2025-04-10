package com.example.fe.data.network.api

import com.example.fe.data.model.cardRecommend.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 카드 추천 관련 API 서비스 인터페이스
 */
interface CardRecommendApiService {

    /**
     * 전체 사용자에게 추천하는 TOP 3 카드 목록을 가져옵니다.
     *
     * @return 추천 카드 목록 응답
     */
    @GET("api/recommend/top3")
    suspend fun getTop3ForAll(): ApiResponse<Top3ForAllResponse>

    /**
     * 카테고리별 추천 카드 목록을 가져옵니다.
     *
     * @return 카테고리별 추천 카드 목록 응답
     */
    @GET("api/recommend/category")
    suspend fun getTop3ForCategory(): ApiResponse<List<CategoryRecommendation>>

    /**
     * 검색 매개변수에 따른 카드 목록을 가져옵니다.
     *
     * @param parameters 검색 매개변수
     * @return 검색된 카드 목록 응답
     */
    @POST("api/recommend/search")
    suspend fun searchByParameter(
        @Body parameters: CardSearchParameters
    ): ApiResponse<SearchByParameterResponse>
}