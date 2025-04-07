package com.example.fe.data.model.auth


//담아오는 응답
data class ApiResponseDTO<T>(
    val success: Boolean,
    val message: String,
    val data: T
)

//1차 회원가입시 요청
data class SignupRequest(
    val userName: String,
    val birth: String,
    val pinNumber: String,
    val phoneNumber: String,
    val deviceId: String
)

//2차 회원가입은 헤더 userId만

//3차 패턴 로그임 회원가입
data class registPatternRequest(
    val deviceId: String,
    val patternNumbers: String
)

//로그인 시
data class userLoginRequest(
    var type: String,
    var number: String? = null,
    var phoneSerialNumber: String? = null

)