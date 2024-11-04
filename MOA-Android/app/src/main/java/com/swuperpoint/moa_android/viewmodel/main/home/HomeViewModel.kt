package com.swuperpoint.moa_android.viewmodel.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.home.HomeGroupListResponse
import com.swuperpoint.moa_android.data.remote.model.home.HomeResponse
import com.swuperpoint.moa_android.view.main.home.data.HomeGatheringItem
import com.swuperpoint.moa_android.view.main.home.data.HomeGroupItem

/* 홈 화면 뷰 모델 */
class HomeViewModel : ViewModel() {
    private val _homeResponse = MutableLiveData<HomeResponse>() // 내부 수정용 변수
    val homeResponse: LiveData<HomeResponse> get() = _homeResponse // 외부 읽기 전용 변수

    // 닉네임
    var nickname: LiveData<String> = _homeResponse.map { it.nickname }

    // 모임 리스트
    var groupList = MutableLiveData<ArrayList<HomeGroupItem>>()

    // 1개 모임 정보
    var gatheringInfo = MutableLiveData<HomeGatheringItem>()

    init {
        // DTO -> Entity로 변환
        _homeResponse.observeForever { response ->
            val items = response.groupList?.mapIndexed { index, group ->
                HomeGroupItem(
                    color = group.bgColor,
                    emoji = group.emoji,
                    date = group.date,
                    isSelected = index == 0 // 첫번째 아이템만 true로 설정
                )
            } ?: emptyList()

            val gathering = HomeGatheringItem(
                gatheringId = response.groupInfo.gatheringId,
                groupName = response.groupInfo.groupName,
                gatheringName = response.groupInfo.gatheringName,
                date = response.groupInfo.date,
                location = response.groupInfo.location,
                dDay = response.groupInfo.dDay,
                memberProfileList = response.groupInfo.memberProfileList
            )

            groupList.value = ArrayList(items)
            gatheringInfo.value = gathering
        }
    }

    // 더미데이터 적용
    // TODO: 파이어베이스 연결 시 삭제하기
    fun fetchGroupList(): ArrayList<HomeGroupListResponse> {
        return arrayListOf(
            HomeGroupListResponse(R.color.main_500, "🍔", "9일"),
            HomeGroupListResponse(R.color.main_100, "🐶", "13일"),
            HomeGroupListResponse(R.color.sub_500, "✈️", "24일")
        )
    }

    fun fetchGatheringInfo(): HomeGatheringItem {
        return HomeGatheringItem(
                1,
                "먹짱친구들",
                "빵순이투어🥐",
                "10월 8일 (화) 15:02",
                "경복궁역 5번 출구",
                2,
                arrayListOf("https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDA0MjhfMjY4%2FMDAxNzE0MzEyMjk5NDQ4.Q80gZpRJgqUFcOCVQJHZp8dHtO3R7tNyiDTuyl3jC7Ug.EleTj7FhgQRu4tYOT9lYvx2Frx2dHZO3CPsJV9qspUwg.JPEG%2FScreenshot%25A3%25DF20240428%25A3%25DF224935%25A3%25DFInstagram.jpg&type=sc960_832",
                    "https://search.pstatic.net/sunny/?src=https%3A%2F%2Fi1.sndcdn.com%2Fartworks-N58pyyEi9rcBd4wf-STMfCQ-t500x500.jpg&type=sc960_832",
                    "https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyMjAxMzFfMjg5%2FMDAxNjQzNjM4NzU1NTA5.dhUbIVx5-n9NmT38o1HncivKJAVMzHk3FCoRziAA9d4g.1pLdpIJV8KMxKqYsxtue18CpHeReRPoSV-1R2Ll_0E8g.JPEG.gyulibae0905%2FScreenshot%25A3%25DF20220131%25A3%25AD225304%25A3%25DFInstagram.jpg&type=sc960_832")
        )
    }

    // response 데이터 설정
    fun setHomeResponse(response: HomeResponse) {
        _homeResponse.value = response
    }

    override fun onCleared() {
        super.onCleared()
        // 관찰 해제
        _homeResponse.removeObserver { }
    }

    // 다음 모임 확인하기
    fun showNextGathering() {
    }
}