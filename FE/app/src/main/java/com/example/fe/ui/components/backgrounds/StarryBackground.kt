package com.example.fe.ui.components.backgrounds

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun StarryBackground(
    scrollOffset: Float = 0f,
    horizontalOffset: Float = 0f,
    starCount: Int = 100,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // 별들의 위치와 크기를 저장
    val stars = remember {
        List(starCount) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                alpha = Random.nextFloat() * 0.5f + 0.5f,
                // 모든 별이 같은 속도로 반짝이도록 고정 값 사용
                flickerSpeed = 0.2f, 
                // 10%의 별만 반짝이도록 설정
                shouldTwinkle = Random.nextFloat() < 0.1f
            )
        }
    }
    
    // 애니메이션 적용된 가로 오프셋 - 더 빠른 애니메이션
    val animatedHorizontalOffset by animateFloatAsState(
        targetValue = horizontalOffset,
        // 더 빠른 애니메이션과 가속/감속 효과
        animationSpec = tween(800, easing = EaseInOut),
        label = "horizontalOffset"
    )
    
    // 별의 상태를 업데이트하기 위한 시간 값
    var time by remember { mutableStateOf(0f) }
    
    // 느린 속도로 시간 업데이트 (500ms마다)
    LaunchedEffect(Unit) {
        while (true) {
            delay(500) // 0.5초마다 업데이트
            time += 0.1f // 작은 증분으로 천천히 변화
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A1A))
    ) {
        // 별이 빛나는 배경
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // 별 그리기
            stars.forEach { star ->
                // 스크롤 오프셋에 따라 별의 위치 조정 (수직)
                val yOffset = (scrollOffset / 1000f) % 1f
                
                // 가로 방향 이동에 따른 별의 위치 조정 (수평)
                val xOffset = (animatedHorizontalOffset / (canvasWidth * 0.5f))
                
                // 최종 별 위치 계산 (무한 스크롤을 위해 모듈로 연산)
                val x = (star.x - xOffset) % 1f
                // 음수 처리 (모듈로 연산 결과가 음수일 경우)
                val adjustedX = if (x < 0) x + 1f else x
                
                // 별의 실제 화면 좌표
                val xPos = adjustedX * canvasWidth
                val yPos = (star.y - yOffset) % 1f * canvasHeight
                
                // 별 그리기
                drawStar(
                    center = Offset(xPos, yPos),
                    radius = star.size,
                    color = Color.White,
                    time = time,
                    star = star
                )
            }
        }
        
        // 내용 표시
        content()
    }
}

private fun DrawScope.drawStar(
    center: Offset, 
    radius: Float, 
    color: Color, 
    time: Float,
    star: Star
) {
    // 반짝임 효과 계산 - 더 느리고 부드러운 변화
    val flicker = if (star.shouldTwinkle) {
        // sin 함수로 0.6f에서 1.3f 사이로 천천히 변화
        0.6f + (sin(time * star.flickerSpeed) + 1) / 2f * 0.7f
    } else {
        0.8f // 반짝이지 않는 별은 일정한 밝기
    }
    
    // 반짝이는 별은 크기도 약간 변화
    val sizeMultiplier = if (star.shouldTwinkle) {
        0.9f + (sin(time * star.flickerSpeed * 0.8f) + 1) / 2f * 0.2f
    } else {
        1.0f
    }
    
    // 별 그리기 (반짝이는 별은 더 밝고 크게)
    drawCircle(
        color = color.copy(alpha = star.alpha * flicker),
        radius = radius * sizeMultiplier,
        center = center
    )
    
    // 별빛 효과 (반짝이는 별은 더 큰 광채)
    val glowSize = if (star.shouldTwinkle) 2.0f else 1.5f
    drawCircle(
        color = color.copy(alpha = star.alpha * flicker * 0.3f),
        radius = radius * glowSize * sizeMultiplier,
        center = center
    )
    
    // 반짝이는 별에만 추가 광채 효과
    if (star.shouldTwinkle && flicker > 1.0f) {
        drawCircle(
            color = color.copy(alpha = star.alpha * (flicker - 1.0f) * 0.2f),
            radius = radius * 3f * sizeMultiplier,
            center = center
        )
    }
}

private data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val flickerSpeed: Float, // 반짝임 속도 (고정값)
    val shouldTwinkle: Boolean // 반짝일지 여부 (10%만 true)
)