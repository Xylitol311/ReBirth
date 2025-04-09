package com.example.fe.ui.screens.onboard.screen.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.example.fe.ui.screens.onboard.auth.FingerprintAuthComposable
import com.example.fe.ui.screens.onboard.components.device.DeviceInfoManager
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingerprintLoginScreen(
    viewModel: OnboardingViewModel,
    deviceInfoManager: DeviceInfoManager,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    Scaffold {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.1f))

                Text(
                    "생체 인증을 진행해 주세요",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 지문 인증 컴포넌트
                FingerprintAuthComposable { success ->
                    if (success) {
                        viewModel.login(
                            type = "fingerprint",
                            number =  null,
                            phoneSerialNumber = deviceInfoManager.getDeviceId(),
                            onSuccess = {
                                onLoginSuccess()
                            },
                            onFailure = { error ->
                                Log.e("bioLoginPin","${error}")
                                Toast.makeText(context, "로그인 실패: $error", Toast.LENGTH_SHORT).show()

                            }
                        )
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "지문 인증에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
} 