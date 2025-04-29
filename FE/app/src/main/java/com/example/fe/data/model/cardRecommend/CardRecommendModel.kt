package com.example.fe.data.model.cardRecommend

import com.google.gson.annotations.SerializedName

// 기본 API 응답 구조
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

// 카드 기본 정보 (여러 응답에서 공통으로 사용)
data class CardInfoApi(
    val cardId: Int,
    val cardName: String,

    @SerializedName("imgUrl", alternate = ["cardImgUrl"])
    val imageUrl: String,

    @SerializedName("cardInfo", alternate = ["cardDetailInfo"])
    val cardInfo: String,

    val score: Int? = null,
    val constellation: String? = null,

    // searchByParameter에서만 사용되는 필드들
    val cardTemplateId: Int? = null,
    val cardCompanyId: Int? = null,
    val annualFee: Int? = null,
    val cardType: String? = null,
    val cardConstellationInfo: String? = null,
    val performanceRange: List<Int>? = null
)

// top3ForAll 응답 구조는 Top3ForAllResponse.kt 파일에 정의되어 있습니다.

// top3ForCategory 응답 구조
data class CategoryRecommendation(
    val categoryId: Int,
    val categoryName: String,
    val amount: Int,
    val recommendCards: List<CardInfoApi>
)

data class Top3ForCategoryResponse(
    val categories: List<CategoryRecommendation>
)

// searchByParameter 응답 구조
typealias SearchByParameterResponse = List<CardInfoApi>

// 검색 매개변수 클래스 (요청에 사용)
data class CardSearchParameters(
    val benefitType: List<String> = emptyList(),
    val cardCompany: List<String> = emptyList(),
    val category: List<String> = emptyList(),
    val minPerformanceRange: Int = 0,
    val maxPerformanceRange: Int = Int.MAX_VALUE,
    val minAnnualFee: Int = 0,
    val maxAnnualFee: Int = Int.MAX_VALUE
) {
    override fun toString(): String {
        return "CardSearchParameters(" +
                "benefitType=$benefitType, " +
                "cardCompany=$cardCompany, " +
                "category=$category, " +
                "minPerformanceRange=$minPerformanceRange, " +
                "maxPerformanceRange=$maxPerformanceRange, " +
                "minAnnualFee=$minAnnualFee, " +
                "maxAnnualFee=$maxAnnualFee)"
    }
}