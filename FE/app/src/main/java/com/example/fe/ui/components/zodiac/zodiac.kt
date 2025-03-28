package com.example.fe.ui.components.zodiac

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// 별자리 데이터 클래스
data class Star(val x: Float, val y: Float, val size: Float = 1f)
data class Constellation(
    val stars: List<Star>, 
    val connections: List<Pair<Int, Int>>,
    val centralStarIndex: Int = 0
)

@Composable
fun DynamicZodiacView(
    cardId: String,
    modifier: Modifier = Modifier,
    useJSON: Boolean = true,
    useBackend: Boolean = false // 백엔드 연동 옵션 추가
) {
    // JSON 사용 여부에 따라 별자리 생성 방식 선택
    val constellation = if (useJSON) {
        rememberZodiacFromJSON(cardId, useBackend)  // 백엔드 연동 옵션 전달
    } else {
        val random = Random(cardId.hashCode())
        remember(cardId) {
            generateConstellation(random)  // 기존 방식으로 생성
        }
    }
    
    // 애니메이션 효과 - 프론트엔드에서 처리
    val infiniteTransition = rememberInfiniteTransition(label = "zodiac")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )
    
    // 별자리 그리기
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val scale = min(canvasWidth, canvasHeight) / 1000f
        
        // 배경 효과 - 프론트엔드에서 처리
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A237E).copy(alpha = 0.3f),
                    Color(0xFF0D47A1).copy(alpha = 0.15f),
                    Color(0xFF000000).copy(alpha = 0.0f)
                ),
                center = Offset(centerX, centerY),
                radius = min(canvasWidth, canvasHeight) * 0.8f
            ),
            radius = min(canvasWidth, canvasHeight) * 0.8f,
            center = Offset(centerX, centerY)
        )
        
        // 별자리 선 그리기 - JSON에서 가져온 데이터 사용
        constellation.connections.forEach { (startIdx, endIdx) ->
            val startStar = constellation.stars[startIdx]
            val endStar = constellation.stars[endIdx]
            
            val startX = centerX + startStar.x * canvasWidth * 0.8f
            val startY = centerY + startStar.y * canvasHeight * 0.8f
            val endX = centerX + endStar.x * canvasWidth * 0.8f
            val endY = centerY + endStar.y * canvasHeight * 0.8f
            
            // 선 그리기 - 그라데이션 효과 (더 강하게)
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD).copy(alpha = 0.95f),  // 더 진한 색상
                        Color(0xFFBBDEFB).copy(alpha = 0.75f)   // 더 진한 색상
                    ),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY)
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.5f * scale,  // 더 굵게
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
            
            // 선 주변 빛 효과 (더 강하게)
            drawLine(
                color = Color(0xFFE1F5FE).copy(alpha = 0.5f),  // 더 진한 색상
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 12f * scale,  // 더 굵게
                cap = StrokeCap.Round
            )
        }
        
        // 별 그리기 - JSON에서 가져온 데이터 사용
        constellation.stars.forEachIndexed { index, star ->
            val x = centerX + star.x * canvasWidth * 0.8f
            val y = centerY + star.y * canvasHeight * 0.8f
            val isCenterStar = index == constellation.centralStarIndex
            
            // 별 크기 및 밝기 계산 (중심 별은 더 크고 밝게)
            val starSize = if (isCenterStar) {
                star.size * 12f * scale  // 더 크게
            } else {
                star.size * 7f * scale  // 더 크게
            }
            
            val starAlpha = if (isCenterStar) {
                1.0f
            } else {
                0.9f + (star.size - 0.5f) * 0.6f  // 더 밝게
            }
            
            // 별 주변 빛 효과 - 그라데이션 (모든 별에 적용)
            val glowRadius = if (isCenterStar) starSize * 8f else starSize * 5f
            
            // 외부 빛 효과 (그라데이션)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE1F5FE).copy(alpha = starAlpha * 0.6f),
                        Color(0xFFB3E5FC).copy(alpha = starAlpha * 0.3f),
                        Color(0xFF81D4FA).copy(alpha = 0f)
                    ),
                    center = Offset(x, y),
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = Offset(x, y)
            )
            
            // 중간 빛 효과
            drawCircle(
                color = Color(0xFFE1F5FE).copy(alpha = starAlpha * 0.9f),
                radius = starSize * 2.5f,
                center = Offset(x, y)
            )
            
            // 별 자체
            drawCircle(
                color = Color.White.copy(alpha = starAlpha),
                radius = starSize,
                center = Offset(x, y)
            )
            
            // 중심 별에 더 강한 방사 효과 추가
            if (isCenterStar) {
                // 주요 방사 효과 - 8방향 긴 선
                for (i in 0 until 8) {
                    val angle = (i * Math.PI / 4).toFloat()
                    val rayLength = starSize * 6f  // 더 길게
                    val rayEndX = x + cos(angle) * rayLength
                    val rayEndY = y + sin(angle) * rayLength
                    
                    // 그라데이션 선 방사 효과
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color(0xFFE1F5FE).copy(alpha = 0.3f)
                            ),
                            start = Offset(x, y),
                            end = Offset(rayEndX, rayEndY)
                        ),
                        start = Offset(x, y),
                        end = Offset(rayEndX, rayEndY),
                        strokeWidth = 2.5f * scale,  // 더 굵게
                        cap = StrokeCap.Round
                    )
                }
                
                // 보조 방사 효과 - 8방향 짧은 선
                for (i in 0 until 8) {
                    val angle = (i * Math.PI / 4 + Math.PI / 8).toFloat()
                    val rayLength = starSize * 4f
                    val rayEndX = x + cos(angle) * rayLength
                    val rayEndY = y + sin(angle) * rayLength
                    
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.8f),
                                Color(0xFFE1F5FE).copy(alpha = 0.2f)
                            ),
                            start = Offset(x, y),
                            end = Offset(rayEndX, rayEndY)
                        ),
                        start = Offset(x, y),
                        end = Offset(rayEndX, rayEndY),
                        strokeWidth = 1.8f * scale,
                        cap = StrokeCap.Round
                    )
                }
                
                // 추가 빛 효과 - 중심 별 주변 밝은 원
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE1F5FE).copy(alpha = 0.3f),
                            Color(0xFFB3E5FC).copy(alpha = 0.15f),
                            Color(0xFF81D4FA).copy(alpha = 0f)
                        ),
                        center = Offset(x, y),
                        radius = starSize * 12f
                    ),
                    radius = starSize * 12f,  // 매우 넓게
                    center = Offset(x, y)
                )
            }
            
            // 일반 별에도 방사 효과 추가 (더 작게)
            else {
                // 4방향 방사 효과
                for (i in 0 until 4) {
                    val angle = (i * Math.PI / 2).toFloat()
                    val rayLength = starSize * 3f * twinkle
                    val rayEndX = x + cos(angle) * rayLength
                    val rayEndY = y + sin(angle) * rayLength
                    
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.7f),
                                Color(0xFFE1F5FE).copy(alpha = 0.2f)
                            ),
                            start = Offset(x, y),
                            end = Offset(rayEndX, rayEndY)
                        ),
                        start = Offset(x, y),
                        end = Offset(rayEndX, rayEndY),
                        strokeWidth = 1.5f * scale,
                        cap = StrokeCap.Round
                    )
                }
                
                // 원형 빛 효과 추가
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE1F5FE).copy(alpha = 0.25f),
                            Color(0xFFB3E5FC).copy(alpha = 0.1f),
                            Color(0xFF81D4FA).copy(alpha = 0f)
                        ),
                        center = Offset(x, y),
                        radius = starSize * 6f
                    ),
                    radius = starSize * 6f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

