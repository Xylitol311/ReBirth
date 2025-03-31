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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
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
    
    // 캔버스 크기 상태
    var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }
    
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
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { canvasSize = it }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                // 스크롤 오프셋 (픽셀 단위로 변환)
                val pixelScrollOffset = scrollOffset / 1000f * canvasHeight
                
                // 복제할 세트의 수 (위 아래로 각각 하나씩)
                val repeatSets = 3
                
                // 3세트의 별을 그림 (현재 위치, 위, 아래)
                for (setIndex in -1 until repeatSets - 1) {
                    stars.forEach { star ->
                        // 기본 y 위치 계산
                        val baseY = star.y * canvasHeight
                        
                        // 세트별 오프셋 계산
                        val setOffset = setIndex * canvasHeight
                        
                        // 최종 y 위치 (세트 오프셋 + 기본 위치 - 스크롤 오프셋)
                        val finalY = baseY + setOffset - (pixelScrollOffset % canvasHeight)
                        
                        // 각 세트별 x 위치 계산 및 가로 방향 오프셋 적용
                        val xOffset = (animatedHorizontalOffset / (canvasWidth * 0.5f)) * canvasWidth
                        val xPos = (star.x * canvasWidth - xOffset) % canvasWidth
                        // 음수 처리
                        val adjustedX = if (xPos < 0) xPos + canvasWidth else xPos
                        
                        // 별이 화면 내에 있을 때만 그리기
                        if (finalY >= -100 && finalY <= canvasHeight + 100) {
                            drawStar(
                                center = Offset(adjustedX, finalY),
                                radius = star.size,
                                color = Color.White,
                                time = time,
                                star = star
                            )
                        }
                    }
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
    color: Color = Color(0x70FFFFFF),  // 더 밝게 변경 (0x30 -> 0x70)
    borderColor: Color = Color(0x90FFFFFF),  // 더 밝게 변경 (0x70 -> 0x90)
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
                .blur(radius = 8.dp)  // 블러 강도 약간 감소 (10.dp -> 8.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x70145B8C),  // 더 밝게 변경 (0x50 -> 0x70)
                            Color(0x601E3A5F)   // 더 밝게 변경 (0x40 -> 0x60)
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
                    width = 0.5.dp,  // 테두리 그대로 유지
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x8590CAF9),  // 더 밝게 변경 (0x55 -> 0x85)
                            Color(0x7064B5F6)   // 더 밝게 변경 (0x30 -> 0x70)
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
                                Color(0x503F51B5),  // 더 밝게 변경 (0x20 -> 0x50)
                                Color(0x402B5BDF)   // 더 밝게 변경 (0x15 -> 0x40)
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
                                    Color(0x608BB2F0),  // 더 밝게 변경 (0x30 -> 0x60)
                                    Color.Transparent    // 완전 투명
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
                                    Color(0x50BBDEFB),  // 더 밝게 변경 (0x25 -> 0x50)
                                    Color.Transparent    // 완전 투명
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
                                    Color(0x30BBDEFB),  // 더 밝게 변경 (0x10 -> 0x30)
                                    Color.Transparent    // 완전 투명
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