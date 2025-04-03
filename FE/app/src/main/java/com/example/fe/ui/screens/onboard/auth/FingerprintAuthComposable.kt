package com.example.fe.ui.screens.onboard.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.example.fe.ui.screens.onboard.screen.setup.findActivity

@Composable
fun FingerprintAuthComposable(
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    LaunchedEffect(Unit) {
        if (activity != null) {
            FingerprintAuth.authenticate(activity) { success ->
                onResult(success)
            }
        } else {
            onResult(false)
        }
    }
} 