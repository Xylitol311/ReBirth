package com.example.fe.ui.screens.onboard.auth

import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import com.example.fe.ui.screens.onboard.components.NumberPad
import com.example.fe.ui.screens.onboard.components.PinDots

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fe.ui.screens.onboard.screen.setup.PinStep
@Composable
fun PinAuth(
    currentStep: PinStep,
    onPinConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    val pinShuffledNumbers = remember { (1..9).toList().shuffled() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.1f))

        Text(
            if (currentStep == PinStep.PIN) "비밀번호를 설정해주세요"
            else "비밀번호를 다시 입력해주세요",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(0.15f))

        PinDots(pinInput.length)

        Spacer(modifier = Modifier.weight(0.3f))

        NumberPad(
            numbers = pinShuffledNumbers,
            input = pinInput,
            onInputChange = { newInput ->
                pinInput = newInput
            },
            onComplete = {
                if (pinInput.length == 6) {
                    onPinConfirmed(pinInput)
                }
            },

        )

        Spacer(modifier = Modifier.weight(0.05f))
    }
}
//@Composable
//fun PinAuth(
//    currentStep: PinStep,
//    onPinConfirmed: () -> Unit,
//    onStepChange: (PinStep) -> Unit
//) {
//    val context = LocalContext.current
//    var pinInput by remember { mutableStateOf("") }
//    var confirmInput by remember { mutableStateOf("") }
//    val pinShuffledNumbers = remember { (1..9).toList().shuffled() }
//    val confirmShuffledNumbers = remember { (1..9).toList().shuffled() }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Spacer(modifier = Modifier.weight(0.1f))
//
//        Text(
//            if (currentStep == PinStep.PIN) "비밀번호를 설정해주세요"
//            else "비밀번호를 다시 입력해주세요",
//            fontSize = 28.sp,
//            fontWeight = FontWeight.Medium
//        )
//
//        Spacer(modifier = Modifier.weight(0.15f))
//
//        PinDots(if (currentStep == PinStep.PIN) pinInput.length else confirmInput.length)
//
//        Spacer(modifier = Modifier.weight(0.3f))
//
//        NumberPad(
//            numbers = if (currentStep == PinStep.PIN) pinShuffledNumbers else confirmShuffledNumbers,
//            input = if (currentStep == PinStep.PIN) pinInput else confirmInput,
//            onInputChange = { newInput ->
//                if (currentStep == PinStep.PIN) pinInput = newInput else confirmInput = newInput
//            },
//            onComplete = {
//                if (currentStep == PinStep.PIN) {
//                    if (pinInput.length == 6) {
//                        onStepChange(PinStep.PIN_CONFIRM)
//                    }
//                } else {
//                    if (confirmInput.length == 6) {
//                        if (pinInput == confirmInput) {
//                            onPinConfirmed()
//                        } else {
//                            Toast.makeText(
//                                context,
//                                "비밀번호가 일치하지 않습니다",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            confirmInput = ""
//                        }
//                    }
//                }
//            }
//        )
//
//        Spacer(modifier = Modifier.weight(0.05f))
//    }
//}