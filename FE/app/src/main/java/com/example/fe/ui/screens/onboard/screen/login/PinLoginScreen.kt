package com.example.fe.ui.screens.onboard.screen.login

import com.example.fe.ui.screens.onboard.OnboardingViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.fe.ui.screens.onboard.auth.PinLoginAuth
import com.example.fe.ui.screens.onboard.screen.setup.PinStep


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinLoginScreen(
    navController: NavController,
    viewModel: OnboardingViewModel
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(PinStep.PIN) }
    Scaffold(
        //상단 및 하단바 없음

    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

                PinLoginAuth(
                    onSuccessfulLogin = {
                        navController.navigate("home")
                    }

                )
        }
    }
}