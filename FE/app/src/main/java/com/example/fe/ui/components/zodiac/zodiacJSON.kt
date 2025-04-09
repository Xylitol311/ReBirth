//package com.example.fe.ui.components.zodiac
//
//import android.content.Context
//import android.util.Log
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.ui.platform.LocalContext
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import java.io.File
//import java.io.IOException
//import kotlin.random.Random
//
//// 간소화된 JSON 직렬화 클래스들
//@Serializable
//data class ZodiacJSON(
//    val zodiacId: String,
//    val cardId: String,
//    val createdAt: Long,
//    val stars: List<StarJSON>,
//    val connections: List<ConnectionJSON>
//)
//
//@Serializable
//data class StarJSON(
//    val id: String,
//    val x: Float,
//    val y: Float,
//    val size: Float,
//    val brightness: Float,
//    val color: String = "#FFFFFF"
//)
//
//@Serializable
//data class ConnectionJSON(
//    val from: String,
//    val to: String,
//    val width: Float,
//    val color: String = "#FFFFFF",
//    val opacity: Float = 0.7f
//)
//
///**
// * 별자리 데이터를 JSON으로 변환 (간소화 버전)
// */
//fun constellationToJSON(
//    constellation: Constellation,
//    cardId: String
//): ZodiacJSON {
//    // 별 데이터 변환
//    val starsJSON = constellation.stars.mapIndexed { index, star ->
//        StarJSON(
//            id = "star_$index",
//            x = star.x,
//            y = star.y,
//            size = star.size * 7.0f,
//            brightness = if (index == constellation.centralStarIndex) 1.0f else 0.7f + (star.size - 0.5f) * 0.3f,
//            color = "#FFFFFF"
//        )
//    }
//
//    // 연결선 데이터 변환
//    val connectionsJSON = constellation.connections.map { (fromIdx, toIdx) ->
//        ConnectionJSON(
//            from = "star_$fromIdx",
//            to = "star_$toIdx",
//            width = 1.2f,
//            color = "#FFFFFF",
//            opacity = 0.7f
//        )
//    }
//
//    // 최종 JSON 객체 생성 (간소화)
//    return ZodiacJSON(
//        zodiacId = "${cardId}_zodiac",
//        cardId = cardId,
//        createdAt = System.currentTimeMillis(),
//        stars = starsJSON,
//        connections = connectionsJSON
//    )
//}
//
///**
// * JSON을 별자리 데이터로 변환
// */
//fun jsonToConstellation(zodiacJSON: ZodiacJSON): Constellation {
//    // 별 데이터 변환
//    val stars = zodiacJSON.stars.map { starJSON ->
//        Star(
//            x = starJSON.x,
//            y = starJSON.y,
//            size = starJSON.size / 7.0f // 크기 역변환
//        )
//    }
//
//    // 연결선 데이터 변환
//    val connections = zodiacJSON.connections.mapNotNull { connection ->
//        val fromId = connection.from.removePrefix("star_").toIntOrNull()
//        val toId = connection.to.removePrefix("star_").toIntOrNull()
//
//        if (fromId != null && toId != null) {
//            Pair(fromId, toId)
//        } else {
//            null
//        }
//    }
//
//    // 중심 별 인덱스 찾기 (가장 큰 별을 중심으로 간주)
//    val centralStarIndex = stars.indices.maxByOrNull { stars[it].size } ?: 0
//
//    return Constellation(
//        name = jsonName ?: "별자리",
//        stars = stars,
//        connections = jsonConnections,
//        centralStarIndex = jsonCentralStarIndex ?: 0
//    )
//}
//
///**
// * 별자리 JSON을 파일로 저장 (앱 전용 외부 저장소 사용)
// */
//suspend fun saveZodiacJSON(context: Context, zodiacJSON: ZodiacJSON): Boolean {
//    return withContext(Dispatchers.IO) {
//        try {
//            // JSON 문자열로 변환
//            val jsonString = Json {
//                prettyPrint = true
//                encodeDefaults = true
//            }.encodeToString(zodiacJSON)
//
//            // 앱 전용 외부 저장소 디렉토리 사용
//            val zodiacDir = File(context.getExternalFilesDir(null), "zodiac")
//            if (!zodiacDir.exists()) {
//                zodiacDir.mkdirs()
//            }
//
//            // 파일 저장 경로 설정
//            val fileName = "zodiac_${zodiacJSON.cardId}.json"
//            val file = File(zodiacDir, fileName)
//
//            // 파일에 JSON 저장
//            file.writeText(jsonString)
//
//            // 저장 경로 로그 출력 (디버깅용)
//            Log.d("ZodiacJSON", "Saved zodiac JSON for ${zodiacJSON.cardId} to ${file.absolutePath}")
//            true
//        } catch (e: IOException) {
//            Log.e("ZodiacJSON", "Error saving zodiac JSON", e)
//            false
//        }
//    }
//}
//
///**
// * 파일에서 별자리 JSON 불러오기 (외부 저장소 사용)
// */
//suspend fun loadZodiacJSON(context: Context, cardId: String): ZodiacJSON? {
//    return withContext(Dispatchers.IO) {
//        try {
//            val zodiacDir = File(context.getExternalFilesDir(null), "zodiac")
//            val fileName = "zodiac_${cardId}.json"
//            val file = File(zodiacDir, fileName)
//
//            if (!file.exists()) {
//                Log.d("ZodiacJSON", "Zodiac JSON file not found for $cardId")
//                return@withContext null
//            }
//
//            val jsonString = file.readText()
//            val zodiacJSON = Json { ignoreUnknownKeys = true }.decodeFromString<ZodiacJSON>(jsonString)
//
//            Log.d("ZodiacJSON", "Loaded zodiac JSON for $cardId from ${file.absolutePath}")
//            zodiacJSON
//        } catch (e: Exception) {
//            Log.e("ZodiacJSON", "Error loading zodiac JSON", e)
//            null
//        }
//    }
//}
//
///**
// * 별자리 생성 및 JSON 저장 Composable
// * 이 함수는 별자리를 생성하고 JSON으로 저장한 후 반환합니다.
// */
//@Composable
//fun rememberZodiacWithJSON(
//    cardId: String,
//    forceRegenerate: Boolean = false
//): Constellation {
//    val context = LocalContext.current
//    val seed = cardId.hashCode()
//    val random = Random(seed)
//
//    // 별자리 생성 또는 로드
//    val constellation = remember(cardId, forceRegenerate) {
//        generateConstellation(random)
//    }
//
////    // JSON 저장 효과
////    LaunchedEffect(constellation, cardId) {
////        val zodiacJSON = constellationToJSON(constellation, cardId)
////        saveZodiacJSON(context, zodiacJSON)
////    }
//
//    return constellation
//}
//
///**
// * 별자리 데이터 로드 Composable (백엔드 연동 준비)
// */
//@Composable
//fun rememberZodiacFromJSON(
//    cardId: String,
//    useBackend: Boolean = false // 백엔드 사용 여부 플래그
//): Constellation {
//    val context = LocalContext.current
//    val seed = cardId.hashCode()
//    val random = Random(seed)
//
//    // 별자리 상태
//    val constellationState = remember(cardId) {
//        mutableStateOf(generateConstellation(random))  // 기본값으로 새 별자리 생성
//    }
//
//    // 별자리 로드 효과
//    LaunchedEffect(cardId) {
//        var zodiacJSON: ZodiacJSON? = null
//
//        // 1. 백엔드에서 로드 시도 (useBackend가 true인 경우)
//        if (useBackend) {
//            zodiacJSON = downloadZodiacFromBackend(cardId)
//        }
//
//        // 2. 백엔드에서 로드 실패한 경우 로컬에서 로드 시도
//        if (zodiacJSON == null) {
//            zodiacJSON = loadZodiacJSON(context, cardId)
//        }
//
//        // 3. 로컬에서도 로드 실패한 경우 새로 생성
//        if (zodiacJSON == null) {
//            // 새 별자리 생성
//            val newConstellation = generateConstellation(random)
//            val newZodiacJSON = constellationToJSON(newConstellation, cardId)
//
//            // 백엔드 업로드 (useBackend가 true인 경우)
//            if (useBackend) {
//                uploadZodiacToBackend(newZodiacJSON)
//            }
//
//            // 상태 업데이트
//            constellationState.value = newConstellation
//        } else {
//            // 로드된 JSON으로 별자리 생성
//            constellationState.value = jsonToConstellation(zodiacJSON)
//        }
//    }
//
//    return constellationState.value
//}
//
///**
// * 백엔드 API와 연동하기 위한 함수
// */
//suspend fun uploadZodiacToBackend(zodiacJSON: ZodiacJSON): Boolean {
//    return withContext(Dispatchers.IO) {
//        try {
//            // TODO: 실제 백엔드 API 호출 구현
//            // 예시:
//            // val jsonString = Json.encodeToString(zodiacJSON)
//            // val response = apiService.uploadZodiac(jsonString)
//            // return@withContext response.isSuccessful
//
//            // 임시 구현 (성공 반환)
//            Log.d("ZodiacJSON", "별자리 JSON 업로드 (백엔드 연동 예정): ${zodiacJSON.cardId}")
//            true
//        } catch (e: Exception) {
//            Log.e("ZodiacJSON", "별자리 JSON 업로드 실패", e)
//            false
//        }
//    }
//}
//
//suspend fun downloadZodiacFromBackend(cardId: String): ZodiacJSON? {
//    return withContext(Dispatchers.IO) {
//        try {
//            // TODO: 실제 백엔드 API 호출 구현
//            // 예시:
//            // val response = apiService.getZodiac(cardId)
//            // if (response.isSuccessful) {
//            //     return@withContext Json.decodeFromString<ZodiacJSON>(response.body())
//            // }
//
//            // 임시 구현 (null 반환)
//            Log.d("ZodiacJSON", "별자리 JSON 다운로드 시도 (백엔드 연동 예정): $cardId")
//            null
//        } catch (e: Exception) {
//            Log.e("ZodiacJSON", "별자리 JSON 다운로드 실패", e)
//            null
//        }
//    }
//}
//
///**
// * 별자리 JSON 문자열 생성 (디버깅용)
// */
//fun generateZodiacJSONString(cardId: String): String {
//    val random = Random(cardId.hashCode())
//    val constellation = generateConstellation(random)
//    val zodiacJSON = constellationToJSON(constellation, cardId)
//
//    return Json {
//        prettyPrint = true
//        encodeDefaults = true
//    }.encodeToString(zodiacJSON)
//}
//
///**
// * 별자리 JSON 파일 경로 반환 (디버깅용)
// */
//fun getZodiacJSONFilePath(context: Context, cardId: String): String {
//    val zodiacDir = File(context.getExternalFilesDir(null), "zodiac")
//    val fileName = "zodiac_${cardId}.json"
//    val file = File(zodiacDir, fileName)
//    return file.absolutePath
//}
//
///**
// * 모든 별자리 JSON 파일 경로 목록 반환 (디버깅용)
// */
//fun getAllZodiacJSONFilePaths(context: Context): List<String> {
//    val zodiacDir = File(context.getExternalFilesDir(null), "zodiac")
//    if (!zodiacDir.exists()) {
//        return emptyList()
//    }
//
//    return zodiacDir.listFiles()
//        ?.filter { it.name.startsWith("zodiac_") && it.name.endsWith(".json") }
//        ?.map { it.absolutePath }
//        ?: emptyList()
//}
