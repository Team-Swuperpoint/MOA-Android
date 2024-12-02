package com.swuperpoint.moa_android.data.remote.model.group.kakaomap

import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 카카오 맵 API를 사용할 때 필요한 필수 정보를 담아둔 클래스
class KakaoMapClass {
    companion object {
        const val BASE_URL = "https://dapi.kakao.com/"
        val API_KEY = applicationContext().resources.getString(R.string.kakao_rest_api_key)
        val retrofit: Retrofit = Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoMapInterface::class.java)   // 통신 인터페이스를 객체로 생성
    }
}