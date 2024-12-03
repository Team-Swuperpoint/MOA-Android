package com.swuperpoint.moa_android.view.main.group.data

/* 그룹원 아이템 */
data class MemberItem(
    val memberId: String, // Long -> String으로 변경
    val profileImgURL: String,
    val memberName: String,
    var isEdit: Boolean?,
    val isCreator: Boolean = false,  // 그룹장 여부 추가
    val isCurrentUser: Boolean = false  // 현재 로그인한 사용자인지 확인 여부 추가
)