//package com.example.fe.ui.screens.onboard
//
//import android.content.Context
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.core.booleanPreferencesKey
//import androidx.datastore.preferences.core.edit
//import androidx.datastore.preferences.preferencesDataStore
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch


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

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val HAS_BIOMETRIC_AUTH = booleanPreferencesKey("has_biometric_auth")
        private val HAS_PATTERN_AUTH = booleanPreferencesKey("has_pattern_auth")
        private val HAS_PIN_AUTH = booleanPreferencesKey("has_pin_auth")
        private val USER_TOKEN = stringPreferencesKey("user_token")
        private val USER_NAME = stringPreferencesKey("user_name")
    }

    // 인증 상태
    var isLoggedIn by mutableStateOf(false)
        private set
    var hasBiometricAuth by mutableStateOf(false)
    var hasPatternAuth by mutableStateOf(false)
    var hasPinAuth by mutableStateOf(false)
    var hasFingerprintAuth by mutableStateOf(false) // 추가된 프로퍼티

    // 사용자 정보
    var userName by mutableStateOf("")
        private set
    var userToken by mutableStateOf("")
        private set

    // 회원가입/로그인 상태
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf("")
        private set

    init {
        // 앱 시작 시 저장된 상태 불러오기
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
            }
        }
    }

    // 로그인 상태 변경 및 저장
    fun setLoggedInState(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = value
            }
            isLoggedIn = value
        }
    }

    // 생체 인증 상태 변경 및 저장
    fun setBiometricAuthState(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[HAS_BIOMETRIC_AUTH] = value
            }
            hasBiometricAuth = value
        }
    }

    // 패턴 인증 상태 변경 및 저장
    fun setPatternAuthState(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[HAS_PATTERN_AUTH] = value
            }
            hasPatternAuth = value
        }
    }

    // PIN 인증 상태 변경 및 저장
    fun setPinAuthState(value: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[HAS_PIN_AUTH] = value
            }
            hasPinAuth = value
        }
    }

    // 지문 인증 상태 변경
    fun setFingerprintAuthState(value: Boolean) {
        hasFingerprintAuth = value
    }

    // 사용자 정보 저장
    private fun saveUserInfo(token: String, name: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[USER_TOKEN] = token
                preferences[USER_NAME] = name
            }
            userToken = token
            userName = name
        }
    }

    // 회원가입 함수
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
                Log.d("LoginAUTH","회원가입 요청가는중")
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
                    Log.e("LoginAUTH","왜 실패했을까..")

                    throw Exception("회원가입 실패: ${signupResponse.message()}")
                }

                context.dataStore.edit { preferences ->
                    preferences[IS_LOGGED_IN] = true
                    preferences[USER_NAME] = name
                    preferences[USER_TOKEN] = signupResponse.body()?.data.toString()
                }

                onSuccess()
            } catch (e: Exception) {
                Log.e("LoginAUTH","알수 없는 오류")
                onFailure(e.message ?: "알 수 없는 오류 발생")
            } finally {
                isLoading = false
            }
        }
    }


    // 패턴 등록 함수
    fun registerPattern(
        token: String,
        pattern: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = ""

                val patternResponse = authApiService.registPattern(
                    token = token, // 토큰 추가 (Bearer 접두사 필요 여부 확인)
                    request = registPatternRequest(
                        deviceId = deviceInfoManager.getDeviceId().toString(),
                        patternNumbers = pattern
                    )
                )

                if (!patternResponse.isSuccessful) {
                    throw Exception("패턴 등록 실패: ${patternResponse.message()}")
                }

                setPatternAuthState(true)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "알 수 없는 오류 발생"
                onFailure(errorMessage)
            } finally {
                isLoading = false
            }
        }
    }

    // 로그아웃 - 모든 인증 상태 초기화 및 저장
    fun logout() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[IS_LOGGED_IN] = false
                preferences[HAS_BIOMETRIC_AUTH] = false
                preferences[HAS_PATTERN_AUTH] = false
                preferences[HAS_PIN_AUTH] = false
                preferences[USER_TOKEN] = ""
                preferences[USER_NAME] = ""
            }
            isLoggedIn = false
            hasBiometricAuth = false
            hasPatternAuth = false
            hasPinAuth = false
            hasFingerprintAuth = false
            userName = ""
            userToken = ""
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