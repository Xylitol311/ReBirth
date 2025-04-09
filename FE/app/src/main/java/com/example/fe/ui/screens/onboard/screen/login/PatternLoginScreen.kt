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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(80.dp))
        Text(
            text = "패턴 로그인",
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "4개 이상의 점을 연결하여\n패턴을 입력해주세요",
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )


        Spacer(modifier = Modifier.height(60.dp))

            PatternLockView(
                patternSize = 3,
                onPatternComplete = { pattern ->
                    if (pattern.size >= 4) {
                        val patternString = pattern.joinToString("")
                        viewModel.login(
                            type = "PATTERN",
                            number = patternString,
                            phoneSerialNumber = deviceInfoManager.getDeviceId(),
                            onSuccess = onLoginSuccess,
                            onFailure = { error ->
                                Toast.makeText(context, "로그인 실패: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "패턴은 최소 4개 점을 연결해야 합니다", Toast.LENGTH_SHORT).show()
                    }
                }
            )


    }
}