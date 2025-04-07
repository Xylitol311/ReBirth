package com.example.fe.data.model.calendar

import com.google.gson.annotations.SerializedName

/**
 * 월별 가계부 로그 응답 모델
 */
data class MonthlyLogResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<DailyLogData>
)

/**
 * 일별 가계부 데이터 모델
 */
data class DailyLogData(
    @SerializedName("day") val day: Int,
    @SerializedName("plus") val plus: Int,
    @SerializedName("minus") val minus: Int
)

/**
 * API 응답을 감싸는 제네릭 래퍼 클래스
 */
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T
)

/**
 * 일일 거래 기록 응답 모델
 */
data class TransactionResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<TransactionData>
)

/**
 * 거래 기록 데이터 모델
 */
data class TransactionData(
    @SerializedName("date") val date: String,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("amount") val amount: Int,
    @SerializedName("merchant_name") val merchantName: String?,
    @SerializedName("card_name") val cardName: String?
)

/**
 * 월간 거래 현황 응답 모델
 */
data class MonthlyInfoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: MonthlyInfoData
)

/**
 * 월간 거래 현황 데이터 모델
 */
data class MonthlyInfoData(
    @SerializedName("total_spending_amount") val totalSpendingAmount: Int,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("monthly_difference") val monthlyDifferenceAmount: Int
)

/**
 * 소비 리포트 응답 모델
 */
data class ReportResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ReportData
)

/**
 * 소비 리포트 데이터 모델
 */
data class ReportData(
    @SerializedName("total_spending_amount") val totalSpendingAmount: Int,
    @SerializedName("pre_total_spending_amount") val preTotalSpendingAmount: Int,
    @SerializedName("total_benefit_amount") val totalBenefitAmount: Int,
    @SerializedName("total_group_benefit_average") val totalGroupBenefitAverage: Int,
    @SerializedName("group_name") val groupName: String?,
    @SerializedName("report_description") val reportDescription: String,
    @SerializedName("consumption_patterns") val consumptionPatterns: ConsumptionPattern?
)

/**
 * 소비 패턴 데이터 모델
 */
data class ConsumptionPattern(
    @SerializedName("pattern_id") val patternId: String,
    @SerializedName("pattern_name") val patternName: String,
    @SerializedName("description") val description: String
)

/**
 * 카드별 소비 및 혜택 조회 응답 모델
 */
data class ReportCardsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<CardReport>
)

/**
 * 카드 리포트 데이터 모델
 */
data class CardReport(
    @SerializedName("name") val name: String,
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("total_amount") val totalAmount: Int,
    @SerializedName("total_benefit") val totalBenefit: Int,
    @SerializedName("categories") val categories: List<CardCategoryReport>
) {
    // API에서 가끔 totalAmount와 totalBenefit이 0으로 오는 경우 카테고리에서 직접 계산
    fun getCalculatedTotalAmount(): Int {
        // totalAmount가 0이 아니면 그대로 사용, 0이면 카테고리에서 계산
        return if (totalAmount != 0) totalAmount else categories.sumOf { 
            // amount가 음수로 오므로 절대값으로 변환
            if (it.amount < 0) -it.amount else it.amount 
        }
    }
    
    fun getCalculatedTotalBenefit(): Int {
        // totalBenefit이 0이 아니면 그대로 사용, 0이면 카테고리에서 계산
        return if (totalBenefit != 0) totalBenefit else categories.sumOf { it.benefit }
    }
}

/**
 * 카드의 카테고리별 소비 데이터 모델
 */
data class CardCategoryReport(
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("benefit") val benefit: Int,
    @SerializedName("count") val count: Int
)

/**
 * 카테고리별 소비 및 혜택 조회 응답 모델
 */
data class ReportCategoriesResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<CategoryReport>
)

/**
 * 카테고리 리포트 데이터 모델
 */
data class CategoryReport(
    @SerializedName("category") val category: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("benefit") val benefit: Int
) {
    // amount가 음수로 오는 경우 절대값으로 변환하여 반환
    fun getAbsoluteAmount(): Int {
        return if (amount < 0) -amount else amount
    }
} 