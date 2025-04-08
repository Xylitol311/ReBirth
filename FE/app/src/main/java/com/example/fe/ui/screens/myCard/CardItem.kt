package com.example.fe.ui.screens.myCard

import androidx.compose.runtime.mutableStateListOf

/**
 * 카드 아이템을 나타내는 데이터 클래스
 * 모든 카드 관련 화면에서 공통으로 사용됩니다.
 */
data class CardItem(
    val id: Int,
    val name: String,
    val cardNumber: String
)

/**
 * 카드 항목과 표시 여부를 함께 관리하는 데이터 클래스
 * 카드 관리 화면에서 사용됩니다.
 */
data class CardItemWithVisibility(
    val card: CardItem,
    val isVisible: Boolean = true
)

/**
 * 카드 순서 관리를 위한 싱글톤 객체
 * 앱 전체에서 카드 순서를 일관되게 유지하기 위해 사용
 */
object CardOrderManager {
    // 정렬된 카드 목록
    private val _cards = mutableStateListOf<CardItemWithVisibility>()
    
    // 읽기 전용 카드 목록
    val sortedCards: List<CardItemWithVisibility>
        get() = _cards.toList()
    
    // 초기 데이터 설정 (비어있을 경우에만)
    fun initializeIfEmpty(initialCards: List<CardItemWithVisibility>) {
        if (_cards.isEmpty()) {
            _cards.clear()
            _cards.addAll(initialCards)
        }
    }
    
    // 카드 목록 업데이트
    fun updateCards(newCards: List<CardItemWithVisibility>) {
        _cards.clear()
        _cards.addAll(newCards)
        notifyListeners()
    }
    
    // 특정 ID의 카드 가져오기
    fun getCardById(id: Int): CardItemWithVisibility? {
        return _cards.find { it.card.id == id }
    }
    
    // 변경 리스너
    private val listeners = mutableListOf<() -> Unit>()
    
    // 리스너 등록
    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }
    
    // 리스너 제거
    fun removeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }
    
    // 리스너에게 변경 알림
    private fun notifyListeners() {
        listeners.forEach { it.invoke() }
    }
} 