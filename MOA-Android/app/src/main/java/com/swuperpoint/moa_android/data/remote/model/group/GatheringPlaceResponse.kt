package com.swuperpoint.moa_android.data.remote.model.group

/* 중간지점 찾기 화면 데이터 Response 모델 */
data class GatheringPlaceResponse(
    val memberList: ArrayList<MemberResponse>, // 그룹원 리스트
    val memberNum: Int, // 총 그룹원 인원수
    val userPlaceName: String?, // 기존에 사용자가 입력했던 장소 이름
    val startPlace: PlaceLocationResponse?, // 사용자의 출발 지점 좌표
    val placeList: ArrayList<RecommendPlaceResponse>? // 추천 중간 지점 리스트
)
