package com.example.fe.ui.screens.onboard.auth


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.ui.screens.onboard.components.NumberPad
import com.example.fe.ui.screens.onboard.components.PinDots
import com.example.fe.ui.screens.onboard.screen.setup.PinStep

@Composable
fun PinAuth(
    currentStep: PinStep,
    onPinConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }


    //새로 화면 뜰때 초기화
    LaunchedEffect(currentStep) {
        pinInput = ""
    }
    val shuffledNumbers = remember { (0..9).toList().shuffled() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.1f))

        Text(
            text = if (currentStep == PinStep.PIN) {
                "비밀번호를 설정해주세요"
            } else {
                "비밀번호를 다시 입력해주세요"
            },
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(0.15f))

        PinDots(pinInput.length)

        Spacer(modifier = Modifier.weight(0.3f))

        NumberPad(
            numbers = shuffledNumbers,
            input = pinInput,
            onInputChange = { pinInput = it },
            onComplete = { onPinConfirmed(pinInput) }
        )

        Spacer(modifier = Modifier.weight(0.05f))
    }
}

