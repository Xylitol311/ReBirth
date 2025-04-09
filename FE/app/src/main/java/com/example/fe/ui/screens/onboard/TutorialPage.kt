package com.example.fe.ui.screens.onboard

/**
 * TutorialPage.kt
 *
 * 각 튜토리얼 페이지에 표시할 정보(타이틀, 설명, 이미지 리소스 ID)를 저장하는 데이터 클래스입니다.
 */
data class TutorialPage(
    val title: String,
    val description: String,
    val imageRes: Int   // 예: R.drawable.earth, R.drawable.card_constellation 등
)
