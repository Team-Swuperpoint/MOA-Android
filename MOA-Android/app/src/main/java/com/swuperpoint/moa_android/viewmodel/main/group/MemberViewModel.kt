package com.swuperpoint.moa_android.viewmodel.main.group

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swuperpoint.moa_android.data.remote.model.group.MemberResponse
import com.swuperpoint.moa_android.view.main.group.data.MemberItem

/* 그룹원 목록 화면 뷰 모델 */
class MemberViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val _response = MutableLiveData<List<MemberResponse>>()
    val response: MutableLiveData<List<MemberResponse>> get() = _response

    var memberList: MutableLiveData<List<MemberItem>> = MutableLiveData()
    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    // 그룹장 여부 확인을 위한 LiveData 추가
    private val _isCreator = MutableLiveData<Boolean>()
    val isCreator: LiveData<Boolean> = _isCreator

    // 삭제 불가 메시지를 위한 LiveData 추가
    private val _deleteErrorMessage = MutableLiveData<String>()
    val deleteErrorMessage: LiveData<String> = _deleteErrorMessage

    // 그룹원 목록 조회
    fun fetchMembers(groupId: String) {
        val currentUserUid = auth.currentUser?.uid ?: return
        val membersList = mutableListOf<MemberResponse>()

        // 그룹 문서에서 멤버 UID 목록과 그룹장 UID 조회
        db.collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener { groupDoc ->
                val memberUIDs = groupDoc.get("memberUIDs") as? List<String> ?: listOf()
                val creatorUID = groupDoc.getString("createdBy")
                // 현재 사용자가 그룹장인지 확인
                _isCreator.value = (currentUserUid == creatorUID)

                var completedQueries = 0

                // 각 멤버의 상세 정보 조회
                memberUIDs.forEach { uid ->
                    db.collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                val member = MemberResponse(
                                    memberId = userDoc.id,
                                    profileImgURL = userDoc.getString("profile") ?: "",
                                    memberName = userDoc.getString("nickname") ?: ""
                                )
                                membersList.add(member)
                            }

                            completedQueries++

                            if (completedQueries == memberUIDs.size) {
                                val memberItems = membersList.map { member ->
                                    MemberItem(
                                        memberId = member.memberId,
                                        profileImgURL = member.profileImgURL,
                                        memberName = member.memberName,
                                        isEdit = false,
                                        isCreator = member.memberId == creatorUID,
                                        isCurrentUser = member.memberId == currentUserUid
                                    )
                                }.sortedWith(compareBy<MemberItem> { !it.isCreator }
                                    .thenBy { it.memberName })

                                memberList.value = memberItems
                            }
                        }
                }
            }
    }

    fun deleteMember(groupId: String, position: Int) {
        val memberToDelete = memberList.value?.get(position) ?: return
        val currentUserUid = auth.currentUser?.uid ?: return

        // 그룹 정보 먼저 확인
        db.collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener { groupDoc ->
                val creatorUID = groupDoc.getString("createdBy")

                // 삭제 권한 체크
                when {
                    // 그룹장을 삭제하려는 경우
                    memberToDelete.memberId == creatorUID -> {
                        _deleteResult.value = false
                        _deleteErrorMessage.value = "그룹장은 삭제할 수 없습니다."
                        return@addOnSuccessListener
                    }
                    // 그룹장이 아닌 사용자가 다른 멤버를 삭제하려는 경우
                    currentUserUid != creatorUID -> {
                        _deleteResult.value = false
                        _deleteErrorMessage.value = "그룹장만 멤버를 삭제할 수 있습니다."
                        return@addOnSuccessListener
                    }
                }

                // 그룹장이 일반 멤버를 삭제하는 경우
                val memberUIDs = groupDoc.get("memberUIDs") as? MutableList<String> ?: mutableListOf()
                memberUIDs.remove(memberToDelete.memberId)

                // 트랜잭션으로 두 작업 동시 처리
                db.runTransaction { transaction ->
                    // memberUIDs 업데이트
                    transaction.update(db.collection("groups").document(groupId),
                        "memberUIDs", memberUIDs)

                    // members 컬렉션에서도 삭제
                    transaction.delete(db.collection("groups").document(groupId)
                        .collection("members").document(memberToDelete.memberId))
                }.addOnSuccessListener {
                    _deleteResult.value = true
                    fetchMembers(groupId)
                }.addOnFailureListener { e ->
                    Log.e("MemberViewModel", "멤버 삭제 실패", e)
                    _deleteResult.value = false
                    _deleteErrorMessage.value = "멤버 삭제에 실패했습니다."
                }
            }
    }
}