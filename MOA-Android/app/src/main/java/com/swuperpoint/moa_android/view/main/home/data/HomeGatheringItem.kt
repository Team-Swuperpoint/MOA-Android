package com.swuperpoint.moa_android.view.main.home.data

/* 홈 화면의 모임 정보 아이템 */
data class HomeGatheringItem(
    val gatheringId: String, // 모임 ID
    val groupId: String, // 그룹 ID 추가
    val groupName: String, // 그룹 이름
    val gatheringName: String, // 모임 이름
    val date: String, // 모임 날짜
    val placeName: String, // 모임 장소(location -> placeName으로 변경)
    val dDay: Int, // 모임 D-Day
    val memberProfileList: ArrayList<String> // 모임원 프로필 사진
)
