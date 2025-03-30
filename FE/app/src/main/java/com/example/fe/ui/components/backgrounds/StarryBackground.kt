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

// 추가 임포트
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.draw.drawWithContent

// 글래스 서피스를 위한 로컬 컴포지션 객체
val LocalStarryBackgroundState = compositionLocalOf { StarryBackgroundState() }

// 스타리 배경 상태를 저장하기 위한 클래스
class StarryBackgroundState {
    var isInitialized = false
    internal var stars: List<Star> = emptyList()  // internal로 변경하여 같은 모듈 내에서만 접근 가능하게 함
    var time: Float = 0f
    var backgroundColor = Color(0xFF0A0A1A)
}

// Star 클래스를 internal로 변경하여 같은 모듈 내에서 접근 가능하게 함
internal data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float,
    val flickerSpeed: Float, // 반짝임 속도 (고정값)
    val shouldTwinkle: Boolean // 반짝일지 여부 (10%만 true)
)

@Composable
fun StarryBackground(
    scrollOffset: Float = 0f,
    horizontalOffset: Float = 0f,
    starCount: Int = 100,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // 글로벌 상태 저장
    val starryBackgroundState = remember { StarryBackgroundState() }
    
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
    
    // 상태를 글로벌 상태에 저장
    starryBackgroundState.stars = stars
    starryBackgroundState.isInitialized = true
    starryBackgroundState.backgroundColor = Color(0xFF0A0A1A)
    
    // 애니메이션 적용된 가로 오프셋 - 더 빠른 애니메이션
    val animatedHorizontalOffset by animateFloatAsState(
        targetValue = horizontalOffset,
        // 더 빠른 애니메이션과 가속/감속 효과
        animationSpec = tween(800, easing = EaseInOut),
        label = "horizontalOffset"
    )
    
    // 별의 상태를 업데이트하기 위한 시간 값
    var time by remember { mutableStateOf(0f) }
    
    // 시간 값을 글로벌 상태에 저장
    starryBackgroundState.time = time
    
    // 느린 속도로 시간 업데이트 (500ms마다)
    LaunchedEffect(Unit) {
        while (true) {
            delay(500) // 0.5초마다 업데이트
            time += 0.1f // 작은 증분으로 천천히 변화
        }
    }
    
    CompositionLocalProvider(LocalStarryBackgroundState provides starryBackgroundState) {
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
}

// 글래스 효과를 가진 표면 컴포저블
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Float = 16f,
    color: Color = Color(0x60FFFFFF),
    borderColor: Color = Color(0xAAFFFFFF),
    blurRadius: Float = 10f,
    content: @Composable BoxScope.() -> Unit
) {
    // 배경과 컨텐츠를 분리하여 배경만 블러 처리
    Box(modifier = modifier) {
        // 배경 레이어 (블러 효과 적용)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius.dp))
                .blur(radius = 25.dp) // 블러 강도 유지
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xE5145B8C), // 더 밝은 파란색으로 변경
                            Color(0xC51E3A5F)  // 더 밝은 하단 색상
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(cornerRadius.dp)
                )
        )
        
        // 테두리와 컨텐츠 레이어 (블러 없음)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xEE90CAF9), // 상단은 더 밝은 테두리
                            Color(0x8064B5F6)  // 하단은 더 어두운 테두리
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(0f, Float.POSITIVE_INFINITY)
                    ),
                    shape = RoundedCornerShape(cornerRadius.dp)
                )
        ) {
            // 유리 효과를 위한 그라디언트 오버레이
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
                    .clip(RoundedCornerShape((cornerRadius - 1).dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0x503F51B5), // 더 밝은 상단 내부 색상
                                Color(0x352B5BDF)  // 더 밝은 하단 내부 색상
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, Float.POSITIVE_INFINITY)
                        )
                    )
            ) {
                // 유리 반사 효과 (상단에 밝은 부분)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.15f)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x608BB2F0), // 더 밝은 반투명 청색
                                    Color.Transparent   // 완전 투명
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
                
                // 왼쪽 상단 밝은 부분 (추가 반사 효과)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .fillMaxHeight(0.3f)
                        .padding(5.dp)
                        .align(Alignment.TopStart)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x60BBDEFB), // 더 밝고 더 뚜렷한 하이라이트
                                    Color.Transparent   // 완전 투명
                                )
                            )
                        )
                )
                
                // 오른쪽 하단 희미한 반짝임 (추가 반사 효과)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .fillMaxHeight(0.2f)
                        .padding(5.dp)
                        .align(Alignment.BottomEnd)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x15BBDEFB), // 매우 희미한 하이라이트
                                    Color.Transparent   // 완전 투명
                                )
                            )
                        )
                )
                
                // 내용 (블러 없음)
                content()
            }
        }
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