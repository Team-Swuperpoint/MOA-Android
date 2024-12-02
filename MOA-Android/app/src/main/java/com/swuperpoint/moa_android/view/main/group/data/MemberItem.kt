package com.swuperpoint.moa_android.view.main.group.data

/* 그룹원 아이템 */
data class MemberItem(
    val memberId: String, // Long -> String으로 변경
    val profileImgURL: String,
    val memberName: String,
    var isEdit: Boolean?
)