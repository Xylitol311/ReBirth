package com.example.fe.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.fe.ui.screens.onboard.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val LOGIN_METHOD = stringPreferencesKey("login_method")

suspend fun saveLoginMethod(context: Context, method: String) {
    context.dataStore.edit { preferences ->
        preferences[LOGIN_METHOD] = method
    }
}

suspend fun getLoginMethod(context: Context): String? {
    var method: String? = null
    context.dataStore.data.collect { preferences ->
        method = preferences[LOGIN_METHOD]
    }
    return method
} 