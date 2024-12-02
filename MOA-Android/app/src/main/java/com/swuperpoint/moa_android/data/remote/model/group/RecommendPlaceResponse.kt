package com.swuperpoint.moa_android.data.remote.model.group

/* 추천 중간 지점 리스트 응답 모델 */
data class RecommendPlaceResponse(
    val placeName: String, // 장소 이름
    val address: String, // 주소
    val subwayTime: String, // 지하철 소요 시간
    val latitude: String, // 위도
    val longitude: String // 경도
)
