package com.example.fe.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.ui.components.cards.HorizontalCardLayout
import com.example.fe.ui.components.backgrounds.GlassSurface

@Composable
fun HomeTransaction(
    modifier: Modifier = Modifier
) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        cornerRadius = 16f
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // 혜택을 놓친 거래 내역
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 카드 이미지와 이모지
                Box(
                    modifier = Modifier
                        .size(width = 200.dp, height = 120.dp)
                        .padding(vertical = 8.dp)
                ) {
                    // 카드 이미지 - HorizontalCardLayout 사용
                    HorizontalCardLayout(
                        cardImage = painterResource(id = R.drawable.card),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    
                    // 슬픈 이모지
                    Image(
                        painter = painterResource(id = R.drawable.sad_emoji),
                        contentDescription = "Sad Emoji",
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
                
                // 혜택을 놓쳤어요 텍스트
                Text(
                    text = "혜택을 놓쳤어요",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF4444),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // 가게 정보 및 결제 금액
                Text(
                    text = "GS25지에스역삼점",
                    fontSize = 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "4,200원 결제",
                    fontSize = 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = Color(0x80FFFFFF)  // 반투명 흰색 구분선
            )
            
            // 다른 카드 사용 시 혜택 정보
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 카드 이미지 - HorizontalCardLayout 사용
                Box(
                    modifier = Modifier
                        .size(width = 200.dp, height = 100.dp)
                        .padding(vertical = 8.dp)
                ) {
                    HorizontalCardLayout(
                        cardImage = painterResource(id = R.drawable.card),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }
                
                // 다른 카드 사용 시 혜택 정보
                Text(
                    text = "삼성 IDONE 카드를 사용했다면",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "420원",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "의 혜택을 볼 수도 있었어요",
                        fontSize = 22.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

