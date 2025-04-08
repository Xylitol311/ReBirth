package com.example.fe.data.model.auth
import com.google.gson.annotations.SerializedName

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
    val deviceId: String,
    val averageMonthlyIncome: String
)

//2차 회원가입은 헤더 userId만

//3차 패턴 로그임 회원가입
data class registPatternRequest(
    val deviceId: String,
    val patternNumbers: String
)

//패턴 정보 받아오기
data class ReportWithPatternDTO(
    @SerializedName("total_spending_amount") val totalSpendingAmount: Int?,
    @SerializedName("pre_total_spending_amount") val preTotalSpendingAmount: Int?,
    @SerializedName("total_benefit_amount") val totalBenefitAmount: Int?,
    @SerializedName("total_group_benefit_average") val totalGroupBenefitAverage: Int?,
    @SerializedName("group_name") val groupName: String?,
    @SerializedName("over_consumption") val overConsumption: Int?,
    val variation: Int?,
    val extrovert: Int?,
    @SerializedName("report_description") val reportDescription: String?,
    @SerializedName("consumption_patterns") val consumptionPattern: ConsumptionPattern?
)

data class ConsumptionPattern(
    @SerializedName("pattern_id") val patternId: String?,
    @SerializedName("pattern_name") val patternName: String?,
    val description: String?,
    @SerializedName("img_url") val imgUrl: String?
)

//로그인 시
data class userLoginRequest(
    var type: String,
    var number: String? = null,
    var phoneSerialNumber: String? = null

)