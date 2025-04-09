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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.data.model.PreBenefitFeedbackData
import com.example.fe.ui.components.cards.HorizontalCardLayout
import com.example.fe.ui.components.backgrounds.GlassSurface
import com.example.fe.ui.screens.home.HomeViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeTransaction(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel
) {
    val preBenefitFeedback by viewModel.preBenefitFeedback.collectAsState()
    
    // API 응답이 아직 없는 경우 기본 UI 표시
    if (preBenefitFeedback == null) {
        GlassSurface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 8.dp),
            cornerRadius = 16f
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "최근 거래 내역이 없습니다",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 8.dp),
        cornerRadius = 16f
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            if (preBenefitFeedback?.isGood == true) {
                // 추천 카드로 결제한 경우 - 좋은 피드백
                GoodTransactionFeedback(data = preBenefitFeedback!!)
            } else {
                // 추천 카드로 결제하지 않은 경우 - 아쉬운 피드백
                BadTransactionFeedback(data = preBenefitFeedback!!)
            }
        }
    }
}

@Composable
private fun GoodTransactionFeedback(
    data: PreBenefitFeedbackData
) {
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
            // 카드 이미지
            HorizontalCardLayout(
                cardName = "사용한 카드",
                cardImageUrl = "",
                cardImage = R.drawable.card,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
            )
            
            // 기쁜 이모지
            Image(
                painter = painterResource(id = R.drawable.happy_emoji),
                contentDescription = "Happy Emoji",
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.BottomCenter)
            )
        }
        
        // 혜택을 잘 받았어요 텍스트
        Text(
            text = "혜택을 잘 받았어요!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // 가게 정보 및 결제 금액
        Text(
            text = data.merchantName,
            fontSize = 22.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "${formatAmount(data.amount)}원 결제",
            fontSize = 22.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 혜택 정보
        Text(
            text = "${formatAmount(data.realBenefitAmount)}원의 혜택을 받았어요",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BadTransactionFeedback(
    data: PreBenefitFeedbackData
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
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
                // 실제 사용한 카드 이미지
                    HorizontalCardLayout(
                    cardName = "사용한 카드",
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
                text = data.merchantName,
                    fontSize = 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Text(
                text = "${formatAmount(data.amount)}원 결제",
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
            // 추천 카드 이미지
                Box(
                    modifier = Modifier
                        .size(width = 200.dp, height = 100.dp)
                        .padding(vertical = 8.dp)
                ) {
                    HorizontalCardLayout(
                    cardName = "추천 카드",
                        cardImageUrl = "",
                        cardImage = R.drawable.card,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    )
                }
                
                // 다른 카드 사용 시 혜택 정보
                Text(
                text = "추천 카드를 사용했다면",
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
                    text = "${formatAmount(data.ifBenefitAmount)}원",
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

// 금액 포맷팅 함수
private fun formatAmount(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount)
}

