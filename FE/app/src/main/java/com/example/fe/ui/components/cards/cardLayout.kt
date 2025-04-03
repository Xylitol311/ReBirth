package com.example.fe.ui.components.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage

/**
 * 가로형 카드 레이아웃 컴포넌트
 * 
 * @param cardName 카드 이름
 * @param cardImageUrl 카드 이미지 URL
 * @param cardImage 카드 이미지 리소스
 * @param modifier 기본 모디파이어
 */
@Composable
fun HorizontalCardLayout(
    cardName: String,
    cardImageUrl: String = "",
    cardImage: Int = R.drawable.card,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2D2A57),
                        Color(0xFF1A1A40)
                    )
                )
            )
    ) {
        // 카드 이미지 (URL 또는 리소스)
        if (cardImageUrl.isNotEmpty()) {
            // URL 이미지 로드
            AsyncImage(
                model = cardImageUrl,
                contentDescription = "카드 이미지",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = painterResource(id = cardImage) // 로드 실패 시 기본 이미지
            )
        } else {
            // 리소스 이미지 사용
            Image(
                painter = painterResource(id = cardImage),
                contentDescription = "카드 이미지",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // 카드 정보 오버레이
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 카드 이름
            Text(
                text = cardName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


/**
 * 세로형 카드 레이아웃 컴포넌트
 * 
 * @param cardImage 카드 이미지 리소스
 * @param modifier 기본 모디파이어
 * @param width 카드 너비 (null이면 fillMaxWidth 사용)
 * @param height 카드 높이
 * @param cornerRadius 모서리 둥글기
 * @param onClick 클릭 이벤트 핸들러
 */
@Composable
fun VerticalCardLayout(
    cardImage: Painter,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 200.dp,
    cornerRadius: Dp = 16.dp,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .let { 
                if (width != null) it.width(width) else it 
            }
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 세로형 카드 (이미지 회전)
        Image(
            painter = cardImage,
            contentDescription = "Card Image",
            modifier = Modifier
                .width(height) // 높이와 너비를 바꿔서 회전 후에도 비율 유지
                .height(width ?: height) // width가 null이면 height 사용
                .graphicsLayer {
                    rotationZ = 90f
                }
        )
    }
}

// 미리보기용 컴포저블
@Preview(showBackground = true)
@Composable
fun HorizontalCardLayoutPreview() {
    HorizontalCardLayout(
        cardName = "Card Name",
        cardImageUrl = "https://example.com/card-image.jpg"
    )
}

@Preview(showBackground = true)
@Composable
fun VerticalCardLayoutPreview() {
    VerticalCardLayout(
        cardImage = painterResource(id = R.drawable.card),
        width = 160.dp,
        height = 240.dp
    )
}