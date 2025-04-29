package com.example.fe.data.model

data class UserInfoResponse(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: String,
    val gender: String,
    val profileImage: String?,
    val joinDate: String
) 