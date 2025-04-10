package com.example.fe.ui.components.categoryIcons

import androidx.compose.runtime.Composable
import com.example.fe.R

// 카테고리별 아이콘 매핑 함수 추가
@Composable
fun getCategoryIcon(category: String): Int {
    return when (category) {
        "건강기능식품" -> R.drawable.ic_healthyfood
        "공공요금" -> R.drawable.ic_gonggong
        "공항라운지" -> R.drawable.ic_lounge
        "교육" -> R.drawable.ic_education
        "교통" -> R.drawable.ic_bus
        "금융" -> R.drawable.ic_money
        "기타" -> R.drawable.ic_all
        "대형마트" -> R.drawable.ic_bigmart
        "드럭스토어" -> R.drawable.ic_drugstore
        "디지털컨텐츠" -> R.drawable.ic_digital
        "생활" -> R.drawable.ic_life
        "숙박" -> R.drawable.ic_home
        "스포츠 관련" -> R.drawable.ic_basketball
        "애견" -> R.drawable.ic_pets
        "약국/병원" -> R.drawable.ic_hospital
        "여행/숙박" -> R.drawable.ic_travel
        "영화관" -> R.drawable.ic_movie
        "외식" -> R.drawable.ic_restaurant
        "유아" -> R.drawable.ic_child
        "자동차" -> R.drawable.ic_car
        "전자상거래" -> R.drawable.ic_digitalexchange
        "주유소" -> R.drawable.ic_gasstation
        "택시" -> R.drawable.ic_taxi
        "통신" -> R.drawable.ic_call
        "패션" -> R.drawable.ic_shopping
        "편의점" -> R.drawable.ic_24store
        "항공" -> R.drawable.ic_airport
        else -> R.drawable.ic_all  // 기본 아이콘
    }
}
