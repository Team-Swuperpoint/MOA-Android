package com.swuperpoint.moa_android.viewmodel.main.group

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.group.GroupGatheringResponse
import com.swuperpoint.moa_android.data.remote.model.group.GroupInfoResponse
import com.swuperpoint.moa_android.data.remote.model.group.MemberResponse
import com.swuperpoint.moa_android.view.main.group.data.GatheringItem
import com.swuperpoint.moa_android.view.main.group.data.MemberItem
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/* 그룹 정보 화면 뷰 모델 */
class GroupInfoViewModel: ViewModel() {
    private val _response = MutableLiveData<GroupInfoResponse>()
    private val db = Firebase.firestore
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
    var emoji: LiveData<String> = _response.map { it.emoji }
    var groupName: LiveData<String> = _response.map { it.groupName }
    var recentGathering: LiveData<String?> = _response.map { it.recentGathering }
    val groupCode: LiveData<String> = _response.map { it.groupCode }
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

    private fun getRelativeTimeString(dateString: String): String? {
        try {
            val date = LocalDate.parse(dateString)
            val today = LocalDate.now()

            // 날짜가 미래인 경우 null 반환
            if (date.isAfter(today)) {
                return null
            }

            val daysDiff = ChronoUnit.DAYS.between(date, today)
            val monthsDiff = ChronoUnit.MONTHS.between(date, today)

            return when {
                daysDiff == 0L -> "오늘"
                monthsDiff == 0L -> "${daysDiff}일 전"
                else -> "${monthsDiff}달 전"
            }
        } catch (e: Exception) {
            Log.e("GroupInfoViewModel", "Error calculating relative time", e)
            return null
        }
    }

    fun fetchGroupInfo(groupId: String) {
        db.collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val recentDate = document.getString("recentGathering")
                    val relativeTime = recentDate?.let { getRelativeTimeString(it) }

                    val groupResponse = GroupInfoResponse(
                        bgColor = (document.get("bgColor") as? Long)?.toInt() ?: 0,
                        emoji = document.getString("emoji") ?: "",
                        groupName = document.getString("groupName") ?: "",
                        recentGathering = relativeTime ?: "아직 모임이 없어요",
                        groupCode = document.id.take(5),
                        memberList = arrayListOf(),
                        gatheringList = null
                    )

                    // 멤버와 모임 정보 동시 조회
                    fetchGroupMembers(groupId) { memberList ->
                        groupResponse.memberList = memberList
                        fetchGroupGatherings(groupId) { gatheringList ->
                            groupResponse.gatheringList = gatheringList
                            _response.value = groupResponse
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("GroupInfoViewModel", "Error fetching group info", e)
            }
    }

    private fun fetchGroupMembers(groupId: String, callback: (ArrayList<MemberResponse>) -> Unit) {
        db.collection("groups")
            .document(groupId)
            .collection("members")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val memberList = ArrayList<MemberResponse>()
                for (document in querySnapshot.documents) {
                    val member = MemberResponse(
                        memberId = document.id,
                        profileImgURL = document.getString("profile") ?: "",
                        memberName = document.getString("nickname") ?: ""
                    )
                    memberList.add(member)
                }
                callback(memberList)
            }
            .addOnFailureListener { e ->
                Log.e("GroupInfoViewModel", "Error fetching members", e)
                callback(ArrayList())
            }
    }

    private fun fetchGroupGatherings(groupId: String, callback: (ArrayList<GroupGatheringResponse>) -> Unit) {
        db.collection("groups")
            .document(groupId)
            .collection("gatherings")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val gatheringList = ArrayList<GroupGatheringResponse>()
                for (document in querySnapshot.documents) {
                    // Firebase에서 가져온 원본 날짜 ("2024-12-06")
                    val rawDate = document.getString("date") ?: ""
                    // 날짜 포맷 변환: yyyy-MM-dd -> yy.MM.dd
                    val formattedDate = try {
                        val date = LocalDate.parse(rawDate) // 원본 날짜 문자열을 LocalDate 객체로 파싱
                        // LocalDate 객체에서 년도 뒷 2자리, 월, 일을 추출하여 yy.MM.dd 형식으로 조합
                        "${date.year.toString().takeLast(2)}.${date.monthValue.toString().padStart(2, '0')}.${date.dayOfMonth.toString().padStart(2, '0')}"
                    } catch (e: Exception) {
                        rawDate // 변환 실패 시 원본 날짜 그대로 사용
                    }

                    val gathering = GroupGatheringResponse(
                        gatheringId = document.id,
                        gatheringName = document.getString("gatheringName") ?: "",
                        date = formattedDate,
                        gatheringImgURL = document.getString("gatheringImgURL") ?: ""
                    )
                    gatheringList.add(gathering)
                }
                callback(gatheringList)
            }
            .addOnFailureListener { e ->
                Log.e("GroupInfoViewModel", "Error fetching gatherings", e)
                callback(ArrayList())
            }
    }
}