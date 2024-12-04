package com.swuperpoint.moa_android.viewmodel.main.group

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.swuperpoint.moa_android.data.remote.model.group.GroupResponse
import com.swuperpoint.moa_android.view.main.group.data.GroupItem
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/* 그룹 화면 뷰 모델 */
class GroupViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth  // Firebase Auth 인스턴스 추가
    private val _groupResponse = MutableLiveData<ArrayList<GroupResponse>>() // 내부 수정용 변수
    val groupResponse: LiveData<ArrayList<GroupResponse>> get() = _groupResponse // 외부 읽기 전용 변수

    // 그룹 리스트
    var groupList = MutableLiveData<ArrayList<GroupItem>?>()

    // Firestore 리스너 참조 저장
    private var groupsListener: ListenerRegistration? = null

    init {
        _groupResponse.observeForever { response ->
            val items = response.map { group ->
                GroupItem(
                    groupId = group.groupId,
                    bgColor = group.bgColor,
                    emoji = group.emoji,
                    groupName = group.groupName,
                    groupMemberNum = group.groupMemberNum,
                    recentGathering = group.recentGathering
                )
            }

            groupList.value = ArrayList(items)
        }
        // 초기 데이터 로드 및 리스너 설정
        setupGroupsListener()
    }

    private fun setupGroupsListener() {
        // 현재 로그인한 사용자의 UID 가져오기
        val currentUserUid = auth.currentUser?.uid ?: return

        groupsListener = db.collection("groups")
            .whereArrayContains("memberUIDs", currentUserUid) // 현재 사용자가 멤버로 속해있는 그룹만 필터링
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("GroupViewModel", "그룹 목록 조회 실패", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.d("GroupViewModel", "그룹 목록 조회: ${snapshot.documents.size}개")
                    val deferredGroups = ArrayList<Triple<DocumentSnapshot, String, Int>>()
                    var processedGroups = 0

                    if (snapshot.documents.isEmpty()) {
                        // 속한 그룹이 없는 경우 빈 리스트로 업데이트
                        _groupResponse.postValue(ArrayList())
                        return@addSnapshotListener
                    }

                    for (document in snapshot.documents) {
                        try {
                            // 멤버 수와 모임 정보 동시에 가져오기
                            val groupDoc = db.collection("groups").document(document.id)

                            // 멤버 수 계산
                            val memberUIDs = document.get("memberUIDs") as? List<String> ?: listOf()
                            val memberCount = memberUIDs.size

                            // 모임 정보 가져오기
                            groupDoc.collection("gatherings").get()
                                .addOnSuccessListener { gatherings ->
                                    val recentGathering = processGatherings(gatherings.documents)

                                    deferredGroups.add(Triple(document, recentGathering, memberCount))
                                    processedGroups++

                                    // 모든 그룹 처리가 완료되면
                                    if (processedGroups == snapshot.documents.size) {
                                        // createdAt 기준으로 다시 정렬
                                        val sortedGroups = deferredGroups.sortedByDescending {
                                            it.first.getTimestamp("createdAt")?.toDate()
                                        }

                                        // 정렬된 순서대로 GroupResponse 생성
                                        val groupsList = sortedGroups.map { (doc, gathering, count) ->
                                            GroupResponse(
                                                groupId = doc.id,
                                                bgColor = (doc.get("bgColor") as? Long)?.toInt() ?: 0,
                                                emoji = doc.getString("emoji") ?: "",
                                                groupName = doc.getString("groupName") ?: "",
                                                groupMemberNum = count,
                                                recentGathering = gathering
                                            )
                                        }

                                        _groupResponse.postValue(ArrayList(groupsList))
                                    }
                                }
                                .addOnFailureListener { error ->
                                    Log.e("GroupViewModel", "모임 정보 조회 실패", error)
                                    processedGroups++
                                }
                        } catch (e: Exception) {
                            Log.e("GroupViewModel", "문서 처리 중 오류 발생: ${document.id}", e)
                            processedGroups++
                        }
                    }
                }
            }
    }

    private fun processGatherings(gatherings: List<DocumentSnapshot>): String {
        if (gatherings.isEmpty()) return "아직 모임이 없어요"

        val today = LocalDate.now()
        var recentPastDate: LocalDate? = null

        gatherings.forEach { gathering ->
            val dateStr = gathering.getString("date") ?: return@forEach
            try {
                val gatheringDate = LocalDate.parse(dateStr)
                if (gatheringDate.isBefore(today) || gatheringDate.isEqual(today)) {
                    if (recentPastDate == null || gatheringDate.isAfter(recentPastDate)) {
                        recentPastDate = gatheringDate
                    }
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "날짜 파싱 오류 발생: $dateStr", e)
            }
        }

        return if (recentPastDate != null) {
            val daysDiff = ChronoUnit.DAYS.between(recentPastDate, today)
            val monthsDiff = ChronoUnit.MONTHS.between(recentPastDate, today)
            when {
                daysDiff == 0L -> "오늘"
                monthsDiff == 0L -> "${daysDiff}일 전"
                else -> "${monthsDiff}달 전"
            }
        } else {
            "아직 모임이 없어요"
        }
    }


    // response 데이터 설정
    fun setGroupResponse(response: ArrayList<GroupResponse>) {
        _groupResponse.value = response
    }

    // 수동으로 데이터를 새로고침하고 싶을 때 사용할 수 있는 메서드
    fun refreshGroups() {
        // 기존 리스너가 있다면 제거
        groupsListener?.remove()
        // 새로운 리스너 설정
        setupGroupsListener()
    }


    override fun onCleared() {
        super.onCleared()
        // 리스너 제거
        groupsListener?.remove()
        // 관찰 해제
        _groupResponse.removeObserver { }
    }
}