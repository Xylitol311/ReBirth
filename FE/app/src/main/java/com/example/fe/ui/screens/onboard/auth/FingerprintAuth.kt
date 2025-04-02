package com.example.fe.ui.screens.onboard.auth


import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL

object FingerprintAuth {
    fun authenticate(activity: FragmentActivity, onResult: (Boolean) -> Unit) {
        Log.d("FingerprintAuth", "Activity: ${activity.javaClass.name}")

        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(activity)
                val biometricPrompt = BiometricPrompt(activity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onResult(true)
                        }
                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            onResult(false)
                        }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            onResult(false)
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("지문 인증")
                    .setSubtitle("지문을 사용하여 인증하세요")
                    .setNegativeButtonText("취소")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            else -> {
                Log.e("FingerprintAuth", "Biometric authentication not available")
                onResult(false)
            }
        }
    }
}