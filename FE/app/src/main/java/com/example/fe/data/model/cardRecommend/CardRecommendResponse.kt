package com.example.fe.data.model.cardRecommend

import com.google.gson.annotations.SerializedName

data class Top3ForAllResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: Top3ForAllData
)

data class Top3ForAllData(
    @SerializedName("amount") val amount: Int,
    @SerializedName("cards") val cards: List<RecommendCard>?
)

data class RecommendCard(
    @SerializedName("cardId") val cardId: Int,
    @SerializedName("cardName") val cardName: String,
    @SerializedName("imgUrl") val imgUrl: String = "",
    @SerializedName("cardInfo") val cardInfo: String = "",
    @SerializedName("score") val score: Int = 0,
    @SerializedName("constellation") val constellation: Any? = null
) {
    // 이미지 URL 유효성 검사 메서드
    fun hasValidImageUrl(): Boolean = imgUrl.isNotEmpty() && imgUrl.startsWith("http")
    
    // 이미지 URL 정규화
    fun normalizedImageUrl(): String {
        // URL이 비어있거나 이미 http로 시작하면 그대로 반환
        if (imgUrl.isEmpty() || imgUrl.startsWith("http")) return imgUrl
        
        // 상대 경로인 경우 기본 URL 추가
        return "https://a602rebirth.s3.ap-northeast-2.amazonaws.com/card_img/$imgUrl"
    }
    
    // 이름 가져오기
    fun name(): String = cardName
}

data class Top3ForCategoryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<CategoryRecommendation>
)

data class CategoryRecommendation(
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("recommendCards") val recommendCards: List<RecommendCard>
) 