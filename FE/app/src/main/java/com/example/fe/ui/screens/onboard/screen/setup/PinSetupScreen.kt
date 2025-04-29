package com.example.fe.ui.screens.onboard.screen.setup
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.fe.data.network.api.AuthApiService
import com.example.fe.ui.screens.onboard.auth.PinAuth
import com.example.fe.ui.screens.onboard.components.device.AndroidDeviceInfoManager
import com.example.fe.ui.screens.onboard.components.device.DeviceInfoManager
import com.example.fe.ui.screens.onboard.components.login.saveLoginMethod
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModelFactory


enum class PinStep { PIN, PIN_CONFIRM, DONE }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    navController: NavController,
    name: String,
    phone: String,
    ssnFront: String,
    income: String
) {
    val context = LocalContext.current
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModelFactory(
            deviceInfoManager = AndroidDeviceInfoManager(context),
            context = context
        )
    )
    var currentStep by remember { mutableStateOf(PinStep.PIN) }
    var pin by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // 시스템 뒤로가기 처리
    BackHandler {
        if (currentStep == PinStep.PIN_CONFIRM) {
            currentStep = PinStep.PIN
        } else {
            // 첫 단계(PIN)에서는 수입 입력 화면으로 돌아감
            navController.navigate(
                "income_input/${name}/${phone}/${ssnFront}",
                NavOptions.Builder()
                    .setEnterAnim(0)
                    .setExitAnim(0)
                    .setPopEnterAnim(0)
                    .setPopExitAnim(0)
                    .build()
            )
        }
    }
    
    // 에러 메시지 표시
    if (errorMessage.isNotEmpty()) {
        LaunchedEffect(errorMessage) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            errorMessage = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { 
                            if (currentStep == PinStep.PIN_CONFIRM) {
                                currentStep = PinStep.PIN
                            } else {
                                // 첫 단계(PIN)에서는 수입 입력 화면으로 돌아감
                                navController.navigate(
                                    "income_input/${name}/${phone}/${ssnFront}",
                                    NavOptions.Builder()
                                        .setEnterAnim(0)
                                        .setExitAnim(0)
                                        .setPopEnterAnim(0)
                                        .setPopExitAnim(0)
                                        .build()
                                )
                            }
                        },
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentStep) {
                PinStep.PIN, PinStep.PIN_CONFIRM -> {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        PinAuth(
                            currentStep = if (currentStep == PinStep.PIN) PinStep.PIN else PinStep.PIN_CONFIRM,
                            onPinConfirmed = { confirmedPin ->
                                viewModel.setUserPin(true)
                                if (currentStep == PinStep.PIN) {
                                    pin = confirmedPin
                                    currentStep = PinStep.PIN_CONFIRM
                                } else {
                                    // PIN 확인이 일치하는지 검증
                                    if (confirmedPin == pin) {
                                        Log.d("LoginAUTH","${name}/${phone}/${ssnFront}/${confirmedPin}/${income}")
                                        isLoading = true

                                        viewModel.registerUser(
                                            name = name,
                                            phone = phone,
                                            ssnFront = ssnFront,
                                            pin = confirmedPin,
                                            income = income,
                                            onSuccess = {
                                                saveLoginMethod(context, "pin")
                                                navController.navigate("card_select")
                                            },
                                            onFailure = { error ->
                                                isLoading = false
                                                errorMessage = error
                                            }
                                        )
                                    } else {
                                        errorMessage = "PIN 번호가 일치하지 않습니다"
                                        currentStep = PinStep.PIN
                                        pin = ""
                                    }
                                }
                            },
                            onBack = {
                                if (currentStep == PinStep.PIN_CONFIRM) {
                                    currentStep = PinStep.PIN
                                }
                            }
                        )
                    }
                }
                PinStep.DONE -> {
                    // 처리 완료 (이미 onSuccess에서 네비게이션 처리)
                }
            }
        }
    }
}