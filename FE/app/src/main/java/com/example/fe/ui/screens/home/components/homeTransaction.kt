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

@Composable
fun HomeTransaction() {
    // 거래 내역 및 혜택 카드
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
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
                        cardName = "GS25 편의점 추천 카드",
                        cardImageUrl = "",
                        cardImage = R.drawable.card,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    )
                    
                    // 슬픈 이모지
                    Image(
                        painter = painterResource(id = R.drawable.sad_emoji),
                        contentDescription = "Sad Emoji",
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
                
                // 혜택을 놓쳤어요 텍스트
                Text(
                    text = "혜택을 놓쳤어요",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // 가게 정보 및 결제 금액
                Text(
                    text = "GS25지에스역삼점",
                    fontSize = 18.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "4,200원 결제",
                    fontSize = 18.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                color = Color(0xFFE0E0E0)
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
                        cardName = "삼성 IDONE 카드",
                        cardImageUrl = "",
                        cardImage = R.drawable.card,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    )
                }
                
                // 다른 카드 사용 시 혜택 정보
                Text(
                    text = "삼성 IDONE 카드를 사용했다면",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Text(
                        text = "의 혜택을 볼 수도 있었어요",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

