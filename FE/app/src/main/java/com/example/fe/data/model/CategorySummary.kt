package com.example.fe.data.model

data class CategorySummary(
    val category: String,
    val amount: Int,
    val benefit: Int,
    val totalAmount: Int // 전체 소비 금액
) 