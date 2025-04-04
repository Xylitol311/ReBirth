package com.example.fe.ui.screens.onboard.screen.setup

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fe.ui.screens.onboard.viewmodel.OnboardingViewModel
import com.example.fe.ui.screens.onboard.auth.PatternAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternLoginScreen(
    navController: NavController,
    viewModel: OnboardingViewModel
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(AdditionalSecurityStep.METHOD) }
    var savedPattern by remember { mutableStateOf<List<Int>?>(null) }

    Scaffold(
        topBar = {
            if (currentStep == AdditionalSecurityStep.PATTERN_CONFIRM) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { currentStep = AdditionalSecurityStep.PATTERN },
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
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

                PatternAuth(
                    currentStep = AdditionalSecurityStep.PATTERN, // Need to use SecurityStep enum here
                    onPatternConfirmed = {
                        Log.d("PatternAUTH","savedPattern11 : $savedPattern / it : $it")
                        savedPattern = it
                        currentStep = AdditionalSecurityStep.PATTERN_CONFIRM
                    },
                    onStepChange = { _ -> }
                )

                PatternAuth(
                    currentStep = AdditionalSecurityStep.PATTERN_CONFIRM, // Need to use SecurityStep enum here
                    onPatternConfirmed = {
                        Log.d("PatternAUTH","savedPattern : $savedPattern / it : $it")
                        // 리스트 내용을 명시적으로 비교
                        if (savedPattern != null && it.size == savedPattern!!.size &&
                            it.zip(savedPattern!!).all { (a, b) -> a == b }) {
                            viewModel.hasPatternAuth = true
                            currentStep = AdditionalSecurityStep.DONE
                        } else {
                            Toast.makeText(context, "패턴이 틀렸습니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show()
                            currentStep = AdditionalSecurityStep.PATTERN
                        }
                    },
                    onStepChange = { _ -> }
                )


        }
    }
}
