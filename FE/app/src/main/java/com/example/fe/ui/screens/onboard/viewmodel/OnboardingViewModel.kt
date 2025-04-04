

package com.example.fe.ui.screens.onboard.viewmodel

import android.content.Context
import android.util.Log
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
import com.example.fe.config.NetworkClient
import com.example.fe.data.model.auth.SignupRequest
import com.example.fe.data.model.auth.registPatternRequest
import com.example.fe.data.network.api.AuthApiService

import com.example.fe.ui.screens.onboard.components.device.DeviceInfoManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.String


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class OnboardingViewModel(
    private val deviceInfoManager: DeviceInfoManager,
    private val context: Context
) : ViewModel() {
    private val authApiService = NetworkClient.authApiService
    private val gson = Gson()

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val HAS_BIOMETRIC_AUTH = booleanPreferencesKey("has_biometric_auth")
        private val HAS_PATTERN_AUTH = booleanPreferencesKey("has_pattern_auth")
        private val HAS_PIN_AUTH = booleanPreferencesKey("has_pin_auth")
        private val USER_TOKEN = stringPreferencesKey("user_token")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_PIN = stringPreferencesKey("user_pin")
        private val USER_PATTERN = stringPreferencesKey("user_pattern")
    }

    // 상태 변수
    var isLoggedIn by mutableStateOf(false)
        internal set
    var hasBiometricAuth by mutableStateOf(false)
    var hasPatternAuth by mutableStateOf(false)
    var hasPinAuth by mutableStateOf(false)
    var userName by mutableStateOf("")
        private set
    var userToken by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf("")
        private set

    // 로컬 인증 정보 (메모리 캐싱)
    private var userPin: String = "000000"
    private var userPattern: List<Int> = listOf(0, 3, 6, 7, 8)

    init {
        loadUserPreferences()
    }

    private fun loadUserPreferences() {
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                isLoggedIn = preferences[IS_LOGGED_IN] ?: false
                hasBiometricAuth = preferences[HAS_BIOMETRIC_AUTH] ?: false
                hasPatternAuth = preferences[HAS_PATTERN_AUTH] ?: false
                hasPinAuth = preferences[HAS_PIN_AUTH] ?: false
                userToken = preferences[USER_TOKEN] ?: ""
                userName = preferences[USER_NAME] ?: ""

                userPin = preferences[USER_PIN] ?: ""
                userPattern = preferences[USER_PATTERN]?.let {
                    gson.fromJson(it, Array<Int>::class.java).toList()
                } ?: emptyList()
            }
        }
    }

    fun setIsLogged(): Boolean = true

    // 로컬 PIN 저장
    fun setUserPin(pin: Boolean) {
        hasPinAuth = true
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[HAS_PIN_AUTH] = true
            }
        }
    }

    fun getUserPin(): String = "000000"

    // 로컬 패턴 저장
    fun setUserPattern(pattern: List<Int>) {
        userPattern = pattern
        hasPatternAuth = true
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[USER_PATTERN] = gson.toJson(pattern)
                preferences[HAS_PATTERN_AUTH] = true
            }
        }
    }

    fun getUserPattern(): List<Int> = userPattern

    // 서버 회원가입
    fun registerUser(
        name: String,
        phone: String,
        ssnFront: String,
        pin: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = ""

                val signupResponse = authApiService.signup(
                    SignupRequest(
                        userName = name,
                        phoneNumber = phone,
                        birth = ssnFront,
                        pinNumber = pin,
                        deviceId = deviceInfoManager.getDeviceId()
                    )
                )

                if (!signupResponse.isSuccessful) {
                    throw Exception("회원가입 실패: ${signupResponse.message()}")
                }

                val token = signupResponse.body()?.data.toString()

                context.dataStore.edit { preferences ->
                    preferences[USER_NAME] = name
                    preferences[USER_TOKEN] = token
                }

                userToken = token
                userName = name

                // PIN 로컬 저장도 동시에
                setUserPin(true)

                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "알 수 없는 오류 발생")
            } finally {
                isLoading = false
            }
        }
    }

    // 서버에 패턴 등록 + 로컬 저장
    fun registerPattern(
        token: String,
        pattern: List<Int>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = ""

                val response = authApiService.registPattern(
                    token = token,
                    request = registPatternRequest(
                        deviceId = deviceInfoManager.getDeviceId().toString(),
                        patternNumbers = pattern.joinToString("")
                    )
                )

                if (!response.isSuccessful) {
                    throw Exception("패턴 등록 실패: ${response.message()}")
                }

                setUserPattern(pattern)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "알 수 없는 오류 발생"
                onFailure(errorMessage)
            } finally {
                isLoading = false
            }
        }
    }

    fun setBiometricAuthState(value: Boolean) {
        hasBiometricAuth = value
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[HAS_BIOMETRIC_AUTH] = value
            }
        }
    }

    fun setLoggedInState(value: Boolean) {
        isLoggedIn = value
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = value
            }
        }
    }

    // 로그아웃
    fun logout() {
        isLoggedIn = false
        hasBiometricAuth = false
        hasPatternAuth = false
        hasPinAuth = false
        userName = ""
        userToken = ""
        userPin = ""
        userPattern = emptyList()

        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = false
                preferences[HAS_BIOMETRIC_AUTH] = false
                preferences[HAS_PATTERN_AUTH] = false
                preferences[HAS_PIN_AUTH] = false
                preferences[USER_TOKEN] = ""
                preferences[USER_NAME] = ""
                preferences[USER_PIN] = ""
                preferences[USER_PATTERN] = "[]"
            }
        }
    }
}

/**
 * ViewModel 생성을 위한 Factory 클래스
 */
class OnboardingViewModelFactory(
    private val deviceInfoManager: DeviceInfoManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(deviceInfoManager, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}