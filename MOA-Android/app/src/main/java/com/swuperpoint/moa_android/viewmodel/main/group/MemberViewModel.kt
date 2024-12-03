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

    fun fetchMembers(groupId: String) {
        val currentUserEmail = auth.currentUser?.email ?: return
        val membersList = mutableListOf<MemberResponse>()

        // 먼저 그룹 문서에서 memberEmails 정보를 가져옴
        db.collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener { groupDoc ->
                val memberEmails = groupDoc.get("memberEmails") as? List<String> ?: listOf()
                val creatorEmail = groupDoc.getString("createdBy") ?: memberEmails.firstOrNull() // createdBy 필드로 그룹장 확인
                _isCreator.value = (currentUserEmail == creatorEmail)

                // members 서브컬렉션도 조회
                db.collection("groups")
                    .document(groupId)
                    .collection("members")
                    .get()
                    .addOnSuccessListener { membersSnapshot ->
                        // 서브컬렉션의 멤버들 추가
                        membersSnapshot.documents.forEach { doc ->
                            val member = MemberResponse(
                                memberId = doc.id,
                                profileImgURL = doc.getString("profile") ?: "",
                                memberName = doc.getString("nickname") ?: ""
                            )
                            if (!membersList.any { it.memberId == member.memberId }) {
                                membersList.add(member)
                            }
                        }

                        // memberEmails에서 추가 정보 조회
                        var completedQueries = 0
                        memberEmails.forEach { email ->
                            db.collection("users")
                                .document(email)
                                .get()
                                .addOnSuccessListener { userDoc ->
                                    if (userDoc.exists() &&
                                        !membersList.any { it.memberId == email }) {
                                        val member = MemberResponse(
                                            memberId = userDoc.id,
                                            profileImgURL = userDoc.getString("profile") ?: "",
                                            memberName = userDoc.getString("nickname") ?: ""
                                        )
                                        membersList.add(member)
                                    }

                                    // 최종 결과를 MemberItem으로 변환하여 설정
                                    completedQueries++
                                    if (completedQueries == memberEmails.size) {
                                        val memberItems = membersList.map { member ->
                                            MemberItem(
                                                memberId = member.memberId,
                                                profileImgURL = member.profileImgURL,
                                                memberName = member.memberName,
                                                isEdit = false,
                                                isCreator = member.memberId == creatorEmail,
                                                isCurrentUser = member.memberId == currentUserEmail
                                            )
                                        }   // 정렬 로직 추가
                                            .sortedWith(compareBy<MemberItem> { !it.isCreator }  // 그룹장을 맨 앞으로
                                            .thenBy { it.memberName })  // 그 다음 이름순

                                        memberList.value = memberItems
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("MemberViewModel", "사용자 정보 조회 실패: $email", e)
                                    completedQueries++
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MemberViewModel", "멤버 서브컬렉션 조회 실패", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MemberViewModel", "그룹 정보 조회 실패", e)
            }

    }

    fun deleteMember(groupId: String, position: Int) {
        val memberToDelete = memberList.value?.get(position) ?: return

        // 그룹장 본인은 삭제 불가
        if (memberToDelete.isCreator && memberToDelete.isCurrentUser) {
            _deleteResult.value = false
            _deleteErrorMessage.value = "그룹장은 삭제할 수 없습니다!"
            return
        }

        // memberEmails 배열에서 멤버 제거
        db.collection("groups")
            .document(groupId)
            .get()
            .addOnSuccessListener { groupDoc ->
                val memberEmails = groupDoc.get("memberEmails") as? MutableList<String> ?: mutableListOf()
                memberEmails.remove(memberToDelete.memberId)

                // memberEmails 업데이트
                db.collection("groups")
                    .document(groupId)
                    .update("memberEmails", memberEmails)
                    .addOnSuccessListener {
                        // members 서브컬렉션에서도 제거
                        db.collection("groups")
                            .document(groupId)
                            .collection("members")
                            .document(memberToDelete.memberId)
                            .delete()
                            .addOnSuccessListener {
                                _deleteResult.value = true
                                fetchMembers(groupId)  // 목록 새로고침
                            }
                            .addOnFailureListener { e ->
                                Log.e("MemberViewModel", "멤버 서브컬렉션 삭제 실패", e)
                                _deleteResult.value = false
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("MemberViewModel", "memberEmails 업데이트 실패", e)
                        _deleteResult.value = false
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MemberViewModel", "그룹 정보 조회 실패", e)
                _deleteResult.value = false
            }
    }

}