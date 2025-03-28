package com.example.fe.ui.screens.payment.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun PaymentAddCardSection(
    modifier: Modifier = Modifier,
    onAddCardClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A1E))
    ) {
        // 중앙 - 안내 메시지
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "어떤 별자리의 도움을 받아볼까요?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "카드를 추가하면 별자리가 생성됩니다",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ConstellationCarousel(
    modifier: Modifier = Modifier
) {
    // 여러 별자리 생성 (더 적은 수로 변경)
    val constellations = remember {
        List(3) { index ->
            generateEnhancedConstellation(Random(index), index)
        }
    }
    
    // 자동 회전 애니메이션 (더 느리게)
    val infiniteTransition = rememberInfiniteTransition(label = "carousel")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing)
        ),
        label = "carousel_rotation"
    )
    
    // 별 깜빡임 애니메이션
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val radius = minOf(centerX, centerY) * 0.7f
        
        // 배경 효과 - 은하수 느낌
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A237E).copy(alpha = 0.25f),
                    Color(0xFF0D47A1).copy(alpha = 0.08f),
                    Color(0xFF000000).copy(alpha = 0.0f)
                ),
                center = Offset(centerX, centerY),
                radius = radius * 1.5f
            ),
            radius = radius * 1.5f,
            center = Offset(centerX, centerY)
        )
        
        // 각 별자리를 원형으로 배치 (더 큰 크기로, 더 멀리 떨어지게)
        constellations.forEachIndexed { index, constellation ->
            val angle = Math.toRadians((rotation + index * (360f / constellations.size)).toDouble())
            val distance = radius * 0.8f  // 0.6f에서 0.8f로 증가하여 더 멀리 배치
            val x = centerX + cos(angle).toFloat() * distance
            val y = centerY + sin(angle).toFloat() * distance
            
            // 거리에 따른 크기와 투명도 조정 (더 극적인 변화)
            val distanceFromCenter = sqrt((x - centerX).pow(2) + (y - centerY).pow(2))
            val maxDistance = radius * 0.8f
            val scale = 1.5f - (distanceFromCenter / maxDistance) * 0.7f
            val alpha = 1f - (distanceFromCenter / maxDistance) * 0.3f
            
            // 별자리 그리기 (더 크고 화려하게)
            drawEnhancedConstellation(
                constellation = constellation,
                centerX = x,
                centerY = y,
                scale = scale * 1.5f,
                alpha = alpha * twinkle,
                rotation = index * 72f,
                twinkle = twinkle
            )
        }
    }
}

private fun DrawScope.drawEnhancedConstellation(
    constellation: Constellation,
    centerX: Float,
    centerY: Float,
    scale: Float,
    alpha: Float,
    rotation: Float,
    twinkle: Float
) {
    // 별자리 크기 (크게 증가)
    val constellationSize = 300f * scale  // 더 크게 조정
    
    // 별자리 배경 효과 - 은하 느낌
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF5D4037).copy(alpha = 0.15f * alpha),
                Color(0xFF3E2723).copy(alpha = 0.08f * alpha),
                Color(0xFF000000).copy(alpha = 0.0f)
            ),
            center = Offset(centerX, centerY),
            radius = constellationSize * 0.7f
        ),
        radius = constellationSize * 0.7f,
        center = Offset(centerX, centerY)
    )
    
    // 별자리 선 그리기 (더 화려하게)
    constellation.connections.forEach { (startIdx, endIdx) ->
        val startStar = constellation.stars[startIdx]
        val endStar = constellation.stars[endIdx]
        
        // 회전 및 위치 조정
        val startX = centerX + rotateX(startStar.x - 0.5f, startStar.y - 0.5f, rotation) * constellationSize
        val startY = centerY + rotateY(startStar.x - 0.5f, startStar.y - 0.5f, rotation) * constellationSize
        val endX = centerX + rotateX(endStar.x - 0.5f, endStar.y - 0.5f, rotation) * constellationSize
        val endY = centerY + rotateY(endStar.x - 0.5f, endStar.y - 0.5f, rotation) * constellationSize
        
        // 선 그리기 - 그라데이션 효과
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE3F2FD).copy(alpha = alpha * 0.9f),
                    Color(0xFFBBDEFB).copy(alpha = alpha * 0.7f)
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 3.0f * scale,  // 선 굵기 증가
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)  // 대시 패턴 조정
        )
        
        // 선 주변 빛 효과
        drawLine(
            color = Color(0xFFE1F5FE).copy(alpha = alpha * 0.3f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 10f * scale,  // 빛 효과 증가
            cap = StrokeCap.Round
        )
    }
    
    // 별 그리기 (더 화려하게)
    constellation.stars.forEach { star ->
        // 회전 및 위치 조정
        val x = centerX + rotateX(star.x - 0.5f, star.y - 0.5f, rotation) * constellationSize
        val y = centerY + rotateY(star.x - 0.5f, star.y - 0.5f, rotation) * constellationSize
        
        // 별 주변 빛 효과 (더 넓게)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE1F5FE).copy(alpha = alpha * 0.4f),
                    Color(0xFFB3E5FC).copy(alpha = alpha * 0.2f),
                    Color(0xFF81D4FA).copy(alpha = 0f)
                ),
                center = Offset(x, y),
                radius = star.size * 25f * scale  // 빛 효과 크기 증가
            ),
            radius = star.size * 25f * scale,
            center = Offset(x, y)
        )
        
        // 중간 빛 효과
        drawCircle(
            color = Color(0xFFE1F5FE).copy(alpha = alpha * 0.7f),
            radius = star.size * 12f * scale,  // 크기 증가
            center = Offset(x, y)
        )
        
        // 별 자체 (더 밝게)
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = star.size * 6f * scale,  // 별 크기 증가
            center = Offset(x, y)
        )
        
        // 별 빛 방사 효과
        for (i in 0 until 6) {  // 6방향으로 증가
            val angle = (i * Math.PI / 3 + rotation / 180 * Math.PI).toFloat()
            val rayLength = star.size * 18f * scale * twinkle  // 길이 증가
            val rayEndX = x + cos(angle) * rayLength
            val rayEndY = y + sin(angle) * rayLength
            
            drawLine(
                color = Color.White.copy(alpha = alpha * 0.4f * twinkle),
                start = Offset(x, y),
                end = Offset(rayEndX, rayEndY),
                strokeWidth = 1.5f * scale,  // 굵기 증가
                cap = StrokeCap.Round
            )
        }
    }
}

