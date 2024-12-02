package com.swuperpoint.moa_android.viewmodel.main.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.swuperpoint.moa_android.data.remote.model.group.GatheringInfoResponse
import com.swuperpoint.moa_android.data.remote.model.group.PlaceLocationResponse

/* 모임 정보 뷰모델 */
class GatheringInfoViewModel: ViewModel() {
    private val _response = MutableLiveData<GatheringInfoResponse>()
    val response: LiveData<GatheringInfoResponse> get() = _response

    // 모임 이름
    var gatheringName: LiveData<String> = _response.map { it.gatheringName }

    // 모임 날짜
    var gatheringDate: LiveData<String> = _response.map { it.date }

    // 모임 시작 시간
    var gatheringStartTime: LiveData<String> = _response.map { it.gatheringStartTime }

    // 모임 끝 시간
    var gatheringEndTime: LiveData<String> = _response.map { it.gatheringEndTime }

    // 모임 중간 지점
    var placeName: LiveData<String?> = _response.map { it.placeName }

    // 지하철 소요 시간
    var subwayTime: LiveData<String?> = _response.map { it.subwayTime }

    // 사용자의 출발 좌표
    var startPlace: LiveData<PlaceLocationResponse?> = _response.map { it.startPlace }

    // 중간지점 좌표
    var gatheringPlace: LiveData<PlaceLocationResponse?> = _response.map { it.gatheringPlace }

    // TODO: 파이어베이스에서 데이터 가져오기
    fun fetchGatheringInfo(gatheringId: Long) {
        // FIXME: 현재는 더미데이터 적용. 파이어베이스 로직으로 변경하기
        val dummyResponse = GatheringInfoResponse(
            "빵순이투어🥐",
            "10월 18일 (금)",
            "15:00",
            "18:00",
            "상왕십리역 2호선",
            "40분",
            PlaceLocationResponse(
                37.5939491407769,
                127.054890960564
            ),
            PlaceLocationResponse(
                37.6273815936787,
                127.091621159803
            )
        )
        // 데이터 업로드
        _response.value = dummyResponse
    }
}