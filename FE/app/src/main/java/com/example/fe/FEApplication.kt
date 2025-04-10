package com.example.fe

import android.app.Application
import com.example.fe.data.local.TokenManager

class FEApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // TokenManager 초기화
        TokenManager.initialize(this)
    }
} 