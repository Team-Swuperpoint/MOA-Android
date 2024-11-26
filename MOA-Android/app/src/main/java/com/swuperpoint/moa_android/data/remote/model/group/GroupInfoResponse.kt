package com.swuperpoint.moa_android.data.remote.model.group

/* 그룹 정보 화면에서 사용하는 데이터 응답 모델 */
data class GroupInfoResponse(
    val bgColor: Int,
    val emoji: String,
    val groupName: String,
    val recentGathering: String?,
    val groupCode: String,
    val memberList: ArrayList<MemberResponse>,
    val gatheringList: ArrayList<GroupGatheringResponse>?
)

/* 모임 1개 정보 데이터 응답 모델 */
data class GroupGatheringResponse(
    val gatheringId: Long,
    val gatheringName: String,
    val date: String,
    val gatheringImgURL: String
)