package com.example.fe.ui.screens.onboard.components.device

import javax.inject.Inject
import android.content.Context
import android.os.Build
import android.provider.Settings

interface DeviceInfoManager {
    fun getDeviceId(): String
    fun getDeviceName(): String
}

class AndroidDeviceInfoManager @Inject constructor(
    private val context: Context
) : DeviceInfoManager {

    override fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun getDeviceName(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            "${Build.MANUFACTURER} ${Build.MODEL}"
        } else {
            "${Build.BRAND} ${Build.MODEL}"
        }
    }
}