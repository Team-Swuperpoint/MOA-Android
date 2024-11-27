package com.swuperpoint.moa_android.view.main.home.data

/* 홈 화면의 모임 정보 아이템 */
data class HomeGatheringItem(
    val gatheringId: Long, // 모임 ID
    val groupName: String, // 그룹 이름
    val gatheringName: String, // 모임 이름
    val date: String, // 모임 날짜
    val location: String, // 모임 장소
    val dDay: Int, // 모임 D-Day
    val memberProfileList: ArrayList<String> // 모임원 프로필 사진
)
