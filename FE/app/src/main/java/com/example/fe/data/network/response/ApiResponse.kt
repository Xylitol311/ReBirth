package com.example.fe.data.network.response

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T
) 