// 향상된 별자리 생성 함수
private fun generateEnhancedConstellation(random: Random, index: Int): Constellation {
    // 별자리 종류에 따라 다른 패턴 생성 (더 간결하게)
    val pattern = when (index % 3) {
        0 -> { // 물고기자리 (Pisces) - 간결한 버전
            val stars = listOf(
                Star(0.3f, 0.4f, 1.7f),
                Star(0.45f, 0.3f, 1.5f),
                Star(0.6f, 0.35f, 1.8f),
                Star(0.7f, 0.5f, 1.6f),
                Star(0.55f, 0.6f, 1.4f)
            )
            val connections = listOf(
                Pair(0, 1),
                Pair(1, 2),
                Pair(2, 3),
                Pair(3, 4)
            )
            Constellation(stars, connections)
        }
        1 -> { // 천칭자리 (Libra) - 간결한 버전
            val stars = listOf(
                Star(0.3f, 0.3f, 1.9f),
                Star(0.5f, 0.25f, 1.6f),
                Star(0.7f, 0.3f, 1.8f),
                Star(0.4f, 0.5f, 1.5f),
                Star(0.6f, 0.5f, 1.7f)
            )
            val connections = listOf(
                Pair(0, 1),
                Pair(1, 2),
                Pair(1, 3),
                Pair(1, 4)
            )
            Constellation(stars, connections)
        }
        else -> { // 전갈자리 (Scorpio) - 간결한 버전
            val stars = listOf(
                Star(0.25f, 0.3f, 1.8f),
                Star(0.4f, 0.35f, 1.6f),
                Star(0.55f, 0.4f, 2.0f),  // 안타레스 (가장 밝은 별)
                Star(0.7f, 0.45f, 1.7f),
                Star(0.8f, 0.55f, 1.5f),
                Star(0.75f, 0.7f, 1.4f)
            )
            val connections = listOf(
                Pair(0, 1),
                Pair(1, 2),
                Pair(2, 3),
                Pair(3, 4),
                Pair(4, 5)
            )
            Constellation(stars, connections)
        }
    }
    
    return pattern
}

// 회전 변환 함수
private fun rotateX(x: Float, y: Float, degrees: Float): Float {
    val radians = Math.toRadians(degrees.toDouble())
    return (x * cos(radians) - y * sin(radians)).toFloat()
}

private fun rotateY(x: Float, y: Float, degrees: Float): Float {
    val radians = Math.toRadians(degrees.toDouble())
    return (x * sin(radians) + y * cos(radians)).toFloat()
}

// 별자리 데이터 클래스
private data class Star(val x: Float, val y: Float, val size: Float = 1f)
private data class Constellation(val stars: List<Star>, val connections: List<Pair<Int, Int>>) 