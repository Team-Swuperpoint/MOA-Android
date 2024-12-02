package com.swuperpoint.moa_android.viewmodel.main.group

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swuperpoint.moa_android.data.remote.model.group.MemberResponse
import com.swuperpoint.moa_android.view.main.group.data.MemberItem

/* 그룹원 목록 화면 뷰 모델 */
class MemberViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val _response = MutableLiveData<List<MemberResponse>>()
    val response: MutableLiveData<List<MemberResponse>> get() = _response

    // 현재 선택된 그룹 ID 저장
    private var currentGroupId: String? = null

    // 그룹원 리스트
    var memberList: MutableLiveData<List<MemberItem>> = MutableLiveData()

    // 삭제 결과를 위한 LiveData 추가
    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult

    init {
        // 서버에서 데이터를 받아서 memberList로 변환
        _response.observeForever { response ->
            memberList.value = response.map { member ->
                MemberItem(
                    memberId = member.memberId,
                    profileImgURL = member.profileImgURL,
                    memberName = member.memberName,
                    isEdit = false
                )
            }
        }
    }

    fun fetchMembers(groupId: String) { // Long -> String

        db.collection("groups")
            .document(groupId)
            .collection("members")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val membersList = querySnapshot.documents.map { document ->
                    MemberResponse(
                        memberId = document.id,
                        profileImgURL = document.getString("profile") ?: "",
                        memberName = document.getString("nickname") ?: ""
                    )
                }
                _response.value = membersList
            }
            .addOnFailureListener { e ->
                Log.e("MemberViewModel", "Error fetching members", e)
            }
    }

    // deleteMember 함수 수정
    fun deleteMember(groupId: String, position: Int) {
        val currentList = memberList.value?.toMutableList() ?: return
        if (position !in currentList.indices) return

        val memberToDelete = currentList[position]

        db.collection("groups")
            .document(groupId)
            .collection("members")
            .document(memberToDelete.memberId)
            .delete()
            .addOnSuccessListener {
                currentList.removeAt(position)
                memberList.value = currentList
                _deleteResult.value = true
            }
            .addOnFailureListener { e ->
                Log.e("MemberViewModel", "Error deleting member", e)
                _deleteResult.value = false
            }
    }

    // 새 멤버 추가 함수
    fun addMember(nickname: String, profile: String) {
        currentGroupId?.let { groupId ->
            val newMember = hashMapOf(
                "nickname" to nickname,
                "profile" to profile
            )

            db.collection("groups")
                .document(groupId)
                .collection("members")
                .add(newMember)
                .addOnSuccessListener { documentReference ->
                    // 새 멤버 추가 후 멤버 목록 다시 불러오기
                    fetchMembers(groupId)
                    Log.d("MemberViewModel", "Member successfully added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("MemberViewModel", "Error adding member", e)
                }
        }
    }
}