package com.swuperpoint.moa_android.data.remote.model.group

/* 타임라인 만들기 화면에 들어왔을 때 호출하는 API의 데이터 응답 모델 */
data class CreateTimelineInfoResponse(
    val groupList: ArrayList<CreateTimelineGroupResponse>, // 사용자가 가입한 그룹 리스트
    val gatheringList: ArrayList<CreateTimelineGatheringResponse> // 모임 리스트
)

data class CreateTimelineGroupResponse(
    val groupId: String, // 그룹 아이디
    val groupName: String // 그룹 이름
)

data class CreateTimelineGatheringResponse(
    val gatheringId: String, // 모임 id
    val gatheringName: String // 모임 이름
)