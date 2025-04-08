package com.example.fe.ui.components.images

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.fe.R

/**
 * 네트워크 이미지 로딩을 위한 컴포넌트
 * 
 * 로딩 상태, 오류 처리, 회전 및 기타 효과를 포함합니다.
 * 
 * @param url 이미지 URL
 * @param contentDescription 이미지 설명
 * @param modifier 컴포넌트 수정자
 * @param alignment 이미지 정렬
 * @param contentScale 이미지 스케일링 방식
 * @param alpha 투명도
 * @param colorFilter 색상 필터
 * @param rotationZ 이미지 회전 각도
 * @param fallbackResId 로드 실패 시 표시할 기본 이미지 리소스 ID
 * @param onSuccess 이미지 로드 성공 시 콜백
 * @param onError 이미지 로드 실패 시 콜백
 */
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    rotationZ: Float = 0f,
    fallbackResId: Int = R.drawable.card,
    onSuccess: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
) {
    val imageUrl = url.trim()
    val TAG = "NetworkImage"
    
    // 디버깅을 위한 상세 로그
    Log.d(TAG, "🖼️ 이미지 로드 시작: $imageUrl")
    
    // 이미지 요청 설정 최적화
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context)
        .data(imageUrl)
        .crossfade(500) // 크로스페이드 애니메이션 활성화 (500ms)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .listener(
            onStart = { 
                Log.d(TAG, "🔄 이미지 요청 시작: $imageUrl")
            },
            onSuccess = { _, _ ->
                Log.d(TAG, "✅ 이미지 요청 성공: $imageUrl")
            },
            onError = { _, error ->
                Log.e(TAG, "⚠️ 이미지 요청 실패: $imageUrl")
                error.throwable?.message?.let { errorMsg ->
                    Log.e(TAG, "⚠️ 오류 메시지: $errorMsg")
                }
            }
        )
        .build()
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // SubcomposeAsyncImage 사용해서 로딩/에러 상태 세밀하게 처리
        SubcomposeAsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(rotationZ = rotationZ),
            contentScale = contentScale,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
            onSuccess = {
                Log.d(TAG, "✅ 이미지 표시 성공: $imageUrl")
                onSuccess?.invoke()
            },
            onError = {
                Log.e(TAG, "⚠️ 이미지 표시 실패: $imageUrl")
                onError?.invoke()
            },
            error = {
                Log.d(TAG, "🔄 폴백 이미지 표시 중: $imageUrl")
                Image(
                    painter = painterResource(id = fallbackResId),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    alignment = alignment,
                    alpha = alpha,
                    colorFilter = colorFilter
                )
            },
            loading = {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        )
    }
} 