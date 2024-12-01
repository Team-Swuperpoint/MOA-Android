package com.swuperpoint.moa_android.view.main.group.data

/* 사용자가 선택한 추천 장소 아이템*/
data class RecommendPlaceItem(
    val placeName: String, // 장소 이름
    val latitude: Double, // 위도
    val longitude: Double // 경도
)
