package com.swuperpoint.moa_android.viewmodel.main.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelineResponse

/* 타임라인 화면의 뷰모델 */
class TimelineViewModel: ViewModel() {
    private val _response = MutableLiveData<ArrayList<TimelineResponse>>()
    val response: LiveData<ArrayList<TimelineResponse>> get() = _response

    // 타임라인 리스트
    var timelineList: LiveData<ArrayList<TimelineResponse>?> = _response.map { it }

    // TODO: 파이어베이스에서 데이터 가져오기
    fun fetchTimeline() {
        val dummyResponse = arrayListOf(
            TimelineResponse(
                timelineId = "0",
                date = "2024.10.18",
                placeName = "서울 북촌",
                groupName = "먹짱친구들",
                gatheringName = "빵순이투어",
                groupMemberNum = 3,
                gatheringImgURL = "https://scontent.cdninstagram.com/v/t51.29350-15/459785497_563054136150695_1506853460429416071_n.jpg?stp=dst-jpg_e35&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent.cdninstagram.com&_nc_cat=111&_nc_ohc=tbcDYa8cFSMQ7kNvgEWprJI&_nc_gid=7d5d161ae3ae485ba64b4324ca13fa99&edm=APs17CUBAAAA&ccb=7-5&ig_cache_key=MzQ1Njg0NDMxNDc0NjU1MDQ3NQ%3D%3D.3-ccb7-5&oh=00_AYDMkrETwDkmlTyPBURJbSpvDaB68lA7CMcqWTdIWsa9zw&oe=674B33C6&_nc_sid=10d13b"
            )
        )

        // 데이터 업로드
        _response.value = dummyResponse
    }
}