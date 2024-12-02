package com.swuperpoint.moa_android.view.main.home.data

/* 홈 화면의 그룹 RV 아이템 */
data class HomeGroupItem(
    val groupId: String,
    val groupName: String,
    val color: Int,
    val emoji: String,
    val date: String,
    var isSelected: Boolean
)