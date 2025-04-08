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
import com.example.fe.data.model.auth.ReportWithPatternDTO
import com.example.fe.data.network.NetworkClient
import com.example.fe.data.model.auth.SignupRequest
import com.example.fe.data.model.auth.userLoginRequest
import com.example.fe.data.network.Interceptor.TokenProvider
import com.example.fe.ui.screens.onboard.components.device.DeviceInfoManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
        internal val USER_TOKEN = stringPreferencesKey("user_token")
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

    fun validateUserPin(){

    }

    // 로컬 패턴 저장
    fun setUserPattern() {
//        userPattern = pattern
        hasPatternAuth = true
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
//                preferences[USER_PATTERN] = gson.toJson(pattern)
                preferences[HAS_PATTERN_AUTH] = true
            }
        }
    }

    fun getUserPattern(): List<Int> = userPattern

    fun getUserPatternType(
        userId: String,
        year: Int,
        month: Int,
        onSuccess: (ReportWithPatternDTO) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = ""

                val response = authApiService.getReportWithPattern(
                    userId = userId,
                    year = year,
                    month = month
                )

                if (!response.isSuccessful) {
                    throw Exception("패턴 정보를 불러오지 못했습니다: ${response.message()}")
                }

                val body = response.body() ?: throw Exception("응답 데이터가 없습니다.")
                val reportData = body.data ?: throw Exception("패턴 데이터가 없습니다.")
                Log.d("PatternInfo", "바디전체가져오기: ${body}")
                Log.d("PatternInfo", "전체가져오기: ${reportData}")


                Log.d("PatternInfo", "패턴 이름: ${reportData.consumptionPattern?.patternName}")
                Log.d("PatternInfo", "설명: ${reportData.consumptionPattern?.description}")
                Log.d("PatternInfo", "이미지 URL: ${reportData.consumptionPattern?.imgUrl}")

                onSuccess(reportData)

            } catch (e: Exception) {
                Log.e("PatternError", e.message ?: "알 수 없는 오류")
                onFailure(e.message ?: "알 수 없는 오류 발생")
            } finally {
                isLoading = false
            }
        }
    }

    // 서버 회원가입
    fun registerUser(
        name: String,
        phone: String,
        ssnFront: String,
        pin: String,
        income: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = ""

                Log.d("AuthSignup","수입 > ${income} / 핀 넘버 >${pin}")


                val signupResponse = authApiService.signup(
                    SignupRequest(
                        userName = name,
                        phoneNumber = phone,
                        birth = ssnFront,
                        pinNumber = pin,
                        averageMonthlyIncome = income,
                        deviceId = deviceInfoManager.getDeviceId()
                    )
                )

                if (!signupResponse.isSuccessful) {
                    Log.d("AuthToken","${signupResponse.headers()}")
                    throw Exception("회원가입 실패: ${signupResponse.message()}")
                }

                // 헤더에서 토큰 꺼내기
                val token = signupResponse.headers()["Authorization"]?: throw Exception("토큰이 없습니다.")

                Log.d("AuthToken","${token}")
                Log.d("AuthToken","${signupResponse.headers()}")

                // 토큰 저장 및 Interceptor 초기화
                userToken = token
                userName = name


                context.dataStore.edit { preferences ->
                    preferences[USER_NAME] = name
                    preferences[USER_TOKEN] = token
                }
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

    fun login(
        type: String,
        number: String? = null,
        phoneSerialNumber: String? = null,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = ""
                Log.d("pinAuthLogin","${type}/${number}/${phoneSerialNumber}")

                val response =authApiService.login(
                    userLoginRequest(
                    type = type,
                    number = number,
                    phoneSerialNumber = phoneSerialNumber
                )
                )
                if (!response.isSuccessful) {
                    Log.d("Login", "로그인 실패: ${response}")
                    throw Exception("로그인 실패: ${response.message()}")
                }

                // JWT 토큰 저장 등 필요한 후처리 작업이 있다면 여기에
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message ?: "알 수 없는 오류 발생"
                onFailure(errorMessage)
            } finally {
                isLoading = false
            }
        }
    }

    // 서버에 패턴 등록 + 로컬 저장
    fun registerPattern(
        pattern: List<Int>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = ""

                Log.d("AuthPattern","${pattern.joinToString("")}")
                val response = authApiService.registPattern(
                    patternNumbers = pattern.joinToString("")

                )

                if (!response.isSuccessful) {
                    Log.d("AuthPattern","${response.message()}")
                    throw Exception("패턴 등록 실패: ${response.message()}")
                }

                setUserPattern()
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

    // 마이데이터 로드 함수
    fun loadAllMyData(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = ""

                val result = authApiService.loadAllMyData()
                if (result.isSuccessful) {
                    onSuccess()
                } else {
                    Log.d("AuthloadmyData","데이터 로드시 ${result}")

                    throw Exception("마이데이터 로드 실패")
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "마이데이터 로드 중 오류 발생"
                onFailure(errorMessage)
            } finally {
                isLoading = false
            }
        }
    }

    // 리포트 생성 함수
    fun generateReportFromMyData(
        userId: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = ""

                val result = authApiService.generateReportFromMyData(userId)
                if (result.isSuccessful) {
                    onSuccess()
                } else {
                    throw Exception("리포트 생성 실패")
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "리포트 생성 중 오류 발생"
                onFailure(errorMessage)
            } finally {
                isLoading = false
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

class AppTokenProvider(private val context: Context) : TokenProvider {
    override fun getToken(): String {
        // DataStore는 suspend라서 동기적으로 값을 못 받아옴.
        // 그래서 runBlocking으로 일시적으로 블로킹해서 값을 가져와야 함.
        return runBlocking {
            val preferences = context.dataStore.data.first()
            preferences[OnboardingViewModel.USER_TOKEN] ?: ""
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