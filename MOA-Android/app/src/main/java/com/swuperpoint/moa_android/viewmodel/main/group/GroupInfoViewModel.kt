package com.swuperpoint.moa_android.viewmodel.main.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.group.GroupGatheringResponse
import com.swuperpoint.moa_android.data.remote.model.group.GroupInfoResponse
import com.swuperpoint.moa_android.data.remote.model.group.MemberResponse
import com.swuperpoint.moa_android.view.main.group.data.GatheringItem
import com.swuperpoint.moa_android.view.main.group.data.MemberItem

/* 그룹 정보 화면 뷰 모델 */
class GroupInfoViewModel: ViewModel() {
    private val _response = MutableLiveData<GroupInfoResponse>()
    val response: LiveData<GroupInfoResponse> get() = _response

    // 배경색상
    var bgColor: LiveData<Int> = _response.map { color ->
        when (color.bgColor) {
            0 -> R.color.main_500
            1 -> R.color.sub_300
            2 -> R.color.sub_500
            3 -> R.color.main_50
            4 -> R.color.main_100
            5 -> R.color.sub_200
            else -> R.color.main_500
        }
    }

    // 이모지
    var emoji: LiveData<String> = _response.map { it.emoji }

    // 그룹 이름
    var groupName: LiveData<String> = _response.map { it.groupName }

    // 최근 모임 날짜
    var recentGathering: LiveData<String> = _response.map { it.recentGathering.toString() }

    // 그룹 코드
    val groupCode: LiveData<String> = _response.map { it.groupCode }

    // 그룹원 리스트
    var memberList: LiveData<List<MemberItem>> = _response.map { response ->
        response.memberList.map { member ->
            MemberItem(
                memberId = member.memberId,
                profileImgURL = member.profileImgURL,
                memberName = member.memberName
            )
        }
    }

    // 모임 리스트
    var gatheringList: LiveData<List<GatheringItem>?> = _response.map { response ->
        response.gatheringList?.map { gathering ->
            GatheringItem(
                gatheringId = gathering.gatheringId,
                gatheringName = gathering.gatheringName,
                date = gathering.date,
                gatheringImgURL = gathering.gatheringImgURL
            )
        }
    }

    // TODO: 파이어베이스에서 데이터 가져오기
    fun fetchGroupInfo(groupId: Long) {
        // FIXME: 현재는 더미 데이터 적용! 여기에 파이어베이스 로직 작성하기
        val dummyResponse = GroupInfoResponse(
            bgColor = 0,
            emoji = "🍔",
            groupName = "먹짱친구들",
            recentGathering = "1일 전",
            groupCode = "ABC12",
            memberList = arrayListOf(
                MemberResponse(0, "https://scontent.cdninstagram.com/v/t51.29350-15/459785497_563054136150695_1506853460429416071_n.jpg?stp=dst-jpg_e35&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent.cdninstagram.com&_nc_cat=111&_nc_ohc=tbcDYa8cFSMQ7kNvgEWprJI&_nc_gid=7d5d161ae3ae485ba64b4324ca13fa99&edm=APs17CUBAAAA&ccb=7-5&ig_cache_key=MzQ1Njg0NDMxNDc0NjU1MDQ3NQ%3D%3D.3-ccb7-5&oh=00_AYDMkrETwDkmlTyPBURJbSpvDaB68lA7CMcqWTdIWsa9zw&oe=674B33C6&_nc_sid=10d13b", "영현"),
                MemberResponse(1, "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/401092395_876814224109119_7386721604391564567_n.heic?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xMjAweDE1MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=103&_nc_ohc=FDK2-Gu5j1MQ7kNvgGU4kvH&_nc_gid=9e3ccd67d1c94135b437ff95b6fc5556&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzIzNDM4MTYwODI3MDU3NzU0NA%3D%3D.3-ccb7-5&oh=00_AYB5OzEMNgvCblMaBVh3xEj_epZQgnarsyjWp2D-EeUpbQ&oe=674B1F70&_nc_sid=bfaa47", "도운"),
                MemberResponse(2, "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/457364110_1727202384772753_502586513782673359_n.jpg?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=SJG3PxnyNGgQ7kNvgFkAECV&_nc_gid=190fd06c5d4f4257891af4f620eb1f78&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzQ0NjY1MDcxMzAyNDA5MDI5NQ%3D%3D.3-ccb7-5&oh=00_AYAH5f-vQouhO4Bb5SI4XDBb1HIXDWJ5XhLKTVSQyoxctw&oe=674B2BFF&_nc_sid=bfaa47", "지수")
            ),
            gatheringList = arrayListOf(
                GroupGatheringResponse(0, "빵순이투어🥐", "24.10.08", "https://search.pstatic.net/common/?src=http%3A%2F%2Fblogfiles.naver.net%2FMjAyNDA5MTBfMjgx%2FMDAxNzI1OTQ3MzI4OTk0.9eYGlShugej6Jqes7PyvQdEzl2cg_Kc5_adz4-Q1acQg.qa9X3db6vOXyKHU8e5A4N0S6ZiT2K8Y7TLUaxKmX830g.JPEG%2FSeoul-50_%25282%2529_%25281%2529.jpg")
            )
        )
        // 데이터 업데이트
        _response.value = dummyResponse
    }
}