package com.example.fe.ui.components.loadings

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 전체 화면을 덮는 로딩 오버레이
 */
@Composable
fun FullScreenLoading(
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    Box {
        content()
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularLoading()
            }
        }
    }
}

/**
 * 원형 프로그레스 인디케이터를 사용한 로딩 컴포넌트
 */
@Composable
fun CircularLoading() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp),
            color = Color(0xFF2196F3), // 파란색 프로그레스 인디케이터
            strokeWidth = 5.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "로딩 중...",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 펄스 애니메이션을 사용한 로딩 컴포넌트
 */
@Composable
fun PulseLoading() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // 첫 번째 원의 애니메이션
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale1"
    )
    
    // 두 번째 원의 애니메이션 (약간의 지연)
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale2"
    )
    
    // 세 번째 원의 애니메이션 (더 많은 지연)
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, delayMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale3"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 첫 번째 원
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .scale(scale1)
                    .background(Color(0xFF2196F3), CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 두 번째 원
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .scale(scale2)
                    .background(Color(0xFF2196F3), CircleShape)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // 세 번째 원
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .scale(scale3)
                    .background(Color(0xFF2196F3), CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "로딩 중...",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 다이얼로그 형태의 로딩 컴포넌트
 */
@Composable
fun LoadingDialog(
    isLoading: Boolean,
    onDismissRequest: () -> Unit = {}
) {
    if (isLoading) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color.Black.copy(alpha = 0.8f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF2196F3),
                        strokeWidth = 4.dp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "잠시만 기다려주세요",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircularLoadingPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularLoading()
    }
}

@Preview(showBackground = true)
@Composable
fun PulseLoadingPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        PulseLoading()
    }
}

@Preview
@Composable
fun LoadingDialogPreview() {
    LoadingDialog(isLoading = true)
}
