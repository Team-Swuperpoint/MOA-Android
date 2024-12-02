package com.swuperpoint.moa_android.data.remote.model.group

/* 그룹원 데이터 응답 모델*/
data class MemberResponse(
    val memberId: String, // Long -> String으로 변경
    val profileImgURL: String,
    val memberName: String,
    val startPlace: PlaceLocationResponse? = null  // 추가
)
