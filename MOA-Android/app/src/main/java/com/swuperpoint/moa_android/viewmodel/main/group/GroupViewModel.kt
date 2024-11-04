package com.swuperpoint.moa_android.viewmodel.main.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.swuperpoint.moa_android.data.remote.model.group.GroupResponse
import com.swuperpoint.moa_android.view.main.group.data.GroupItem

/* 그룹 화면 뷰 모델 */
class GroupViewModel: ViewModel() {
    private val _groupResponse = MutableLiveData<ArrayList<GroupResponse>>() // 내부 수정용 변수
    val groupResponse: LiveData<ArrayList<GroupResponse>> get() = _groupResponse // 외부 읽기 전용 변수

    // 그룹 리스트
    var groupList = MutableLiveData<ArrayList<GroupItem>?>().apply {
        value = null
    }

    init {
        _groupResponse.observeForever { response ->
            val items = response.map { group ->
                GroupItem(
                    bgColor = group.bgColor,
                    emoji = group.emoji,
                    groupName = group.groupName,
                    groupMemberNum = group.groupMemberNum,
                    recentGathering = group.recentGathering
                )
            } ?: emptyList()

            groupList.value = ArrayList(items)
        }
    }

    // API 로직 작성
//    fun fetchGroupList(): ArrayList<GroupResponse> {
//
//    }

    // response 데이터 설정
    fun setGroupResponse(response: ArrayList<GroupResponse>) {
        _groupResponse.value = response
    }

    override fun onCleared() {
        super.onCleared()
        // 관찰 해제
        _groupResponse.removeObserver { }
    }
}