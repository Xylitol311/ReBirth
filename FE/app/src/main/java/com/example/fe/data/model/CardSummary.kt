package com.example.fe.data.model

data class CardSummary(
    val cardId: Int,
    val cardName: String,
    val cardImgUrl: String,
    val spendingAmount: Int,
    val benefitAmount: Int,
    val annualFee: Int
) 