package com.example.fe.ui.screens.home

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fe.data.model.PreBenefitFeedbackData
import com.example.fe.data.model.SpendingItem
import com.example.fe.data.model.cardRecommend.CardInfoApi
import com.example.fe.data.repository.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.fe.ui.screens.onboard.viewmodel.dataStore
import kotlinx.coroutines.flow.first

class HomeViewModel(private val context: Context) : ViewModel() {
    private val TAG = "HomeViewModel"
    private val repository = HomeRepository()

    // UI 상태
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 사용자 이름 상태
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // 소비 금액 상태
    private val _totalSpendingAmount = MutableStateFlow(0)
    val totalSpendingAmount: StateFlow<Int> = _totalSpendingAmount.asStateFlow()

    private val _totalBenefitAmount = MutableStateFlow(0)
    val totalBenefitAmount: StateFlow<Int> = _totalBenefitAmount.asStateFlow()

    // 소비 리스트 상태
    private val _goodList = MutableStateFlow<List<SpendingItem>>(emptyList())
    val goodList: StateFlow<List<SpendingItem>> = _goodList.asStateFlow()

    private val _badList = MutableStateFlow<List<SpendingItem>>(emptyList())
    val badList: StateFlow<List<SpendingItem>> = _badList.asStateFlow()

    // 직전 거래 피드백 상태
    private val _preBenefitFeedback = MutableStateFlow<PreBenefitFeedbackData?>(null)
    val preBenefitFeedback: StateFlow<PreBenefitFeedbackData?> = _preBenefitFeedback.asStateFlow()

    // 추천 카드 상태
    private val _recommendedCards = MutableStateFlow<List<CardInfoApi>>(emptyList())
    val recommendedCards: StateFlow<List<CardInfoApi>> = _recommendedCards.asStateFlow()

    // 검색 결과 상태
    private val _searchResults = MutableStateFlow<List<CardInfoApi>>(emptyList())
    val searchResults: StateFlow<List<CardInfoApi>> = _searchResults.asStateFlow()

    init {
        loadData()
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            try {
                val preferences = context.dataStore.data.first()
                val userName = preferences[stringPreferencesKey("user_name")] ?: "사용자"
                _userName.value = userName
                Log.d(TAG, "사용자 이름 로드 성공: $userName")
            } catch (e: Exception) {
                Log.e(TAG, "사용자 이름 로드 실패", e)
                _userName.value = "사용자"
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = HomeUiState.Loading

                // 병렬로 API 호출
                val summaryResult = repository.getSummary()
                val feedbackResult = repository.getPreBenefitFeedback()

                // 결과 처리
                when {
                    summaryResult.isSuccess && feedbackResult.isSuccess -> {
                        // Summary 데이터 처리
                        summaryResult.getOrNull()?.data?.let { data ->
                            _totalSpendingAmount.value = data.totalSpendingAmount
                            _totalBenefitAmount.value = data.totalBenefitAmount
                            _goodList.value = data.goodList
                            _badList.value = data.badList
                        }

                        // Feedback 데이터 처리
                        feedbackResult.getOrNull()?.data?.let { data ->
                            _preBenefitFeedback.value = data
                        }

                        _uiState.value = HomeUiState.Success
                    }
                    else -> {
                        val error = summaryResult.exceptionOrNull() ?: feedbackResult.exceptionOrNull()
                        _uiState.value = HomeUiState.Error(error?.message ?: "알 수 없는 오류가 발생했습니다.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "데이터 로드 중 오류 발생", e)
                _uiState.value = HomeUiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }

    fun refresh() {
        loadData()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Success : HomeUiState()
    data class Error(val message: String) : HomeUiState()
} 