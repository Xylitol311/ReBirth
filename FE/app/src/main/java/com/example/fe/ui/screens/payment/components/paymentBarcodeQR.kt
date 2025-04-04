package com.example.fe.ui.screens.payment.components

import android.graphics.Bitmap
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.withContext


@Composable
fun PaymentBarcodeQRSection(
    remainingTime: Int,
    refreshTrigger: Int,
    onRefresh: () -> Unit,
    paymentToken: String?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 바코드 표시
        if (paymentToken != null) {
            BarcodeView(
                barcodeData = paymentToken,
                refreshTrigger = refreshTrigger,
                barcodeFormat = BarcodeFormat.CODE_128,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // QR 코드와 새로고침 버튼을 나란히 배치
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // QR 코드 표시
                QRCodeView(
                    qrData = paymentToken,
                    refreshTrigger = refreshTrigger,
                    errorCorrectionLevel = ErrorCorrectionLevel.M,
                    margin = 0,
                    modifier = Modifier
                        .size(130.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 새로고침 버튼과 타이머를 세로로 배치
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
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
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 타이머 표시
                    Text(
                        text = "${remainingTime}초",
                        color = if (remainingTime <= 10) Color.Red else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // 토큰이 없는 경우 로딩 표시
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "토큰을 가져오는 중...",
                color = Color.White,
                fontSize = 16.sp
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
