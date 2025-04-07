package com.example.fe.data.repository

import com.example.fe.data.model.cardRecommend.Top3ForAllData
import com.example.fe.data.model.cardRecommend.Top3ForAllResponse
import com.example.fe.data.model.cardRecommend.Top3ForCategoryResponse
import com.example.fe.data.network.api.CardRecommendApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 카드 추천 관련 데이터를 관리하는 리포지토리
 */
@Singleton
class CardRecommendRepository @Inject constructor(
    private val apiService: CardRecommendApiService
) {
    /**
     * 전체 TOP 3 추천 카드를 가져옵니다.
     * @param userId 사용자 ID
     * @return TOP 3 추천 카드 응답
     */
    suspend fun getTop3ForAll(userId: Int = 1): Result<Top3ForAllResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTop3ForAll(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("TOP 3 추천 카드를 가져오는데 실패했습니다", e))
        }
    }

    /**
     * 카테고리별 추천 카드를 가져옵니다.
     * @param userId 사용자 ID
     * @return 카테고리별 추천 카드 응답
     */
    suspend fun getTop3ForCategory(userId: Int = 1): Result<Top3ForCategoryResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTop3ForCategory(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(Exception("카테고리별 추천 카드를 가져오는데 실패했습니다", e))
        }
    }

    // 에러 응답 생성 헬퍼 메서드
    private fun createTop3ForAllErrorResponse(message: String): Top3ForAllResponse {
        return Top3ForAllResponse(success = false, message = message, data = Top3ForAllData(amount = 0, cards = emptyList()))
    }

    private fun createTop3ForCategoryErrorResponse(message: String): Top3ForCategoryResponse {
        return Top3ForCategoryResponse(success = false, message = message, data = emptyList())
    }
} 