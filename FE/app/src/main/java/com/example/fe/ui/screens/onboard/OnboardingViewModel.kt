package com.example.fe.ui.screens.onboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OnboardingViewModel : ViewModel() {
    var isLoggedIn by mutableStateOf(false)
        private set

    var hasBiometricAuth by mutableStateOf(false)
    var hasPatternAuth by mutableStateOf(false)

    fun setLoggedInState(value: Boolean) {
        isLoggedIn = value
    }

    fun logout() {
        isLoggedIn = false
        hasBiometricAuth = false
        hasPatternAuth = false
    }
}