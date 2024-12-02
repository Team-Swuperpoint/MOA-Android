package com.swuperpoint.moa_android.data.remote.model.timeline

/* 타임라인 정보 화면의 데이터 응답 모델 */
data class TimelineInfoResponse(
    val gatheringName: String, // 모임 이름
    val date: String, // 모임 날짜
    val placeName: String, // 모임 중간 지점 장소
    val groupName: String, // 그룹 이름
    val timelineList: ArrayList<TimelinePhotoResponse> // 타임라인 사진 리스트
)

/* 타임라인 정보 화면의 사진 리스트 응답 모델 */
data class TimelinePhotoResponse(
    val time: String, // 시간 ex. 오전 8시 19분, 오후 12시 24분 등
    val placeName: String, // 장소 이름
    val photoList: ArrayList<String> // 사진 리스트
)