package com.example.fe.ui.components.backgrounds

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun StarryBackground(
    scrollOffset: Float = 0f,
    horizontalOffset: Float = 0f,
    starCount: Int = 100,
    content: @Composable BoxScope.() -> Unit
) {
    // 별들의 위치와 크기를 저장
    val stars = remember {
        List(starCount) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1f,
                alpha = Random.nextFloat() * 0.5f + 0.5f
            )
        }
    }
    
    // 애니메이션 적용된 가로 오프셋
    val animatedHorizontalOffset by animateFloatAsState(
        targetValue = horizontalOffset,
        animationSpec = tween(500),
        label = "horizontalOffset"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A1A))
    ) {
        // 별이 빛나는 배경
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val time = System.currentTimeMillis() / 1000f
            
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
    // 낮은 확률로 반짝이는 효과 (20% 확률로 반짝임)
    val shouldTwinkle = star.flickerProbability < 0.2f
    
    // 반짝임 효과 계산
    val flicker = if (shouldTwinkle) {
        (sin(time * star.flickerSpeed) + 1) / 2f * 0.7f + 0.3f
    } else {
        1f // 반짝이지 않음
    }
    
    drawCircle(
        color = color.copy(alpha = star.alpha * flicker),
        radius = radius,
        center = center
    )
    
    // 별빛 효과
    drawCircle(
        color = color.copy(alpha = star.alpha * flicker * 0.3f),
        radius = radius * 1.5f,
        center = center
    )
}

private data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val flickerSpeed: Float = Random.nextFloat() * 2f + 1f,
    val flickerProbability: Float = Random.nextFloat() // 0~1 사이 랜덤 값
) 