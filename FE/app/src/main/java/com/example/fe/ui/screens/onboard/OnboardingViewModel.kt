package com.example.fe.ui.screens.onboard

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Context 확장 속성으로 DataStore 정의
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class OnboardingViewModel(private val context: Context) : ViewModel() {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val HAS_BIOMETRIC_AUTH = booleanPreferencesKey("has_biometric_auth")
        private val HAS_PATTERN_AUTH = booleanPreferencesKey("has_pattern_auth")
    }

    // 상태를 Observable State로 관리
    var isLoggedIn by mutableStateOf(false)
        private set

    var hasBiometricAuth by mutableStateOf(false)
    var hasPatternAuth by mutableStateOf(false)

    init {
        // 앱 시작 시 저장된 상태 불러오기
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                isLoggedIn = preferences[IS_LOGGED_IN] ?: false
                hasBiometricAuth = preferences[HAS_BIOMETRIC_AUTH] ?: false
                hasPatternAuth = preferences[HAS_PATTERN_AUTH] ?: false
            }
        }
    }

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
        }
    }

    // 패턴 인증 상태 변경 및 저장
    fun setPatternAuthState(value: Boolean) {
        hasPatternAuth = value
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[HAS_PATTERN_AUTH] = value
            }
        }
    }

    // 로그아웃 - 모든 인증 상태 초기화 및 저장
    fun logout() {
        isLoggedIn = false
        hasBiometricAuth = false
        hasPatternAuth = false

        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = false
                preferences[HAS_BIOMETRIC_AUTH] = false
                preferences[HAS_PATTERN_AUTH] = false
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