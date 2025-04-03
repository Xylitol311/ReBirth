package com.example.fe.ui.screens.onboard.components.login

import android.content.Context
import androidx.compose.runtime.Composable


fun saveLoginMethod(context: Context, method: String) {
    val sharedPref = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("login_method", method)
        apply()
    }
}


fun getSavedLoginMethod(context: Context): String? {
    val sharedPref = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
    return sharedPref.getString("login_method", null)
}
