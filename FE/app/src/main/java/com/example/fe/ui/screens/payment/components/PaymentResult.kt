package com.example.fe.ui.screens.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fe.R
import com.example.fe.data.model.payment.PaymentResult
import com.example.fe.ui.components.backgrounds.StarryBackground
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PaymentResultScreen(
    paymentResult: PaymentResult?,
    isSuccess: Boolean = true,
    onConfirm: () -> Unit
) {
    StarryBackground(
        scrollOffset = 0f,
        horizontalOffset = 0f,
        starCount = 100,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 결제 성공/실패 아이콘
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(60.dp))
                        .background(Color(0x33FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isSuccess) R.drawable.ic_check else R.drawable.ic_error
                        ),
                        contentDescription = if (isSuccess) "결제 완료" else "결제 실패",
                        tint = if (isSuccess) Color(0xFF00E0FF) else Color.Red,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 결제 상태 제목
                Text(
                    text = if (isSuccess) "결제 완료" else "결제 실패",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 결제 상태 메시지
                Text(
                    text = if (isSuccess) "결제가 정상적으로 처리 되었습니다" else "결제 처리 중 오류가 발생했습니다",
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 결제 정보 표시 (결제 성공 시에만)
                if (isSuccess && paymentResult != null) {
                    PaymentResultItem(
                        label = "결제 금액",
                        value = "${formatCurrency(paymentResult.amount)}원"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PaymentResultItem(
                        label = "승인 번호",
                        value = paymentResult.approvalCode ?: "승인번호 없음"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PaymentResultItem(
                        label = "결제 일시",
                        value = formatDateTime(paymentResult.createdAt)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 확인 버튼
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E0FF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    Text(
                        text = "확인",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentResultItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// 금액 포맷팅 함수
private fun formatCurrency(amount: Int): String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(amount)
}

// 날짜 포맷팅 함수
private fun formatDateTime(dateTimeString: String?): String {
    if (dateTimeString.isNullOrEmpty()) return "시간 정보 없음"

    try {
        // 입력 형식 (서버에서 오는 형식에 맞게 조정 필요)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
        // 출력 형식
        val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA)

        val date = inputFormat.parse(dateTimeString)
        return date?.let { outputFormat.format(it) } ?: "시간 정보 없음"
    } catch (e: Exception) {
        // 파싱 실패 시 원본 문자열 반환
        return dateTimeString
    }
}