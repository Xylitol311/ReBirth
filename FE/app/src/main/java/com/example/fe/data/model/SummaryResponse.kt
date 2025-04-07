package com.example.fe.data.model

data class SummaryResponse(
    val success: Boolean,
    val message: String,
    val data: SummaryData
)

data class SummaryData(
    val totalSpendingAmount: Int,
    val totalBenefitAmount: Int,
    val goodList: List<SpendingItem>,
    val badList: List<SpendingItem>
)

data class SpendingItem(
    val category: String,
    val amount: Int,
    val benefit: Int
) 