// package com.example.fe.data.repository

// import android.util.Log
// import com.example.fe.config.AppConfig
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.withContext
// import okhttp3.OkHttpClient
// import okhttp3.Request
// import java.util.concurrent.TimeUnit

// class ServerStatusRepository {
//     private val client = OkHttpClient.Builder()
//         .connectTimeout(5, TimeUnit.SECONDS)
//         .readTimeout(5, TimeUnit.SECONDS)
//         .build()
    
//     suspend fun checkServerStatus(): Boolean = withContext(Dispatchers.IO) {
//         try {
//             val request = Request.Builder()
//                 .url("${AppConfig.Server.BASE_URL}health")
//                 .build()
            
//             val response = client.newCall(request).execute()
//             val isSuccess = response.isSuccessful
            
//             Log.d("ServerStatus", "서버 상태 확인: $isSuccess (${response.code})")
            
//             return@withContext isSuccess
//         } catch (e: Exception) {
//             Log.e("ServerStatus", "서버 상태 확인 실패", e)
//             return@withContext false
//         }
//     }
// } 