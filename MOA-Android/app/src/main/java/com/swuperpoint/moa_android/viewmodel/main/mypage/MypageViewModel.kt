package com.swuperpoint.moa_android.viewmodel.main.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swuperpoint.moa_android.data.remote.model.mypage.MypageResponse

/* 마이페이지 화면의 뷰모델 */
class MypageViewModel: ViewModel() {
    private val _response = MutableLiveData<MypageResponse>()
    val response: LiveData<MypageResponse> get() = _response

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val profileImgURL: LiveData<String> = _response.map { it.profileImgURL } // 프로필 사진
    val nickname: LiveData<String> = _response.map { it.nickname } // 닉네임

    // 에러 처리를 위한 변수
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // 파이어베이스에서 사용자 정보 가져오기
    fun fetchMypage() {
        val currentUserUid = auth.currentUser?.uid ?: run {
            _error.value = "로그인된 사용자가 없습니다"
            return
        }

        db.collection("users")
            .document(currentUserUid)
            .get()
            .addOnSuccessListener { userDoc ->
                if (userDoc.exists()) {
                    _response.value = MypageResponse(
                        profileImgURL = userDoc.getString("profile") ?: "",
                        nickname = userDoc.getString("nickname") ?: ""
                    )
                } else {
                    _error.value = "사용자 정보를 찾을 수 없습니다"
                }
            }
            .addOnFailureListener { e ->
                _error.value = e.message ?: "알 수 없는 오류가 발생했습니다"
            }
    }
}