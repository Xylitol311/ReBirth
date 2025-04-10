package com.example.fe.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.fe.R

@Composable
fun TopBar(
    title: String = "",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    // 투명한 상태바 아래 탑바 배치
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding() // 상태바 아래로 패딩 추가
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .zIndex(1000f), // 매우 높은 z-index로 별 배경 위에 표시
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기 버튼 (showBackButton이 true일 때만 표시)
        if (showBackButton) {
            // 동그란 배경으로 감싸기
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xAA000000)) // 더 진한 반투명 배경
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
                fontSize = 26.sp,
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
                .background(Color(0xAA000000)) // 더 진한 반투명 배경
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
                .background(Color(0xAA000000)) // 더 진한 반투명 배경
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