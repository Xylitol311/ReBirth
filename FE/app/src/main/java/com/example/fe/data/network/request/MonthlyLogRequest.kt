package com.example.fe.data.network.request

import com.google.gson.annotations.SerializedName

data class MonthlyLogRequest(
    @SerializedName("year")
    val year: Int,
    
    @SerializedName("month")
    val month: Int
) 