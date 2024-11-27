package com.swuperpoint.moa_android.data.remote.model.group

/* 그룹원 데이터 응답 모델*/
data class MemberResponse(
    val memberId: Long,
    val profileImgURL: String,
    val memberName: String
)
