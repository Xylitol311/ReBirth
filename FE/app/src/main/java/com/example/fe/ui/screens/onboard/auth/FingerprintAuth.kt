package com.example.fe.ui.screens.onboard.auth

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL

/**
 * 지문 인증 기능을 처리하는 객체
 * BiometricPrompt를 사용하여 사용자의 지문을 인증
 */
object FingerprintAuth {
    /**
     * 지문 인증을 실행하는 함수
     * 
     * @param activity 지문 인증 다이얼로그를 표시할 FragmentActivity
     * @param onResult 인증 결과를 처리할 콜백 함수 (성공: true, 실패: false)
     */
    fun authenticate(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
        Log.d("FingerprintAuth", "Activity: ${activity.javaClass.name}")

        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // 메인 스레드에서 콜백을 실행하기 위한 Executor
                val executor = ContextCompat.getMainExecutor(activity)

                // 지문 인증 결과를 처리하는 콜백
                val callback = object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Log.d("Fingerprint", "Authentication succeeded!")
                        onResult(true)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.e("Fingerprint", "Authentication error: $errString")
                        onResult(false)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.e("Fingerprint", "Authentication failed")
                        onResult(false)
                    }
                }

                // BiometricPrompt 인스턴스 생성
                val biometricPrompt = BiometricPrompt(activity, executor, callback)

                // 인증 다이얼로그 설정
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("지문 인증")
                    .setSubtitle("등록된 지문을 사용하여 인증해주세요")
                    .setNegativeButtonText("취소")
                    .build()

                // 지문 인증 다이얼로그 표시
                biometricPrompt.authenticate(promptInfo)
            }
            else -> {
                Log.e("FingerprintAuth", "Biometric authentication not available")
                onResult(false)
            }
        }
    }
}