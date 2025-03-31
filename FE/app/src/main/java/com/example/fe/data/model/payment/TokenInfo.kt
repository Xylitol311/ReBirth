package com.example.fe.data.model.payment

data class TokenInfo(
    val token: String,
    val cardId: String? = null,
    val cardName: String? = null,
    val cardImgUrl: String? = null,
    val cardConstellationInfo: String? = null
) 