package com.example.fe.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.fe.config.AppConfig
import com.example.fe.data.model.SpendingItem
import com.example.fe.data.network.SummaryService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeViewModel : ViewModel() {
    private val retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.Server.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val summaryService = retrofit.create(SummaryService::class.java)

    // 상태 변수들
    private val _totalSpendingAmount = MutableStateFlow(0)
    val totalSpendingAmount: StateFlow<Int> = _totalSpendingAmount.asStateFlow()

    private val _totalBenefitAmount = MutableStateFlow(0)
    val totalBenefitAmount: StateFlow<Int> = _totalBenefitAmount.asStateFlow()

    private val _goodList = MutableStateFlow<List<SpendingItem>>(emptyList())
    val goodList: StateFlow<List<SpendingItem>> = _goodList.asStateFlow()

    private val _badList = MutableStateFlow<List<SpendingItem>>(emptyList())
    val badList: StateFlow<List<SpendingItem>> = _badList.asStateFlow()

    init {
        fetchSummary()
    }

    private fun fetchSummary() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = summaryService.getSummary(1) // 임시로 userId 1 사용
                Log.d("HomeViewModel", "Summary Response: $response")
                
                if (response.success) {
                    _totalSpendingAmount.value = response.data.totalSpendingAmount
                    _totalBenefitAmount.value = response.data.totalBenefitAmount
                    _goodList.value = response.data.goodList
                    _badList.value = response.data.badList
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching summary: ${e.message}")
            }
        }
    }
} 