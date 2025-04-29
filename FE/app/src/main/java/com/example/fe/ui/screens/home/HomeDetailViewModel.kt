package com.example.fe.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fe.data.model.CardSummary
import com.example.fe.data.model.CategorySummary
import com.example.fe.data.network.NetworkClient
import com.example.fe.data.network.api.SummaryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeDetailViewModel : ViewModel() {
    private val TAG = "HomeDetailViewModel"
    private val summaryService = NetworkClient.summaryService

    private val _cardList = MutableStateFlow<List<CardSummary>>(emptyList())
    val cardList: StateFlow<List<CardSummary>> = _cardList.asStateFlow()

    private val _categoryList = MutableStateFlow<List<CategorySummary>>(emptyList())
    val categoryList: StateFlow<List<CategorySummary>> = _categoryList.asStateFlow()

    fun fetchSummaryData() {
        viewModelScope.launch {
            try {
                // 카드별 요약 조회
                val cardResponse = summaryService.getSummaryCard()
                if (cardResponse.success) {
                    _cardList.value = cardResponse.data
                }
                Log.d(TAG, "Card Summary Response: $cardResponse")

                // 카테고리별 요약 조회
                val categoryResponse = summaryService.getSummaryCategory()
                if (categoryResponse.success) {
                    _categoryList.value = categoryResponse.data
                }
                Log.d(TAG, "Category Summary Response: $categoryResponse")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching summary data: ${e.message}")
            }
        }
    }
} 