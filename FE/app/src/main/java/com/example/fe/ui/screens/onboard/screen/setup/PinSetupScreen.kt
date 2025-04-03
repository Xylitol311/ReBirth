package com.example.fe.ui.screens.onboard.screen.setup
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fe.ui.screens.onboard.OnboardingViewModel
import com.example.fe.ui.screens.onboard.auth.PinAuth
import com.example.fe.ui.screens.onboard.components.login.saveLoginMethod
import kotlinx.coroutines.launch

enum class PinStep { PIN, PIN_CONFIRM, DONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSetupScreen(
    navController: NavController,
    viewModel: OnboardingViewModel
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(PinStep.PIN) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            if (currentStep == PinStep.PIN_CONFIRM) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { currentStep = PinStep.PIN },
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(
                                androidx.compose.material.icons.Icons.Default.ArrowBack,
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
            when (currentStep) {
                PinStep.PIN, PinStep.PIN_CONFIRM -> PinAuth(
                    currentStep = if (currentStep == PinStep.PIN) PinStep.PIN else PinStep.PIN_CONFIRM,
                    onPinConfirmed = { pin ->
                        viewModel.hasPinAuth = true
                        viewModel.setUserPin(pin)
                        scope.launch {
                            saveLoginMethod(context, "pin")
                            currentStep = PinStep.DONE
                        }
                    },
                    onStepChange = {
                        currentStep = when (it) {
                            PinStep.PIN -> PinStep.PIN
                            PinStep.PIN_CONFIRM -> PinStep.PIN_CONFIRM
                            else -> currentStep
                        }
                    }
                )

                PinStep.DONE -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("card_select")
                    }
                }
            }
        }
    }
}