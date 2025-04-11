
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
data class Star(
    val x: Float,
    val y: Float,
    val size: Float = 1f
) {
    // Offset으로 변환하는 함수
    fun toOffset(): Offset = Offset(x, y)
}

data class Constellation(
    val name: String = "",
    val stars: List<Star>,
    val connections: List<Pair<Int, Int>>,
    val centralStarIndex: Int = 0
)

@Composable
fun DynamicZodiacView(
    cardId: String,
    modifier: Modifier = Modifier,
    useJSON: Boolean = false,  // 기본값을 false로 변경
    useBackend: Boolean = false,
    scrollOffset: Float = 0f,
    horizontalOffset: Float = 0f
) {
    // JSON 사용 여부에 따라 별자리 생성 방식 선택
    val constellation = if (useJSON) {
        // JSON 사용 시 로직 (현재는 사용하지 않음)
        val random = Random(cardId.hashCode())
        remember(cardId) {
            generateConstellation(random)  // 임시로 동일한 함수 사용
        }
    } else {
        // 기존 방식으로 생성
        val random = Random(cardId.hashCode())
        remember(cardId) {
            generateConstellation(random)
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

        // 별자리 선 그리기
        constellation.connections.forEach { (startIdx, endIdx) ->
            val startStar = constellation.stars[startIdx]
            val endStar = constellation.stars[endIdx]

            val startX = centerX + startStar.x * canvasWidth * 0.8f
            val startY = centerY + startStar.y * canvasHeight * 0.8f
            val endX = centerX + endStar.x * canvasWidth * 0.8f
            val endY = centerY + endStar.y * canvasHeight * 0.8f

            // 선 그리기 - 그라데이션 효과
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD).copy(alpha = 0.95f),
                        Color(0xFFBBDEFB).copy(alpha = 0.75f)
                    ),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY)
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.5f * scale,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )

            // 선 주변 빛 효과
            drawLine(
                color = Color(0xFFE1F5FE).copy(alpha = 0.5f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 12f * scale,
                cap = StrokeCap.Round
            )
        }

        // 별 그리기
        constellation.stars.forEachIndexed { index, star ->
            val x = centerX + star.x * canvasWidth * 0.8f
            val y = centerY + star.y * canvasHeight * 0.8f
            val isCenterStar = index == constellation.centralStarIndex

            // 별 크기 및 밝기 계산 (중심 별은 더 크고 밝게)
            val starSize = if (isCenterStar) {
                star.size * 12f * scale
            } else {
                star.size * 7f * scale
            }

            val starAlpha = if (isCenterStar) {
                1.0f
            } else {
                0.9f + (star.size - 0.5f) * 0.6f
            }

            // 별 주변 빛 효과 - 그라데이션
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
                    val rayLength = starSize * 6f
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
                        strokeWidth = 2.5f * scale,
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
                    radius = starSize * 12f,
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

internal fun generateConstellation(random: Random): Constellation {
    val starCount = 6 + random.nextInt(3)  // 6~8개의 별 생성
    val stars = mutableListOf<Star>()
    val minDistanceBetweenStars = 0.15f  // 별 사이 최소 거리

    // 별자리 패턴 결정 (0: 방사형, 1: 선형, 2: 불규칙형, 3: 원형)
    val pattern = random.nextInt(4)

    // 중심 별 인덱스 (나중에 결정)
    var centerStarIndex = 0

    // 별 생성 함수 - 겹치지 않게 생성
    fun createNonOverlappingStar(x: Float, y: Float, size: Float): Star? {
        val newStar = Star(x, y, size)

        // 기존 별들과 충분히 떨어져 있는지 확인
        for (existingStar in stars) {
            val dist = distanceSquared(newStar, existingStar)
            if (dist < minDistanceBetweenStars * minDistanceBetweenStars) {
                return null  // 너무 가까우면 null 반환
            }
        }

        return newStar
    }

    when (pattern) {
        0 -> {
            // 방사형 패턴
            // 중심 별 생성
            stars.add(Star(
                x = (random.nextFloat() * 0.1f - 0.05f),
                y = (random.nextFloat() * 0.1f - 0.05f),
                size = 1.8f + random.nextFloat() * 0.4f
            ))

            // 나머지 별들을 방사형으로 배치 (겹치지 않게)
            var attempts = 0
            while (stars.size < starCount && attempts < 100) {
                val angle = 2 * Math.PI * random.nextFloat()
                val distance = 0.25f + random.nextFloat() * 0.25f
                val x = (cos(angle) * distance).toFloat()
                val y = (sin(angle) * distance).toFloat()
                val size = 1.2f - distance * 0.5f + random.nextFloat() * 0.3f

                val star = createNonOverlappingStar(x, y, size)
                if (star != null) {
                    stars.add(star)
                }
                attempts++
            }

            centerStarIndex = 0
        }
        1 -> {
            // 선형 패턴 (지그재그 또는 곡선)
            val isZigzag = random.nextBoolean()

            // 첫 번째 별 추가
            val firstX = -0.4f
            val firstY = if (isZigzag) 0.2f else 0f
            stars.add(Star(firstX, firstY, 1.0f + random.nextFloat() * 0.8f))

            // 나머지 별 추가
            for (i in 1 until starCount) {
                val progress = i.toFloat() / (starCount - 1)
                val x = progress * 0.8f - 0.4f

                // 지그재그 또는 곡선 형태로 y값 결정
                val y = if (isZigzag) {
                    (if (i % 2 == 0) 0.2f else -0.2f) + random.nextFloat() * 0.1f - 0.05f
                } else {
                    sin(progress * Math.PI * 2).toFloat() * 0.2f + random.nextFloat() * 0.1f - 0.05f
                }

                val size = 1.0f + random.nextFloat() * 0.8f
                val star = createNonOverlappingStar(x, y, size)
                if (star != null) {
                    stars.add(star)
                } else {
                    // 위치 조정 시도
                    var found = false
                    for (attempt in 1..5) {
                        val adjustedY = y + (random.nextFloat() * 0.2f - 0.1f)
                        val adjustedStar = createNonOverlappingStar(x, adjustedY, size)
                        if (adjustedStar != null) {
                            stars.add(adjustedStar)
                            found = true
                            break
                        }
                    }

                    // 그래도 실패하면 크기 줄여서 시도
                    if (!found) {
                        stars.add(Star(x, y, size * 0.7f))
                    }
                }
            }

            // 가장 큰 별을 중심 별로 설정
            centerStarIndex = stars.indices.maxByOrNull { stars[it].size } ?: 0
        }
        2 -> {
            // 불규칙 패턴 (겹치지 않게)
            while (stars.size < starCount) {
                val x = random.nextFloat() * 0.8f - 0.4f
                val y = random.nextFloat() * 0.8f - 0.4f
                val size = 1.0f + random.nextFloat() * 0.8f

                val star = createNonOverlappingStar(x, y, size)
                if (star != null) {
                    stars.add(star)
                }
            }

            // 가장 큰 별을 중심 별로 설정
            centerStarIndex = stars.indices.maxByOrNull { stars[it].size } ?: 0
        }
        3 -> {
            // 원형 패턴
            val isOval = random.nextBoolean()
            val verticalScale = if (isOval) 0.6f + random.nextFloat() * 0.4f else 1.0f

            // 중심 별 추가 (원의 중심)
            if (random.nextFloat() > 0.3f) {
                stars.add(Star(
                    x = 0f,
                    y = 0f,
                    size = 1.5f + random.nextFloat() * 0.5f
                ))
            }

            // 원 주변에 별 배치 (균등하게)
            val actualStarCount = if (stars.isEmpty()) starCount else starCount - 1
            for (i in 0 until actualStarCount) {
                val angle = 2 * Math.PI * i / actualStarCount
                val distance = 0.3f + random.nextFloat() * 0.1f
                val x = (cos(angle) * distance).toFloat()
                val y = (sin(angle) * distance * verticalScale).toFloat()
                val size = 1.0f + random.nextFloat() * 0.6f

                stars.add(Star(x, y, size))
            }

            // 중심 별이 있으면 인덱스 0, 없으면 가장 큰 별
            centerStarIndex = if (stars.size > actualStarCount) 0 else
                stars.indices.maxByOrNull { stars[it].size } ?: 0
        }
    }

    // 별자리 연결선 생성 - 모든 별이 최소 하나의 연결을 갖도록
    val connections = mutableListOf<Pair<Int, Int>>()

    // 최소 스패닝 트리 알고리즘으로 모든 별 연결
    val connected = mutableSetOf(centerStarIndex)
    val unconnected = stars.indices.toMutableSet()
    unconnected.remove(centerStarIndex)

    // 모든 별이 최소한 하나의 연결을 갖도록 함
    while (unconnected.isNotEmpty()) {
        var bestDist = Float.MAX_VALUE
        var bestPair = Pair(0, 0)

        for (i in connected) {
            for (j in unconnected) {
                val dist = distanceSquared(stars[i], stars[j])
                if (dist < bestDist) {
                    bestDist = dist
                    bestPair = Pair(i, j)
                }
            }
        }

        connections.add(bestPair)
        connected.add(bestPair.second)
        unconnected.remove(bestPair.second)
    }

    // 패턴에 따라 추가 연결선 생성 (선이 겹치지 않도록 확인)
    when (pattern) {
        0 -> {
            // 방사형 패턴은 이미 충분한 연결이 있음
        }
        1 -> {
            // 선형 패턴에 추가 연결 (가끔)
            if (stars.size > 4 && random.nextFloat() > 0.7f) {
                for (i in 0 until stars.size - 2) {
                    val newConnection = Pair(i, i + 2)
                    if (!doLinesIntersect(stars, connections, newConnection)) {
                        connections.add(newConnection)
                        break
                    }
                }
            }
        }
        2, 3 -> {
            // 불규칙/원형 패턴에 추가 연결 (약 20% 확률)
            val extraConnectionsCount = (stars.size * 0.2).toInt().coerceAtLeast(1)
            val possibleConnections = mutableListOf<Pair<Int, Int>>()

            // 가능한 모든 연결 쌍 생성
            for (i in stars.indices) {
                for (j in i + 1 until stars.size) {
                    val newConnection = Pair(i, j)
                    // 이미 존재하는 연결이 아니고, 선이 겹치지 않는지 확인
                    if (!connections.contains(newConnection) &&
                        !connections.contains(Pair(j, i)) &&
                        !doLinesIntersect(stars, connections, newConnection)) {
                        possibleConnections.add(newConnection)
                    }
                }
            }

            // 가능한 연결 중 무작위로 선택
            possibleConnections.shuffled(random)
                .take(extraConnectionsCount.coerceAtMost(possibleConnections.size))
                .forEach { connections.add(it) }
        }
    }

    return Constellation(
        name = "별자리",
        stars = stars,
        connections = connections,
        centralStarIndex = centerStarIndex
    )
}

// 두 선분이 교차하는지 확인하는 함수
private fun doLinesIntersect(
    stars: List<Star>,
    existingConnections: List<Pair<Int, Int>>,
    newConnection: Pair<Int, Int>
): Boolean {
    val p1 = stars[newConnection.first].toOffset()
    val p2 = stars[newConnection.second].toOffset()

    for (conn in existingConnections) {
        // 같은 별을 공유하는 선분은 교차하지 않음
        if (conn.first == newConnection.first || conn.first == newConnection.second ||
            conn.second == newConnection.first || conn.second == newConnection.second) {
            continue
        }

        val p3 = stars[conn.first].toOffset()
        val p4 = stars[conn.second].toOffset()

        // 선분 교차 확인
        if (doSegmentsIntersect(p1, p2, p3, p4)) {
            return true
        }
    }

    return false
}

// 두 선분의 교차 여부 계산
private fun doSegmentsIntersect(p1: Offset, p2: Offset, p3: Offset, p4: Offset): Boolean {
    fun orientation(p: Offset, q: Offset, r: Offset): Int {
        val val1 = (q.y - p.y) * (r.x - q.x)
        val val2 = (q.x - p.x) * (r.y - q.y)

        return when {
            val1 == val2 -> 0  // 일직선
            val1 > val2 -> 1   // 시계 방향
            else -> 2          // 반시계 방향
        }
    }

    fun onSegment(p: Offset, q: Offset, r: Offset): Boolean {
        return q.x <= maxOf(p.x, r.x) && q.x >= minOf(p.x, r.x) &&
                q.y <= maxOf(p.y, r.y) && q.y >= minOf(p.y, r.y)
    }

    val o1 = orientation(p1, p2, p3)
    val o2 = orientation(p1, p2, p4)
    val o3 = orientation(p3, p4, p1)
    val o4 = orientation(p3, p4, p2)

    // 일반적인 경우
    if (o1 != o2 && o3 != o4) return true

    // 특수 경우 (일직선 및 한 점이 다른 선분 위에 있는 경우)
    if (o1 == 0 && onSegment(p1, p3, p2)) return true
    if (o2 == 0 && onSegment(p1, p4, p2)) return true
    if (o3 == 0 && onSegment(p3, p1, p4)) return true
    if (o4 == 0 && onSegment(p3, p2, p4)) return true

    return false
}

// 두 별 사이의 거리 제곱 계산 (최적화를 위해 제곱근은 계산하지 않음)
private fun distanceSquared(a: Star, b: Star): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return dx * dx + dy * dy
}