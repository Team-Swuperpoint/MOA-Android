package com.swuperpoint.moa_android.view.main.group.data

/* 그룹 화면의 그룹 RV 아이템 */
data class GroupItem(
    val groupId: String, // Long -> String으로 변경
    val bgColor: Int,
    val emoji: String,
    val groupName: String,
    val groupMemberNum: Int,
    val recentGathering: String
)