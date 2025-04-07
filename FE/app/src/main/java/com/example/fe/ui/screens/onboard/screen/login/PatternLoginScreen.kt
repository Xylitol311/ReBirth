package com.example.fe.ui.screens.onboard.screen.setup

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.screens.onboard.auth.PatternAuth
import com.example.fe.ui.screens.onboard.auth.PatternLockView
import com.example.fe.ui.screens.onboard.components.device.DeviceInfoManager
import com.example.fe.ui.screens.onboard.screen.setup.security.AdditionalSecurityStep
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternLoginScreen(
    navController: NavController,
    viewModel: OnboardingViewModel,
    deviceInfoManager: DeviceInfoManager,
    onLoginSuccess: () -> Unit
) {
    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            PatternLoginContent(
                viewModel = viewModel,
                deviceInfoManager = deviceInfoManager,
                onLoginSuccess = onLoginSuccess
            )
        }
    }
}

@Composable
fun PatternLoginContent(
    viewModel: OnboardingViewModel,
    deviceInfoManager: DeviceInfoManager,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "패턴을 입력해주세요",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        PatternLockView(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            patternSize = 3,
            onPatternComplete = { pattern ->

                val patternString = pattern.joinToString("") // 숫자 리스트 → String 변환
                Log.d("AuthLoginPattern","${patternString}")
                viewModel.login(
                    type = "PATTERN",
                    number = patternString,
                    phoneSerialNumber = deviceInfoManager.getDeviceId(),
                    onSuccess = {
                        Log.d("AuthLoginPattern", "로그인 성공: $patternString")
                        onLoginSuccess()
                    },
                    onFailure = { error ->
                        Log.d("AuthLoginPattern", "${patternString}/${deviceInfoManager.getDeviceId()}")
                        Log.e("AuthLoginPattern", "${error}")
                        Toast.makeText(context, "로그인 실패: $error", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}