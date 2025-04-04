package com.example.fe.data.network.api

import com.example.fe.data.model.cardRecommend.*
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * 카드 추천 관련 API 서비스 인터페이스
 */
interface CardRecommendApiService {

    /**
     * 전체 사용자에게 추천하는 TOP 3 카드 목록을 가져옵니다.
     *
     * @param userId 사용자 ID
     * @return 추천 카드 목록 응답
     */
    @GET("api/recommend/top3")
    suspend fun getTop3ForAll(
        @Query("userId") userId: Int
    ): ApiResponse<Top3ForAllResponse>

    /**
     * 카테고리별 추천 카드 목록을 가져옵니다.
     *
     * @param userId 사용자 ID
     * @return 카테고리별 추천 카드 목록 응답
     */
    @GET("api/recommend/category")
    suspend fun getTop3ForCategory(
        @Query("userId") userId: Int
    ): ApiResponse<List<CategoryRecommendation>>

    /**
     * 검색 매개변수에 따른 카드 목록을 가져옵니다.
     *
     * @param userId 사용자 ID
     * @param benefitType 혜택 타입 목록
     * @param cardCompany 카드사 이름 목록
     * @param category 카테고리
     * @param minPerformanceRange 최소 실적 범위
     * @param maxPerformanceRange 최대 실적 범위
     * @param minAnnualFee 최소 연회비
     * @param maxAnnualFee 최대 연회비
     * @return 검색된 카드 목록 응답
     */

    @POST("api/recommend/search")
    suspend fun searchByParameter(
        @Body parameters: CardSearchParameters
    ): ApiResponse<SearchByParameterResponse>

    /**
     * 인증된 사용자를 위한 검색 매개변수에 따른 카드 목록을 가져옵니다.
     *
     * @param token 사용자 인증 토큰
     * @param userId 사용자 ID
     * @param benefitType 혜택 타입 목록
     * @param cardCompany 카드사 이름 목록
     * @param category 카테고리
     * @param minPerformanceRange 최소 실적 범위
     * @param maxPerformanceRange 최대 실적 범위
     * @param minAnnualFee 최소 연회비
     * @param maxAnnualFee 최대 연회비
     * @return 검색된 카드 목록 응답
     */
    @GET("api/recommend/search")
    suspend fun searchByParameterWithAuth(
        @Header("Authorization") token: String,
        @Query("userId") userId: Int,
        @Query("benefitType") benefitType: List<String>? = null,
        @Query("cardCompany") cardCompany: List<String>? = null,
        @Query("category") category: List<String>? = null,
        @Query("minPerformanceRange") minPerformanceRange: Int? = null,
        @Query("maxPerformanceRange") maxPerformanceRange: Int? = null,
        @Query("minAnnualFee") minAnnualFee: Int? = null,
        @Query("maxAnnualFee") maxAnnualFee: Int? = null
    ): ApiResponse<SearchByParameterResponse>
}