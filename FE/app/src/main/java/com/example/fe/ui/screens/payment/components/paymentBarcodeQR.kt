package com.example.fe.ui.screens.payment.components

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.example.fe.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.absoluteValue

@Composable
fun PaymentBarcodeQRSection(
    remainingTime: Int,
    refreshTrigger: Int,
    onRefresh: () -> Unit,
    paymentToken: String? = null,
    modifier: Modifier = Modifier
) {
    var isQRMode by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // 서버에서 받은 토큰 또는 가짜 토큰 사용
    val fullToken = remember(refreshTrigger, paymentToken) {
        paymentToken ?: "MOCK_TOKEN_${System.currentTimeMillis()}"
    }
    
    val barcodeToken = remember(fullToken) {
        fullToken.take(20)
    }
    
    val qrToken = remember(fullToken) {
        fullToken
    }
    
    // 새로고침 효과
    LaunchedEffect(refreshTrigger) {
        isRefreshing = true
        delay(300)
        isRefreshing = false
    }
    
    Box(
        modifier = modifier
    ) {
        // 바코드 영역 (왼쪽)
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .width(250.dp)
                .align(Alignment.TopStart)
                .padding(top = 8.dp)
                .graphicsLayer(
                    alpha = if (isRefreshing) 0.3f else 1f
                )
        ) {
            // 바코드 - 토큰 기반 데이터 사용
            BarcodeView(
                barcodeData = barcodeToken,
                refreshTrigger = refreshTrigger,
                barcodeFormat = BarcodeFormat.CODE_128,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            
            // 바코드 번호 (사용자에게는 원래 카드 번호만 표시)
            Text(
                text = barcodeToken.take(20),
                fontSize = 14.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
            Log.d("PaymentBarcodeQRSection", "barcodeToken: $barcodeToken")
            Spacer(modifier = Modifier.height(32.dp))
            
            // 타이머 및 새로고침 버튼 (왼쪽 하단)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 새로고침 버튼
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2D2A57))
                        .clickable { onRefresh() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh),
                        contentDescription = "새로고침",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // 타이머
                Text(
                    text = "$remainingTime",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
        
        // QR 코드
        Box(
            modifier = Modifier
                .size(130.dp)
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .graphicsLayer(
                    alpha = if (isRefreshing) 0.3f else 1f
                )
        ) {
            QRCodeView(
                qrData = qrToken,
                refreshTrigger = refreshTrigger,
                errorCorrectionLevel = ErrorCorrectionLevel.M,
                margin = 0,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun BarcodeView(
    barcodeData: String,
    refreshTrigger: Int,
    barcodeFormat: BarcodeFormat = BarcodeFormat.CODE_128,
    modifier: Modifier = Modifier
) {
    var barcodeImage by remember { mutableStateOf<Bitmap?>(null) }
    
    // 갱신 트리거에 현재 시간을 추가하여 항상 새로운 바코드 생성
    val uniqueKey = remember(barcodeData, refreshTrigger, barcodeFormat) {
        "$barcodeData-$refreshTrigger-$barcodeFormat-${System.currentTimeMillis()}"
    }
    
    // 바코드 생성 (고유 키가 변경될 때마다)
    LaunchedEffect(uniqueKey) {
        withContext(Dispatchers.IO) {
            val multiFormatWriter = MultiFormatWriter()
            
            try {
                // 바코드 생성 힌트 - 여백 조정
                val hints = mapOf(
                    EncodeHintType.MARGIN to 10 // 여백 줄임
                )
                
                // CODE_128 형식으로 바코드 생성
                val bitMatrix = multiFormatWriter.encode(
                    barcodeData,
                    BarcodeFormat.CODE_128,
                    300, // 너비 조정
                    100, // 높이 조정
                    hints
                )
                
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bitmap = createBitmap(width, height)
                
                // 비트맵에 바코드 데이터 채우기
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap[x, y] = if (bitMatrix[x, y]) Color.Black.toArgb() else Color.White.toArgb()
                    }
                }
                
                barcodeImage = bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (barcodeImage != null) {
            // 바코드 이미지 컨테이너
            Box(
                modifier = Modifier
                    .width(300.dp) // 너비 조정
                    .height(100.dp) // 높이 조정
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // 바코드 이미지
                Image(
                    bitmap = barcodeImage!!.asImageBitmap(),
                    contentDescription = "Barcode",
                    modifier = Modifier
                        .width(280.dp) // 너비 조정
                        .height(80.dp), // 높이 조정
                    contentScale = ContentScale.FillWidth
                )
            }

        } else {
            Box(
                modifier = Modifier
                    .width(300.dp) // 너비 조정
                    .height(100.dp) // 높이 조정
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White) // 단순 흰색 배경
            )
        }
    }
}


@Composable
fun QRCodeView(
    qrData: String,
    refreshTrigger: Int,
    errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
    margin: Int = 0,
    modifier: Modifier = Modifier
) {
    var qrCodeImage by remember { mutableStateOf<Bitmap?>(null) }
    
    // 갱신 트리거에 현재 시간을 추가하여 항상 새로운 QR 코드 생성
    val uniqueKey = remember(qrData, refreshTrigger) {
        "$qrData-$refreshTrigger-${System.currentTimeMillis()}"
    }
    
    // QR 코드 생성 (고유 키가 변경될 때마다)
    LaunchedEffect(uniqueKey) {
        withContext(Dispatchers.IO) {
            try {
                val qrCodeWriter = QRCodeWriter()
                val hints = mapOf(
                    EncodeHintType.ERROR_CORRECTION to errorCorrectionLevel,
                    EncodeHintType.MARGIN to margin
                )
                
                val bitMatrix = qrCodeWriter.encode(
                    qrData,
                    BarcodeFormat.QR_CODE,
                    120,
                    120,
                    hints
                )
                
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bitmap = createBitmap(width, height)
                
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap[x, y] =
                            if (bitMatrix[x, y]) Color.White.toArgb() else Color.Transparent.toArgb()
                    }
                }
                
                qrCodeImage = bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (qrCodeImage != null) {
            Image(
                bitmap = qrCodeImage!!.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            // 로딩 중 표시 (배경 없이)
            Box(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// 실제 토큰과 동일한 형식의 모의 토큰 생성 함수

// 실제 토큰과 유사한 Base64 문자열 생성 함수
private fun generateBase64String(random: Random, length: Int): String {
    // 실제 토큰에서 사용된 문자셋 분석
    val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
    
    return buildString {
        repeat(length) {
            append(charset[random.nextInt(charset.length)])
        }
    }
}