package com.swuperpoint.moa_android.data.remote.model.group

/* 모임 정보 조회 응답 모델 */
data class GatheringInfoResponse(
    val gatheringId: String, // 추가
    val gatheringName: String, // 모임 이름
    val date: String, // 모임 날짜
    val gatheringStartTime: String, // 모임 시작 시간
    val gatheringEndTime: String, // 모임 끝 시간
    val placeName: String?, // 모임 중간 지점 이름
    val subwayTime: String?, // 지하철 소요 시간
    val startPlace: PlaceLocationResponse?, // 사용자의 출발 지점 좌표
    val gatheringPlace: PlaceLocationResponse? // 모임 중간 지점 좌표
)

/* 장소 좌표 */
data class PlaceLocationResponse(
    val latitude: String, // 위도
    val longitude: String // 경도
)