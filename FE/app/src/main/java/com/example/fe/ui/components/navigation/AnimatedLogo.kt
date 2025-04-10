package com.example.fe.ui.components.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

@Composable
fun AnimatedLogo(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // 애니메이션 재생 여부를 저장하는 상태 (초기엔 true)
    var playAnimation by remember { mutableStateOf(true) }
    // rebirth.json 파일을 assets 폴더에 두었다고 가정
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("rebirth.json"))

    // animateLottieCompositionAsState를 사용해 progress 값을 받아옴 (1회 반복)
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = playAnimation,
        speed = 1.0f
    )

    // progress가 1f 이상이면 애니메이션이 끝난 것으로 판단하고 재생 플래그를 false로 변경
    LaunchedEffect(progress) {
        if (progress >= 1f && playAnimation) {
            playAnimation = false
        }
    }

    // 애니메이션이 재생 중이면 progress를, 아니면 마지막 상태(예를 들어 1f)를 사용
    Box(
        modifier = modifier.clickable { onClick() }
    ) {
        LottieAnimation(
            composition = composition,
            progress = { if (playAnimation) progress else 1f },
            modifier = Modifier
                .size(80.dp)         // 최종적으로 보일 사이즈
                .scale(3f)
                .padding(start = 16.dp)  // 2배 확대한 뒤
                .clip(RectangleShape)
        )
    }
}
