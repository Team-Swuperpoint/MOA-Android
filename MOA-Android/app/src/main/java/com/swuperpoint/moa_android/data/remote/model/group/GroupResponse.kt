package com.swuperpoint.moa_android.data.remote.model.group

/* 그룹 화면에서 사용하는 데이터 응답 모델 */
data class GroupResponse(
    val groupId: String, // Long -> String으로 변경
    val bgColor: Int,
    val emoji: String,
    val groupName: String,
    val groupMemberNum: Int,
    val recentGathering: String
)
