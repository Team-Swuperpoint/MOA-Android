package com.swuperpoint.moa_android.viewmodel.main.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.swuperpoint.moa_android.data.remote.model.group.GatheringPlaceResponse
import com.swuperpoint.moa_android.data.remote.model.group.MemberResponse
import com.swuperpoint.moa_android.data.remote.model.group.PlaceLocationResponse
import com.swuperpoint.moa_android.data.remote.model.group.RecommendPlaceResponse
import com.swuperpoint.moa_android.view.main.group.data.AddressItem
import com.swuperpoint.moa_android.view.main.group.data.MemberItem

/* 모임 중간지점 뷰모델 */
class GatheringPlaceViewModel: ViewModel() {
    private val _response = MutableLiveData<GatheringPlaceResponse>()
    val response: LiveData<GatheringPlaceResponse> get() = _response

    // 그룹원 리스트
    var memberList: LiveData<List<MemberItem>> = _response.map { response ->
        response.memberList.map { member ->
            MemberItem(
                memberId = member.memberId,
                profileImgURL = member.profileImgURL,
                memberName = member.memberName,
                isEdit = false
            )
        }
    }

    // 총 그룹원 인원수
    var memberNum: LiveData<Int> = _response.map { it.memberNum }

    // 사용자가 새롭게 선택한 출발 장소 정보
    private val _addressItem = MutableLiveData<AddressItem?>()
    val addressItem: LiveData<AddressItem?> get() = _addressItem

    // 사용자가 입력했었던 출발 장소 이름
    var userPlaceName: LiveData<String?> = MediatorLiveData<String?>().apply {
        addSource(_response.map { it.userPlaceName }) { value = it }
        addSource(_addressItem) { new ->
            value = new?.title ?: _response.value?.userPlaceName
        }
    }

    // 사용자의 출발 좌표
    var startPlace: LiveData<PlaceLocationResponse?> = _response.map { it.startPlace }

    // 추천 중간 지점 리스트
    var placeList: LiveData<ArrayList<RecommendPlaceResponse>?> = _response.map { it.placeList }

    // TODO: 파이어베이스에서 데이터 가져오기
    fun fetchGatheringPlace(gatheringId: Long) {
        val dummyResponse = GatheringPlaceResponse(
            memberList = arrayListOf(
                MemberResponse(0, "https://scontent.cdninstagram.com/v/t51.29350-15/459785497_563054136150695_1506853460429416071_n.jpg?stp=dst-jpg_e35&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent.cdninstagram.com&_nc_cat=111&_nc_ohc=tbcDYa8cFSMQ7kNvgEWprJI&_nc_gid=7d5d161ae3ae485ba64b4324ca13fa99&edm=APs17CUBAAAA&ccb=7-5&ig_cache_key=MzQ1Njg0NDMxNDc0NjU1MDQ3NQ%3D%3D.3-ccb7-5&oh=00_AYDMkrETwDkmlTyPBURJbSpvDaB68lA7CMcqWTdIWsa9zw&oe=674B33C6&_nc_sid=10d13b", "영현"),
                MemberResponse(1, "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/401092395_876814224109119_7386721604391564567_n.heic?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xMjAweDE1MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=103&_nc_ohc=FDK2-Gu5j1MQ7kNvgGU4kvH&_nc_gid=9e3ccd67d1c94135b437ff95b6fc5556&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzIzNDM4MTYwODI3MDU3NzU0NA%3D%3D.3-ccb7-5&oh=00_AYB5OzEMNgvCblMaBVh3xEj_epZQgnarsyjWp2D-EeUpbQ&oe=674B1F70&_nc_sid=bfaa47", "도운"),
            ),
            memberNum = 3,
            userPlaceName = "혜화역 4호선",
            startPlace = PlaceLocationResponse(
                37.5939491407769,
                127.054890960564
            ),
            placeList = arrayListOf(
                RecommendPlaceResponse("상왕십리역 2호선", "서울 성동구 왕십리로 374", "40분 소요", 37.5939491407769, 127.054890960564),
                RecommendPlaceResponse("건대입구 7호선", "서울 광진구 능동로 110", "45분 소요", 37.5939491407769, 127.054890960564),
            )
        )
        // 데이터 업로드
        _response.value = dummyResponse
    }

    // 새롭게 선택한 장소 정보 업데이트
    fun updateAddressItem(newAddress: AddressItem?) {
        _addressItem.value = newAddress
    }
}