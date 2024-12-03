package com.swuperpoint.moa_android.data.remote.model.home

import com.swuperpoint.moa_android.view.main.home.data.HomeGatheringItem

/* 홈 화면에서 사용하는 데이터 응답 모델 */
data class HomeResponse(
    val nickname: String,
    var groupList: ArrayList<HomeGroupListResponse>?,
    var groupInfo: HomeGatheringItem?
)

/* 다가오는 모임 리스트 */
data class HomeGroupListResponse(
    val groupId: String,
    val groupName: String,
    val bgColor: Int, // 그룹 배경 색상
    val emoji: String, // 이모지
    val date: String // 모임 날짜
)

/* 가장 가까운 1개 모임 정보 */
data class HomeGroupInfoResponse(
    val gatheringId: String, // 모임 ID
    val groupId: String, // 그룹 ID 추가
    val groupName: String, // 그룹 이름
    val gatheringName: String, // 모임 이름
    val date: String, // 모임 날짜
    val location: String, // 모임 장소
    val dDay: Int, // 모임 D-Day
    val memberProfileList: ArrayList<String> // 모임원 프로필 사진
)