package com.swuperpoint.moa_android.viewmodel.main.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.swuperpoint.moa_android.data.remote.model.mypage.MypageResponse

/* 마이페이지 화면의 뷰모델 */
class MypageViewModel: ViewModel() {
    private val _response = MutableLiveData<MypageResponse>()
    val response: LiveData<MypageResponse> get() = _response

    val profileImgURL: LiveData<String> = _response.map { it.profileImgURL } // 프로필 사진
    val nickname: LiveData<String> = _response.map { it.nickname } // 닉네임

    // TODO: 파이어베이스에서 데이터 검색
    fun fetchMypage() {
        val dummyResponse = MypageResponse(
            "https://scontent-ssn1-1.cdninstagram.com/v/t51.29350-15/457364110_1727202384772753_502586513782673359_n.jpg?stp=dst-jpg_e35_tt6&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE4MDAuc2RyLmYyOTM1MC5kZWZhdWx0X2ltYWdlIn0&_nc_ht=scontent-ssn1-1.cdninstagram.com&_nc_cat=101&_nc_ohc=SJG3PxnyNGgQ7kNvgFkAECV&_nc_gid=190fd06c5d4f4257891af4f620eb1f78&edm=AOmX9WgBAAAA&ccb=7-5&ig_cache_key=MzQ0NjY1MDcxMzAyNDA5MDI5NQ%3D%3D.3-ccb7-5&oh=00_AYAH5f-vQouhO4Bb5SI4XDBb1HIXDWJ5XhLKTVSQyoxctw&oe=674B2BFF&_nc_sid=bfaa47",
            "지수"
        )
        // 데이터 업로드
        _response.value = dummyResponse
    }
}