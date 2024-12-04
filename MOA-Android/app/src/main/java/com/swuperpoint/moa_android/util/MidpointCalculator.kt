package com.swuperpoint.moa_android.util

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.swuperpoint.moa_android.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.*

data class Coordinate(
    val lat: String,
    val lon: String
)

data class Station(
    val id: String,
    val name: String,
    val line: String = "",
    val coordinates: Coordinate,
    val distance: Double,
    val transferLines: List<String>
)

data class TransitPath(
    val totalTime: Int,
    val transitCount: Int,
    val walkTime: Int,
    val payment: Int,
    val pathDetails: List<PathDetail>
)

data class PathDetail(
    val type: Int,
    val distance: Int,
    val sectionTime: Int,
    val stationCount: Int?,
    val startName: String,
    val endName: String,
    val lineName: String
)

data class StationEvaluation(
    val station: String,
    val line: String,
    val coordinates: Coordinate,
    val maxTime: Int,
    val minTime: Int,
    val avgTime: Double,
    val deviation: Int,
    val distance: Double,
    val transferScore: Int,
    val transitPaths: List<TransitPath>
)

class MidpointCalculator(private val context: Context) {
    private val odsayApiKey = context.getString(R.string.odsay_api_key)

    // 핫플레이스 역 목록
    private val hotplaceStations = setOf(
        "강남", "혜화", "잠실", "홍대입구", "합정", "상수", "안암", "신촌", "광화문", "여의나루",
        "뚝섬유원지", "서울숲", "건대입구", "이태원", "신사", "성수", "망원", "연남", "이대", "종로3가",
        "서울대입구", "고속터미널", "압구정", "역삼", "노량진", "공덕", "왕십리", "대학로", "사당", "천호",
        "문래", "강변", "신림", "선릉", "구로디지털단지", "회기", "석계", "청량리", "응암", "마포구청"
    )


    suspend fun findBestStation(coords: List<Coordinate>): List<StationEvaluation> = withContext(Dispatchers.IO) {
        try {
            Log.d("MidpointCalculator", "Input coordinates: $coords")
            Log.d("MidpointCalculator", "Starting findBestStation with ${coords.size} coordinates")

            val centerPoint = calculateCenterPoint(coords)
            var stations = emptyList<Station>()

            for (radius in listOf(1000, 2000, 3000, 5000)) {
                stations = getNearbyStations(centerPoint, radius)
                if (stations.isNotEmpty()) break
            }

            if (stations.isEmpty()) {
                throw Exception("주변에서 지하철역을 찾을 수 없습니다.")
            }

            val evaluations = stations.mapNotNull { station ->
                evaluateStation(station, coords)
            }.filter {
                it.maxTime <= 60
            }.sortedBy {
                it.avgTime - it.transferScore
            }.take(5)

            if (evaluations.isEmpty()) {
                throw Exception("평가 가능한 역을 찾을 수 없습니다.")
            }

            evaluations  // 상위 5개 역을 그대로 반환
        } catch (e: Exception) {
            Log.e("MidpointCalculator", "Error in findBestStation", e)
            emptyList()
        }
    }

    // 핫플레이스 찾기 함수 추가
    suspend fun findHotplaceStation(coordinates: List<Coordinate>): List<StationEvaluation> {
        try {
            val centerPoint = calculateCenterPoint(coordinates)
            var stations = emptyList<Station>()

            // 반경을 늘려가며 역 검색
            for (radius in listOf(1000, 2000, 3000, 5000)) {
                // 주변 역을 가져올 때 핫플레이스만 필터링
                stations = getNearbyStations(centerPoint, radius).filter { station ->
                    hotplaceStations.any { hotplace ->
                        station.name.contains(hotplace)
                    }
                }
                if (stations.isNotEmpty()) break
            }

            if (stations.isEmpty()) {
                throw Exception("이 중간 지점에서 가장 가까운 핫플레이스를 찾지 못했어요.")
            }

            // 핫플레이스 역들 평가
            val evaluations = stations.mapNotNull { station ->
                evaluateStation(station, coordinates)
            }.filter {
                it.maxTime <= 60
            }.sortedBy {
                it.avgTime - it.transferScore
            }

            if (evaluations.isEmpty()) {
                throw Exception("이 중간 지점에서 가장 가까운 핫플레이스를 찾지 못했어요.")
            }

            return evaluations
        } catch (e: Exception) {
            Log.e("MidpointCalculator", "핫플레이스 역 찾기 오류 발생", e)
            throw e
        }
    }

