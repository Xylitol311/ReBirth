package com.example.fe.ui.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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

@Composable
fun TopBar(
    title: String = "RE",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(60.dp),
        color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 뒤로가기 버튼 (showBackButton이 true일 때만 표시)
                if (showBackButton) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onBackClick() }
                    )
                } else {
                    // 뒤로가기 버튼 없을 때 동일한 공간 유지
                    Spacer(modifier = Modifier.width(28.dp))
                }

                // 중앙 정렬을 위한 여백
                Spacer(modifier = Modifier.weight(1f))

                // 로그아웃 버튼
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "로그아웃",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onLogoutClick() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 프로필 아이콘
                Box(
                    modifier = Modifier.padding(top = 0.dp, bottom = 4.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Icon(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onProfileClick() },
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "프로필",
                        tint = Color.White
                    )
                }
            }
            
            // 중앙에 제목 배치
            Text(
                text = title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Center),
                textAlign = TextAlign.Center
            )
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