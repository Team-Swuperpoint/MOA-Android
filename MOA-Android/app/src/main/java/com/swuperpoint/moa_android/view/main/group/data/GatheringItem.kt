package com.swuperpoint.moa_android.view.main.group.data

/* 그룹 정보 화면의 모임 RV 아이템 */
data class GatheringItem(
    val gatheringId: String, // Long -> String으로 변경
    val gatheringName: String,
    val date: String,
    val gatheringImgURL: String
)