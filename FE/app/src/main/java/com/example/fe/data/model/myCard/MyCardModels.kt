package com.example.fe.data.model.myCard

data class MyCardInfoResponse(
    val success: Boolean,
    val message: String,
    val data: MyCardInfoData?
)

data class MyCardInfoData(
    val cardId: Int,
    val cardImageUrl: String,  // cardImgUrl에서 cardImageUrl로 변경
    val cardName: String,
    val maxPerformanceAmount: Int,
    val currentPerformanceAmount: Int,
    val spendingMaxTier: Int,
    val currentSpendingTier: Int,
    val amountRemainingNext: Int,
    val performanceRange: List<Int>,  // 추가된 필드
    val cardBenefits: List<CardBenefit>,
    val lastMonthPerformance: Int
)

data class CardBenefit(
    val benefitCategory: List<String>,  // String에서 List<String>으로 변경
    val receivedBenefitAmount: Int,
    val maxBenefitAmount : Int
)

data class CardTransactionHistoryResponse(
    val success: Boolean,
    val message: String,
    val data: CardTransactionHistoryData?
)

data class CardTransactionHistoryData(
    val transactionHistory: List<Transaction>,
    val pagination: Pagination
)

data class Transaction(
    val transactionDate: String,
    val transactionCategory: String?,  // null 가능하도록 변경
    val spendingAmount: Int,
    val merchantName: String?,  // null 가능하도록 변경
    val receivedBenefitAmount: Int?  // null 가능하도록 변경
)

data class Pagination(
    val currentPage: Int,
    val pageSize: Int,
    val hasMore: Boolean
)

//유지
data class MyCardsResponse(
    val success: Boolean,
    val message: String,
    val data: List<MyCardData>?
)

data class MyCardData(
    val cardId: Int,
    val cardImgUrl: String,
    val cardName: String,
    val totalSpending: Int,
    val maxSpending: Int,
    val receivedBenefitAmount: Int,
    val maxBenefitAmount: Int,
    val performanceRange: List<Int>
)

data class SetMyCardsOrderResponse(
    val success: Boolean,
    val message: String,
    val data: Any? // null이므로 Any?로 처리
)

data class MostFrequentlyUsedCardResponse(
    val success: Boolean,
    val message: String,
    val data: CardUsageData?
)

data class CardUsageData(
    val cardId: Int,
    val cardImgUrl: String,
    val character: String,
    val cardName: String,
    val usageCount: Int,
    val totalSpending: Int,
    val totalBenefitAmount: Int,
    val topBenefitCategory: String
)

data class LowBenefitsCardResponse(
    val success: Boolean,
    val message: String,
    val data: CardUsageData?
)

data class LowUsageRateCardResponse(
    val success: Boolean,
    val message: String,
    val data: CardUsageData?
)

data class DeleteCardResponse(
    val success: Boolean,
    val message: String,
    val data: Any? // null이므로 Any?로 처리
)

data class AllCardProductsResponse(
    val success: Boolean,
    val message: String,
    val data: CardProductData?
)

data class CardProductData(
    val cardTemplateId: Int,
    val cardImgUrl: String,
    val cardName: String
)

data class SearchCardProductsResponse(
    val success: Boolean,
    val message: String,
    val data: CardProductData?
)

data class CardProductInfoResponse(
    val success: Boolean,
    val message: String,
    val data: CardProductInfoData?
)

data class CardProductInfoData(
    val cardTemplateId: Int,
    val cardImgUrl: String,
    val annualFee: Int,
    val benefitPerformance: BenefitPerformance,
    val characterName: String,
    val characterImgUrl: String,
    val characterStory: String,
    val characterEffect: String
)

data class BenefitPerformance(
    val spendingMaxTier: Int,
    val maxPerformanceAmount: Int
)

typealias HighestSpendingCardResponse = MostFrequentlyUsedCardResponse
typealias MostBenefitsCardResponse = MostFrequentlyUsedCardResponse