package com.swuperpoint.moa_android.data.remote.model.timeline

/* 타임라인 화면의 데이터 응답 모델 */
data class TimelineResponse(
    val date: String, // 날짜
    val placeName: String, // 중간 지점 장소 이름
    val groupName: String, // 그룹 이름
    val gatheringName: String, // 모임 이름
    val groupMemberNum: Int, // 그룹 인원 수
    val gatheringImgURL: String // 모임 대표 사진
)
