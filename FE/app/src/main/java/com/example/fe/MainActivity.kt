package com.example.fe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fe.ui.navigation.AppNavigation
import com.example.fe.ui.navigation.OnboardingNavHost
import com.example.fe.ui.theme.FETheme
import com.example.fe.ui.screens.onboard.OnboardingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FETheme {
                val viewModel: OnboardingViewModel = viewModel()
                if (!viewModel.isLoggedIn) {
                    OnboardingNavHost(viewModel)
                } else {
                    AppNavigation()
                }
            }
        }
    }
}