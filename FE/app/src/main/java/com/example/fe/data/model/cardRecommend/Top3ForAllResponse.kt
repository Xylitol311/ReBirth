package com.example.fe.data.model.cardRecommend

import com.google.gson.annotations.SerializedName
import com.example.fe.data.model.cardRecommend.CardInfoApi

data class Top3ForAllResponse(
    val amount: Int,
    val recommendCards: List<CardInfoApi>?
) 