    private suspend fun getTransitInfo(origin: Coordinate, dest: Coordinate): TransitPath? = withContext(Dispatchers.IO) {
        try {
            // API 요청 전 짧은 딜레이 추가
            delay(1000)

            val urlString = buildString {
                append("https://api.odsay.com/v1/api/searchPubTransPath?")
                append("apiKey=${URLEncoder.encode(odsayApiKey, "UTF-8")}")
                append("&SX=${origin.lon}")
                append("&SY=${origin.lat}")
                append("&EX=${dest.lon}")
                append("&EY=${dest.lat}")
            }

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d("MidpointCalculator", "Raw API Response: $response")

            val gson = Gson()
            val odsayResponse = gson.fromJson(response, ODsayPathResponse::class.java)

            // 에러 응답 체크 추가
            if (response.contains("\"error\"")) {
                Log.e("MidpointCalculator", "API Error Response: $response")
                return@withContext null
            }

            val path = odsayResponse.result?.path?.firstOrNull() ?: return@withContext null

            TransitPath(
                totalTime = path.info.totalTime,
                transitCount = path.info.busTransitCount + path.info.subwayTransitCount,
                walkTime = path.info.totalWalk,
                payment = path.info.payment,
                pathDetails = path.subPath.mapNotNull { subPath ->  // map 대신 mapNotNull 사용
                    try {
                        PathDetail(
                            type = subPath.trafficType,
                            distance = subPath.distance,
                            sectionTime = subPath.sectionTime,
                            stationCount = subPath.stationCount,
                            startName = subPath.startName ?: "Unknown",
                            endName = subPath.endName ?: "Unknown",
                            lineName = subPath.lane?.firstOrNull()?.name ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("MidpointCalculator", "경로 상세 정보 생성 중 오류 발생", e)
                        null
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("MidpointCalculator", "대중교통 정보 조회 중 오류 발생", e)
            null
        }
    }

    private fun calculateCenterPoint(coords: List<Coordinate>): Coordinate {
        var x = 0.0
        var y = 0.0
        var z = 0.0

        coords.forEach { coord ->
            val lat = toRadians(coord.lat.toDouble())
            val lon = toRadians(coord.lon.toDouble())

            x += cos(lat) * cos(lon)
            y += cos(lat) * sin(lon)
            z += sin(lat)
        }

        x /= coords.size
        y /= coords.size
        z /= coords.size

        val centerLon = toDegrees(atan2(y, x))
        val hyp = sqrt(x * x + y * y)
        val centerLat = toDegrees(atan2(z, hyp))

        return Coordinate(centerLat.toString(), centerLon.toString())
    }

    private fun toRadians(degrees: Double): Double = degrees * PI / 180
    private fun toDegrees(radians: Double): Double = radians * 180 / PI

    private suspend fun evaluateStation(station: Station, coords: List<Coordinate>): StationEvaluation? {
        val pathAnalyses = coords.mapNotNull { coord ->
            getTransitInfo(coord, station.coordinates)
        }

        if (pathAnalyses.size != coords.size) return null

        val times = pathAnalyses.map { it.totalTime }
        val transferScore = station.transferLines.size * 2

        return StationEvaluation(
            station = station.name,
            line = station.line,
            coordinates = station.coordinates,
            maxTime = times.maxOrNull() ?: 0,
            minTime = times.minOrNull() ?: 0,
            avgTime = times.average(),
            deviation = (times.maxOrNull() ?: 0) - (times.minOrNull() ?: 0),
            distance = station.distance,
            transferScore = transferScore,
            transitPaths = pathAnalyses
        )
    }

    private suspend fun getNearbyStations(center: Coordinate, radius: Int): List<Station> = withContext(Dispatchers.IO) {
        try {
            val urlString = buildString {
                append("https://api.odsay.com/v1/api/pointSearch?")
                append("apiKey=${URLEncoder.encode(odsayApiKey, "UTF-8")}")
                append("&x=${center.lon}")
                append("&y=${center.lat}")
                append("&radius=$radius")
            }

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d("MidpointCalculator", "Station Search Response: $response")

            val gson = Gson()
            val odsayResponse = gson.fromJson(response, ODsayStationResponse::class.java)

            odsayResponse.result?.station
                ?.filter { station ->
                    !station.laneName.isNullOrEmpty() &&
                            (station.laneName.contains("수도권") || station.laneName.contains("호선"))
                }
                ?.map { station ->
                    Station(
                        id = station.stationID,
                        name = station.stationName,
                        line = station.laneName ?: "",
                        coordinates = Coordinate(
                            lat = station.y.toString(),
                            lon = station.x.toString()
                        ),
                        distance = station.distance,
                        transferLines = station.laneName?.split(",")?.map { it.trim() } ?: emptyList()
                    )
                } ?: emptyList()
        } catch (e: Exception) {
            Log.e("MidpointCalculator", "Error getting nearby stations", e)
            emptyList()
        }
    }
}

// API 응답 데이터 클래스들
interface KakaoMapService {
    @GET("v2/local/search/address.json")
    suspend fun getCoordinates(
        @Header("Authorization") auth: String,
        @Query("query") address: String
    ): KakaoAddressResponse
}

interface ODsayService {
    @GET("v1/api/searchPubTransPath")
    suspend fun getTransitPath(
        @Query("apiKey") apiKey: String,
        @Query("SX") startX: Double,
        @Query("SY") startY: Double,
        @Query("EX") endX: Double,
        @Query("EY") endY: Double
    ): ODsayPathResponse

    @GET("v1/api/pointSearch")
    suspend fun searchStations(
        @Query("apiKey") apiKey: String,
        @Query("lang") lang: String = "0",
        @Query("output") output: String = "json",
        @Query("x") x: Double,
        @Query("y") y: Double,
        @Query("radius") radius: Int
    ): ODsayStationResponse
}

data class KakaoAddressResponse(val documents: List<KakaoAddress>)
data class KakaoAddress(val x: String, val y: String)
data class ODsayPathResponse(val result: ODsayPathResult?)
data class ODsayStationResponse(val result: ODsayStationResult?)
data class ODsayPathResult(val path: List<ODsayPath>)
data class ODsayStationResult(val station: List<ODsayStation>)
data class ODsayPath(val info: ODsayPathInfo, val subPath: List<ODsaySubPath>)

data class ODsayStation(
    val stationID: String = "",
    val stationName: String = "",
    val laneName: String? = null,
    val x: Double = 0.0,
    val y: Double = 0.0,
    val distance: Double = 0.0
)

data class ODsaySubPath(
    val trafficType: Int,
    val distance: Int,
    val sectionTime: Int,
    val stationCount: Int?,
    val startName: String,
    val endName: String,
    val lane: List<ODsayLane>?
)

data class ODsayLane(
    val name: String,
    val subwayCode: String? = null,
    val busNo: String? = null
)

data class ODsayPathInfo(
    val totalTime: Int,
    val busTransitCount: Int,
    val subwayTransitCount: Int,
    val totalWalk: Int,
    val payment: Int
)