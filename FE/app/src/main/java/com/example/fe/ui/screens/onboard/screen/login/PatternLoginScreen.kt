package com.example.fe.ui.screens.onboard.screen.login

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.fe.R
import com.example.fe.ui.screens.onboard.OnboardingViewModel
import com.example.fe.ui.screens.onboard.auth.LoginPatternAuth
import com.example.fe.ui.screens.onboard.screen.setup.security.AdditionalSecurityStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternLoginScreen(
    navController: NavController,
    viewModel: OnboardingViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(AdditionalSecurityStep.PATTERN) }

    Scaffold(
        topBar = {
            if (currentStep == AdditionalSecurityStep.PATTERN_CONFIRM) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { currentStep = AdditionalSecurityStep.PATTERN },
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "뒤로가기",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LoginPatternAuth(
                currentStep = currentStep,
                onPatternConfirmed = { pattern ->
                    // 패턴이 일치하면 홈 화면으로 이동
                    if (pattern == viewModel.getUserPattern()) {
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "패턴이 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                    }
                },
                onStepChange = { step ->
                    currentStep = step
                }
            )
        }
    }
}
