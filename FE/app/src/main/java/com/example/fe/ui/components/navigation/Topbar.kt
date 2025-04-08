package com.example.fe.ui.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R

// TopBar 크기를 상수로 정의
object TopBarDimensions {
    // TopBar 컨텐츠 영역의 높이
    val TOPBAR_HEIGHT = 60.dp
    
    // TopBar 내부 상단 패딩
    val TOPBAR_PADDING_TOP = 8.dp
    
    // TopBar의 총 높이 계산 (고정값은 아니고, 상태바 높이에 따라 달라짐)
    // 참고: 실제 사용 시에는 상태바 높이가 deviceDp로 계산되어야 함
    val TOPBAR_TOTAL_HEIGHT_APPROXIMATE = TOPBAR_HEIGHT + TOPBAR_PADDING_TOP
}

@Composable
fun TopBar(
    title: String = "",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 상태바 영역 (완전 투명)
        Spacer(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
        )
        
        // 실제 탑바 컨텐츠 (완전 투명)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TopBarDimensions.TOPBAR_HEIGHT)
                .padding(top = TopBarDimensions.TOPBAR_PADDING_TOP)
        ) {
            // 중앙에 로고 배치 - 그림자 효과 추가
            Text(
                text = title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.6f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.Center)
            )
            
            // 왼쪽 및 오른쪽 아이콘 배치
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 뒤로가기 버튼 (showBackButton이 true일 때만 표시) - 그림자 효과 추가
                if (showBackButton) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White,
                        modifier = Modifier
                            .size(26.dp)
                            .shadow(2.dp, ambientColor = Color.Black.copy(alpha = 0.5f))
                            .clickable { onBackClick() }
                    )
                } else {
                    // 뒤로가기 버튼 없을 때 동일한 공간 유지
                    Spacer(modifier = Modifier.width(26.dp))
                }

                // 중앙 영역
                Spacer(modifier = Modifier.weight(1f))

                // 로그아웃 버튼 - 그림자 효과 추가
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "로그아웃",
                    tint = Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .shadow(2.dp, ambientColor = Color.Black.copy(alpha = 0.5f))
                        .clickable { onLogoutClick() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 프로필 아이콘 - 그림자 효과 추가
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "프로필",
                    tint = Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .shadow(2.dp, ambientColor = Color.Black.copy(alpha = 0.5f))
                        .clickable { onProfileClick() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    TopBar()
}

@Preview(showBackground = true)
@Composable
fun TopBarWithBackButtonPreview() {
    TopBar(
        title = "이번 달 사용 내역",
        showBackButton = true
    )
}