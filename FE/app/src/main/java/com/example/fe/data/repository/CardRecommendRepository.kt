package com.example.fe.data.repository

import android.util.Log
import com.example.fe.config.AppConfig
import com.example.fe.data.model.cardRecommend.*
import com.example.fe.data.network.api.CardRecommendApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 카드 추천 관련 데이터를 관리하는 리포지토리
 */
class CardRecommendRepository {
    private val TAG = "CardRecommendRepository"

    // Retrofit 인스턴스 생성
    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.Server.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // API 서비스 생성
    private val apiService = retrofit.create(CardRecommendApiService::class.java)

    // 캐시 저장소
    private val cacheStore = ConcurrentHashMap<String, Any>()

    // 캐시 만료 시간 (밀리초)
    private val CACHE_EXPIRY_TIME = 5 * 60 * 1000L // 5분
    private val cacheTimestamps = ConcurrentHashMap<String, Long>()

    /**
     * 전체 사용자에게 추천하는 TOP 3 카드 목록을 가져옵니다.
     *
     * @param userId 사용자 ID
     * @param forceRefresh 강제로 새로고침할지 여부
     * @return 추천 카드 목록 응답
     */
    suspend fun getTop3ForAll(userId: Int, forceRefresh: Boolean = false): Result<Top3ForAllResponse> {
        Log.d(TAG, "getTop3ForAll 호출: userId=$userId, forceRefresh=$forceRefresh")
        val cacheKey = "top3_for_all_$userId"

        // 전체 API URL 로그 추가
        val apiUrl = "${AppConfig.Server.BASE_URL}api/recommend/top3?userId=$userId"
        Log.d(TAG, "API 엔드포인트 URL: $apiUrl")

        return fetchData(
            cacheKey = cacheKey,
            forceRefresh = forceRefresh,
            fetch = {
                Log.d(TAG, "API 호출: getTop3ForAll")
                val response = apiService.getTop3ForAll(userId)
                if (response.success) {
                    response.data ?: throw Exception("데이터가 null입니다")
                } else {
                    throw Exception(response.message)
                }
            }
        )
    }

    /**
     * 카테고리별 추천 카드 목록을 가져옵니다.
     *
     * @param userId 사용자 ID
     * @param forceRefresh 강제로 새로고침할지 여부
     * @return 카테고리별 추천 카드 목록 응답
     */
    suspend fun getTop3ForCategory(userId: Int, forceRefresh: Boolean = false): Result<List<CategoryRecommendation>> {
        Log.d(TAG, "getTop3ForCategory 호출: userId=$userId, forceRefresh=$forceRefresh")
        val cacheKey = "top3_for_category_$userId"

        // 전체 API URL 로그 추가
        val apiUrl = "${AppConfig.Server.BASE_URL}api/recommend/category?userId=$userId"
        Log.d(TAG, "API 엔드포인트 URL: $apiUrl")

        return fetchData(
            cacheKey = cacheKey,
            forceRefresh = forceRefresh,
            fetch = {
                Log.d(TAG, "API 호출: getTop3ForCategory")
                val response = apiService.getTop3ForCategory(userId)
                if (response.success) {
                    response.data ?: throw Exception("데이터가 null입니다")
                } else {
                    throw Exception(response.message)
                }
            }
        )
    }

