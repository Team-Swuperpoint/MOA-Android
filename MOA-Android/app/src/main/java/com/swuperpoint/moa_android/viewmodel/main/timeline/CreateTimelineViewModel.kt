package com.swuperpoint.moa_android.viewmodel.main.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.swuperpoint.moa_android.data.remote.model.group.CreateTimelineGatheringResponse
import com.swuperpoint.moa_android.data.remote.model.group.CreateTimelineGroupResponse
import com.swuperpoint.moa_android.data.remote.model.group.CreateTimelineInfoResponse

/* 타임라인 만들기 화면의 뷰모델 */
class CreateTimelineViewModel: ViewModel() {
    private val _response = MutableLiveData<CreateTimelineInfoResponse>()
    val response: LiveData<CreateTimelineInfoResponse> get() = _response

    // 그룹 리스트
    var groupList: LiveData<ArrayList<CreateTimelineGroupResponse>?> = _response.map { it.groupList }

    // 모임 리스트
    var gatheringList: LiveData<ArrayList<CreateTimelineGatheringResponse>?> = _response.map { it.gatheringList }

    // TODO: 타임라인 만들기 화면에 진입했을 때, API 호출
    fun fetchCreateTimeline() {
        val dummyResponse = CreateTimelineInfoResponse(
            groupList = arrayListOf(
                CreateTimelineGroupResponse(
                    "0",
                    "먹짱친구들"
                ),
                CreateTimelineGroupResponse(
                    "1",
                    "강쥐산책모임"
                ),
                CreateTimelineGroupResponse(
                    "2",
                    "여행"
                )
            ),
            gatheringList = arrayListOf(
                CreateTimelineGatheringResponse(
                    "0",
                    "빵순이투어"
                ),
                CreateTimelineGatheringResponse(
                    "1",
                    "북촌카페투어"
                )
            )
        )
        // 데이터 업로드
        _response.value = dummyResponse
    }
}