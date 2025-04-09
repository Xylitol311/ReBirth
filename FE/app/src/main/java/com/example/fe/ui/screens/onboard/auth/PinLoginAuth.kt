package com.example.fe.ui.screens.onboard.auth

import android.util.Log
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.ui.screens.onboard.components.NumberPad
import com.example.fe.ui.screens.onboard.components.PinDots
import com.example.fe.ui.screens.onboard.components.device.DeviceInfoManager
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import org.json.JSONObject

fun decodeJwtPayload(token: String): String {
    try {
        val parts = token.split(".")
        if (parts.size >= 2) {
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            return String(decodedBytes)
        }
    } catch (e: Exception) {
        Log.e("JWT", "토큰 디코딩 실패", e)
    }
    return ""
}

@Composable
fun PinLoginAuth(
    deviceInfoManager: DeviceInfoManager,
    onSuccessfulLogin: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    val shuffledNumbers = remember { (0..9).toList().shuffled() }
    val correctPin = viewModel.getUserPin()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.2f))

        Text(
            "비밀번호를 입력해주세요",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(0.05f))

        PinDots(pinInput.length)
        Spacer(modifier = Modifier.weight(0.3f))

        NumberPad(
            numbers = shuffledNumbers,
            input = pinInput,
            onInputChange = { newInput ->
                pinInput = newInput
            },
            onComplete = {
                if (pinInput.length == 6) {
                    viewModel.login(
                        type = "PIN",
                        number = pinInput,
                        phoneSerialNumber = deviceInfoManager.getDeviceId(),
                        onSuccess = {
                            Log.d("PinInputTest", "로그인 성공: $pinInput")
                            Log.d("UserInfo", "로그인 상태: ${viewModel.isLoggedIn}")
                            Log.d("UserInfo", "사용자 이름: ${viewModel.userName}")
                            
                            // JWT 토큰 디코딩 및 로그 출력
                            val token = viewModel.userToken
                            if (token.isNotEmpty()) {
                                val decodedPayload = decodeJwtPayload(token)
                                try {
                                    val jsonPayload = JSONObject(decodedPayload)
                                    Log.d("JWT", "토큰 페이로드: $decodedPayload")
                                    Log.d("JWT", "사용자 ID: ${jsonPayload.optString("sub", "")}")
                                    Log.d("JWT", "만료 시간: ${jsonPayload.optLong("exp", 0)}")
                                    // 기타 필요한 클레임 정보 출력
                                } catch (e: Exception) {
                                    Log.e("JWT", "JSON 파싱 실패", e)
                                }
                            }
                            
                            onSuccessfulLogin()
                        },
                        onFailure = { error ->
                            Log.e("AuthLoginPin","${error}")
                            Toast.makeText(context, "로그인 실패: $error", Toast.LENGTH_SHORT).show()
                            pinInput = ""
                        }
                    )
                }
            }
        )
    }
}