package com.example.fe.data.model

data class PreBenefitFeedbackResponse(
    val success: Boolean,
    val message: String,
    val data: PreBenefitFeedbackData
)

data class PreBenefitFeedbackData(
    val userId: Int,
    val paymentCardId: Int,
    val recommendedCardId: Int,
    val amount: Int,
    val ifBenefitType: String,
    val ifBenefitAmount: Int,
    val realBenefitType: String,
    val realBenefitAmount: Int,
    val merchantName: String,
    val isGood: Boolean
) 