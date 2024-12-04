package com.swuperpoint.moa_android.data.remote.model.timeline

import com.google.firebase.Timestamp

/* 타임라인 화면의 데이터 응답 모델 */
data class TimelineResponse(
    val timelineId: String, // 타임라인 id
    val date: String, // 날짜
    val placeName: String, // 중간 지점 장소 이름
    val groupId: String, // 그룹 id
    val groupName: String, // 그룹 이름
    val gatheringName: String, // 모임 이름
    val groupMemberNum: Int, // 그룹 인원 수
    val gatheringImgURL: String, // 모임 대표 사진
    val createdAt: Timestamp? = null, // 타임라인 생성 시간 추가
    val createdBy: String  // 생성자 추가
)