// 별자리 생성 함수 - 교차하지 않는 선 생성 알고리즘 개선
internal fun generateConstellation(random: Random): Constellation {
    val starCount = 6 + random.nextInt(3)  // 6-8개의 별 (더 적게)
    val stars = mutableListOf<Star>()
    
    // 중심 별 (항상 중앙 근처에 위치)
    val centerStarIndex = 0  // 중심 별 인덱스를 첫 번째로 고정
    
    // 중심 별 생성
    stars.add(Star(
        x = (random.nextFloat() * 0.1f - 0.05f),  // 중앙 근처에 위치
        y = (random.nextFloat() * 0.1f - 0.05f),
        size = 1.8f + random.nextFloat() * 0.4f  // 더 크게
    ))
    
    // 나머지 별들을 방사형으로 배치 (교차 방지)
    for (i in 1 until starCount) {
        // 방사형 배치를 위한 각도 계산 (균등하게 분포)
        val angle = 2 * Math.PI * (i - 1) / (starCount - 1)
        
        // 거리는 랜덤하게 (하지만 최소 거리 보장)
        val distance = 0.25f + random.nextFloat() * 0.25f
        
        // 위치 계산
        val x = (cos(angle) * distance).toFloat()
        val y = (sin(angle) * distance).toFloat()
        
        // 크기는 중심에서 멀수록 작게
        val size = 1.2f - distance * 0.5f + random.nextFloat() * 0.3f
        
        stars.add(Star(x, y, size))
    }
    
    // 별자리 연결선 생성 (교차 방지)
    val connections = mutableListOf<Pair<Int, Int>>()
    
    // 1. 중심 별과 다른 별들 연결 (방사형 구조)
    for (i in 1 until starCount) {
        connections.add(Pair(centerStarIndex, i))
    }
    
    // 2. 인접한 별들끼리 연결 (원형 구조) - 교차 방지
    for (i in 1 until starCount - 1) {
        // 인접한 별들만 연결 (교차 방지)
        if (random.nextFloat() > 0.3f) {  // 70% 확률로 연결
            connections.add(Pair(i, i + 1))
        }
    }
    
    // 마지막 별과 첫 번째 별(중심 제외) 연결 (원 완성)
    if (starCount > 3 && random.nextFloat() > 0.5f) {  // 50% 확률로 연결
        connections.add(Pair(1, starCount - 1))
    }
    
    return Constellation(stars, connections, centerStarIndex)
}
