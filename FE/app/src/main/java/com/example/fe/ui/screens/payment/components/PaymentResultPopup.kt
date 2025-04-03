package com.example.fe.ui.screens.payment.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fe.data.model.payment.PaymentResult
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentResultPopup(
    paymentResult: PaymentResult,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF2D2A57)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 제목
                Text(
                    text = "결제 완료",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 결제 금액
                Text(
                    text = "결제 금액",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                
                Text(
                    text = "${Math.abs(paymentResult.amount).toString().replace(Regex("\\B(?=(\\d{3})+(?!\\d))"), ",")}원",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 결제 일시
                Text(
                    text = "결제 일시",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                
                Text(
                    text = formatDateTime(paymentResult.createdAt),
                    color = Color.White,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 승인 번호
                Text(
                    text = "승인 번호",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                
                Text(
                    text = paymentResult.approvalCode,
                    color = Color.White,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 확인 버튼
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2D2A57)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "확인",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 날짜 형식 변환 함수
private fun formatDateTime(dateTimeStr: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateTimeStr)
        return outputFormat.format(date)
    } catch (e: Exception) {
        return dateTimeStr
    }
} 