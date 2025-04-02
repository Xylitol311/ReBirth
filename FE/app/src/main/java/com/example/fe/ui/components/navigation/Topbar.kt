package com.example.fe.ui.components.navigation

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R

// starry 배경 색상 정의
private val StarryBackgroundColor = Color(0xFF0A0A1A)

@Composable
fun TopBar(
    title: String = "RE",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .background(StarryBackgroundColor)
            .fillMaxWidth()
    ) {
        // 상태바 영역 (starry 배경색과 동일하게)
        Spacer(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .background(StarryBackgroundColor)
        )
        
        // 실제 탑바 컨텐츠
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(StarryBackgroundColor)
                .padding(top = 8.dp) // 모든 컨텐츠를 아래로 내림
        ) {
            // 중앙에 RE 로고 배치
            Text(
                text = title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
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
                // 뒤로가기 버튼 (showBackButton이 true일 때만 표시)
                if (showBackButton) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { onBackClick() }
                    )
                } else {
                    // 뒤로가기 버튼 없을 때 동일한 공간 유지
                    Spacer(modifier = Modifier.width(26.dp))
                }

                // 중앙 영역
                Spacer(modifier = Modifier.weight(1f))

                // 로그아웃 버튼
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "로그아웃",
                    tint = Color.White,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onLogoutClick() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 프로필 아이콘
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "프로필",
                    tint = Color.White,
                    modifier = Modifier
                        .size(26.dp)
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