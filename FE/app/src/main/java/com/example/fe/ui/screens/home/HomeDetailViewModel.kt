package com.example.fe.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.fe.config.AppConfig
import com.example.fe.data.network.CardSummary
import com.example.fe.data.network.CategorySummary
import com.example.fe.data.network.SummaryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeDetailViewModel : ViewModel() {
    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.Server.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val summaryService = retrofit.create(SummaryService::class.java)

    private val _cardList = MutableStateFlow<List<CardSummary>>(emptyList())
    val cardList: StateFlow<List<CardSummary>> = _cardList.asStateFlow()

    private val _categoryList = MutableStateFlow<List<CategorySummary>>(emptyList())
    val categoryList: StateFlow<List<CategorySummary>> = _categoryList.asStateFlow()

    suspend fun fetchSummaryData() {
        try {
            // 카드별 요약 조회
            val cardResponse = summaryService.getSummaryCard(1) // 임시로 userId 1 사용
            if (cardResponse.success) {
                _cardList.value = cardResponse.data
            }
            Log.d("HomeDetailViewModel", "Card Summary Response: $cardResponse")

            // 카테고리별 요약 조회
            val categoryResponse = summaryService.getSummaryCategory(1) // 임시로 userId 1 사용
            if (categoryResponse.success) {
                _categoryList.value = categoryResponse.data
            }
            Log.d("HomeDetailViewModel", "Category Summary Response: $categoryResponse")
        } catch (e: Exception) {
            Log.e("HomeDetailViewModel", "Error fetching summary data: ${e.message}")
        }
    }
} 