    /**
     * 검색 매개변수에 따른 카드 목록을 가져옵니다.
     *
     * @param userId 사용자 ID
     * @param parameters 검색 매개변수
     * @param forceRefresh 강제로 새로고침할지 여부
     * @return 검색된 카드 목록 응답
     */
    suspend fun searchByParameter(
        parameters: CardSearchParameters,
        forceRefresh: Boolean = false
    ): Result<SearchByParameterResponse> {
        Log.d(TAG, "searchByParameter 호출: parameters=$parameters, forceRefresh=$forceRefresh")

        // 캐시 키 생성
        val cacheKey = generateCacheKey(
            "search",
            parameters.benefitType.joinToString(","),
            parameters.cardCompany.joinToString(","),
            parameters.category.joinToString(","),
            parameters.minPerformanceRange.toString(),
            parameters.maxPerformanceRange.toString(),
            parameters.minAnnualFee.toString(),
            parameters.maxAnnualFee.toString()
        )

        // 전체 API URL 로그 추가
        val apiUrl = "${AppConfig.Server.BASE_URL}api/recommend/search"
        Log.d(TAG, "API 엔드포인트 URL: $apiUrl")

        return fetchData(
            cacheKey = cacheKey,
            forceRefresh = forceRefresh,
            fetch = {
                Log.d(TAG, "API 호출: searchByParameter")

                // API 호출 (Body 사용)
                val response = apiService.searchByParameter(parameters)

                if (response.success) {
                    response.data ?: throw Exception("데이터가 null입니다")
                } else {
                    throw Exception(response.message)
                }
            }
        )
    }
    /**
     * 인증된 사용자를 위한 검색 매개변수에 따른 카드 목록을 가져옵니다.
     *
     * @param userId 사용자 ID
     * @param token 사용자 인증 토큰
     * @param parameters 검색 매개변수
     * @param forceRefresh 강제로 새로고침할지 여부
     * @return 검색된 카드 목록 응답
     */
    suspend fun searchByParameterWithAuth(
        userId: Int,
        token: String,
        parameters: CardSearchParameters,
        forceRefresh: Boolean = false
    ): Result<SearchByParameterResponse> {
        Log.d(TAG, "searchByParameterWithAuth 호출: userId=$userId, token=$token, parameters=$parameters, forceRefresh=$forceRefresh")

        // 캐시 키 생성 (사용자 ID, 토큰과 매개변수를 포함)
        val cacheKey = generateCacheKey(
            "search_auth",
            userId.toString(),
            token,
            parameters.benefitType?.joinToString(",") ?: "",
            parameters.cardCompany?.joinToString(",") ?: "",
            parameters.category?.joinToString(",") ?: "",
            parameters.minPerformanceRange?.toString() ?: "",
            parameters.maxPerformanceRange?.toString() ?: "",
            parameters.minAnnualFee?.toString() ?: "",
            parameters.maxAnnualFee?.toString() ?: ""
        )

        // 전체 API URL 로그 추가
        val apiUrl = "${AppConfig.Server.BASE_URL}api/recommend/search?userId=$userId"
        Log.d(TAG, "API 엔드포인트 URL: $apiUrl")

        return fetchData(
            cacheKey = cacheKey,
            forceRefresh = forceRefresh,
            fetch = {
                Log.d(TAG, "API 호출: searchByParameterWithAuth")
                val response = apiService.searchByParameterWithAuth(
                    token = token,
                    userId = userId,
                    benefitType = parameters.benefitType,
                    cardCompany = parameters.cardCompany,
                    category = parameters.category,
                    minPerformanceRange = parameters.minPerformanceRange,
                    maxPerformanceRange = parameters.maxPerformanceRange,
                    minAnnualFee = parameters.minAnnualFee,
                    maxAnnualFee = parameters.maxAnnualFee
                )
                if (response.success) {
                    response.data ?: throw Exception("데이터가 null입니다")
                } else {
                    throw Exception(response.message)
                }
            }
        )
    }
    /**
     * 캐시를 초기화합니다.
     */
    fun clearCache() {
        Log.d(TAG, "캐시 초기화")
        cacheStore.clear()
        cacheTimestamps.clear()
    }

    /**
     * 특정 캐시를 초기화합니다.
     *
     * @param cacheKey 캐시 키
     */
    fun clearCache(cacheKey: String) {
        Log.d(TAG, "캐시 초기화: $cacheKey")
        cacheStore.remove(cacheKey)
        cacheTimestamps.remove(cacheKey)
    }

    /**
     * 캐시가 유효한지 확인합니다.
     *
     * @param cacheKey 캐시 키
     * @return 캐시 유효 여부
     */
    private fun isCacheValid(cacheKey: String): Boolean {
        val timestamp = cacheTimestamps[cacheKey] ?: return false
        val now = System.currentTimeMillis()
        val isValid = (now - timestamp) < CACHE_EXPIRY_TIME
        Log.d(TAG, "캐시 유효성 확인: $cacheKey, 유효=$isValid")
        return isValid
    }

    /**
     * 캐시 타임스탬프를 업데이트합니다.
     *
     * @param cacheKey 캐시 키
     */
    private fun updateCacheTimestamp(cacheKey: String) {
        cacheTimestamps[cacheKey] = System.currentTimeMillis()
    }

    /**
     * 캐시 키를 생성합니다.
     *
     * @param prefix 접두사
     * @param components 구성 요소
     * @return 캐시 키
     */
    private fun generateCacheKey(prefix: String, vararg components: String): String {
        return listOf(prefix, *components).joinToString("_")
    }

    /**
     * 데이터를 가져오는 일반화된 메서드
     *
     * @param cacheKey 캐시 키
     * @param forceRefresh 강제로 새로고침할지 여부
     * @param fetch 데이터를 가져오는 함수
     * @return 결과
     */
    private suspend inline fun <T : Any> fetchData(
        cacheKey: String,
        forceRefresh: Boolean,
        crossinline fetch: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        Log.d(TAG, "fetchData 시작: cacheKey=$cacheKey, forceRefresh=$forceRefresh")

        // 캐시가 유효하고 강제 새로고침이 아닌 경우 캐시된 데이터 반환
        if (!forceRefresh && isCacheValid(cacheKey) && cacheStore.containsKey(cacheKey)) {
            Log.d(TAG, "캐시에서 데이터 반환: $cacheKey")
            @Suppress("UNCHECKED_CAST")
            return@withContext Result.success(cacheStore[cacheKey] as T)
        }

        try {
            Log.d(TAG, "네트워크에서 데이터 가져오기 시작")
            val response = fetch()
            Log.d(TAG, "네트워크 응답 수신: $response")

            // 캐시 업데이트
            cacheStore[cacheKey] = response
            updateCacheTimestamp(cacheKey)
            Log.d(TAG, "캐시 업데이트: $cacheKey")

            Result.success(response)
        } catch (e: Exception) {
            if (cacheStore.containsKey(cacheKey)) {
                Log.d(TAG, "네트워크 오류 발생, 캐시에서 데이터 반환: $cacheKey")
                @Suppress("UNCHECKED_CAST")
                Result.success(cacheStore[cacheKey] as T)
            } else {
                Log.e(TAG, "네트워크 오류 발생, 캐시 없음: $cacheKey")
                Result.failure(e)
            }
        }
    }
}