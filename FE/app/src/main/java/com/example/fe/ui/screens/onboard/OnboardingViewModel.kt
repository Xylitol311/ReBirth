package com.example.fe.ui.screens.onboard

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.google.gson.Gson

// Context 확장 속성으로 DataStore 정의
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class OnboardingViewModel(private val context: Context) : ViewModel() {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val HAS_BIOMETRIC_AUTH = booleanPreferencesKey("has_biometric_auth")
        private val HAS_PATTERN_AUTH = booleanPreferencesKey("has_pattern_auth")
        private val USER_PIN = stringPreferencesKey("user_pin")
        private val USER_PATTERN = stringPreferencesKey("user_pattern")
    }

    // 상태를 Observable State로 관리
    var isLoggedIn by mutableStateOf(false)
    var hasPinAuth by mutableStateOf(false)
    var hasBiometricAuth by mutableStateOf(false)
    var hasPatternAuth by mutableStateOf(false)
    private var userPin: String = ""
    private var userPattern: List<Int> = emptyList()
    private val gson = Gson()

    init {
        // 앱 시작 시 저장된 상태 불러오기
        viewModelScope.launch {
            try {
                val preferences = context.dataStore.data.first()
                isLoggedIn = preferences[IS_LOGGED_IN] ?: false
                hasBiometricAuth = preferences[HAS_BIOMETRIC_AUTH] ?: false
                hasPatternAuth = preferences[HAS_PATTERN_AUTH] ?: false
                userPin = preferences[USER_PIN] ?: ""
                userPattern = preferences[USER_PATTERN]?.let {
                    gson.fromJson(it, Array<Int>::class.java).toList()
                } ?: emptyList()
                
                // 디버그용 로그
                Log.d("OnboardingViewModel", "Loaded states: isLoggedIn=$isLoggedIn, hasBiometricAuth=$hasBiometricAuth, hasPatternAuth=$hasPatternAuth")
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Error loading preferences", e)
            }
        }
    }

    // PIN 저장
    fun setUserPin(pin: String) {
        userPin = pin
        hasPinAuth = true
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[USER_PIN] = pin
            }
        }
    }

    // PIN 가져오기
    fun getUserPin(): String = userPin

    // 패턴 저장
    fun setUserPattern(pattern: List<Int>) {
        userPattern = pattern
        hasPatternAuth = true
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[USER_PATTERN] = gson.toJson(pattern)
                preferences[HAS_PATTERN_AUTH] = true
            }
            Log.d("OnboardingViewModel", "Pattern saved, hasPatternAuth=$hasPatternAuth")
        }
    }

    // 패턴 가져오기
    fun getUserPattern(): List<Int> = userPattern

    // 로그인 상태 변경 및 저장
    fun setLoggedInState(value: Boolean) {
        isLoggedIn = value
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = value
            }
        }
    }

    // 생체 인증 상태 변경 및 저장
    fun setBiometricAuthState(value: Boolean) {
        hasBiometricAuth = value
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[HAS_BIOMETRIC_AUTH] = value
            }
            Log.d("OnboardingViewModel", "Biometric auth state changed to $value")
        }
    }

    // 패턴 인증 상태 변경 및 저장
    fun setPatternAuthState(value: Boolean) {
        hasPatternAuth = value
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[HAS_PATTERN_AUTH] = value
            }
            Log.d("OnboardingViewModel", "Pattern auth state changed to $value")
        }
    }

    // 로그아웃 - 모든 인증 상태 초기화 및 저장
    fun logout() {
        isLoggedIn = false
        hasBiometricAuth = false
        hasPatternAuth = false
        hasPinAuth = false
        userPin = ""
        userPattern = emptyList()

        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = false
                preferences[HAS_BIOMETRIC_AUTH] = false
                preferences[HAS_PATTERN_AUTH] = false
                preferences[USER_PIN] = ""
                preferences[USER_PATTERN] = "[]"
            }
        }
    }
}

/**
 * ViewModel 생성을 위한 Factory 클래스
 */
class OnboardingViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}