package com.example.fe.ui.components.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.fe.R
import kotlin.random.Random

@Composable
fun TopBar(
    title: String = "",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    // 별 데이터 생성
    val stars = remember {
        List(25) {  // 별 개수 증가
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 1.5f + 0.5f,
                alpha = Random.nextFloat() * 0.4f + 0.1f
            )
        }
    }
    
    // 시간 값 (별 깜빡임에 사용)
    var time by remember { mutableStateOf(0f) }
    
    // 배경 색상
    val backgroundColor = Color(0xFF0A0A1A)  // 완전 불투명 어두운 파란색
    
    // 상태바 높이 계산 
    val statusBarHeightPx = with(LocalDensity.current) {
        24.dp.toPx()  // 일반적인 상태바 높이 (추정값)
    }
    
    // 전체 TopBar 높이
    val topBarHeight = 80.dp
    
    // 투명한 상태바 + 탑바 배치
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(topBarHeight)  // 고정 높이 설정
            .zIndex(1000f) // 매우 높은 z-index로 별 배경 위에 표시
    ) {
        // 별 배경 (상태바 영역 포함)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarHeight)
                .background(backgroundColor)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            stars.forEach { star ->
                drawStar(
                    center = Offset(star.x * canvasWidth, star.y * canvasHeight),
                    radius = star.size,
                    color = Color.White.copy(alpha = star.alpha),
                    time = time
                )
            }
        }
        
        // 탑바 내용 (상태바 패딩 적용)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()  // 여기에 상태바 패딩 적용
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 뒤로가기 버튼 (showBackButton이 true일 때만 표시)
            if (showBackButton) {
                // 동그란 배경으로 감싸기
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF000000)) // 완전 불투명한 배경
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                // 뒤로가기 버튼 없을 때 동일한 공간 유지
                Spacer(modifier = Modifier.width(40.dp))
            }

            // 중앙에 제목 (필요한 경우에만)
            if (title.isNotEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            // 남은 공간을 채워서 아이콘들이 오른쪽으로 이동
            Spacer(modifier = Modifier.weight(1f))

            // 로그아웃 버튼
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF000000)) // 완전 불투명한 배경
                    .clickable { onLogoutClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "로그아웃",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 프로필 아이콘
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF000000)) // 완전 불투명한 배경
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "프로필",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// 별 데이터 클래스
private data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val alpha: Float
)

// 별 그리기 함수
private fun DrawScope.drawStar(
    center: Offset,
    radius: Float,
    color: Color,
    time: Float
) {
    // 기본 별
    drawCircle(
        color = color,
        radius = radius,
        center = center
    )
    
    // 별빛 효과
    drawCircle(
        color = color.copy(alpha = color.alpha * 0.3f),
        radius = radius * 2.0f,
        center = center
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A1A)
@Composable
fun TopBarPreview() {
    TopBar()
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A1A)
@Composable
fun TopBarWithBackButtonPreview() {
    TopBar(
        title = "이번 달 사용 내역",
        showBackButton = true
    )
}