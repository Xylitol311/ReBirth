package com.example.fe.ui.screens.onboard.auth

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
import com.example.fe.ui.screens.onboard.OnboardingViewModel

@Composable
fun PinLoginAuth(
    onSuccessfulLogin: () -> Unit,
    viewModel: OnboardingViewModel
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    val shuffledNumbers = remember { (1..9).toList().shuffled() }
    val correctPin = viewModel.getUserPin()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.1f))

        Text(
            "비밀번호를 입력해주세요",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(0.15f))

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
                    // 입력한 PIN이 정확한지 확인
                    if (pinInput == correctPin) {
                        onSuccessfulLogin()
                    } else {
                        Toast.makeText(
                            context,
                            "비밀번호가 일치하지 않습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                        pinInput = ""
                    }
                }
            }
        )

        Spacer(modifier = Modifier.weight(0.05f))
    }
}