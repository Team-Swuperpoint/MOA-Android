package com.swuperpoint.moa_android.data.remote.model.group.kakaomap

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoMapInterface {
    // 키워드로 검색
    @GET("v2/local/search/keyword.json")        // Keyword.json의 정보 받기
    fun getSearchResult(
        @Header("Authorization") key: String,   // 카카오 REST API 인증 키
        @Query("query") query: String           // 검색을 원하는 질의어 (검색 조건)
    ): Call<SearchAddressResponse>              // 만든 데이터 클래스에 받아온 정보를 저장

    // 좌표로 주소 검색 추가
    @GET("v2/local/geo/coord2address.json")
    fun getAddressFromCoords(
        @Header("Authorization") key: String,
        @Query("x") longitude: String,
        @Query("y") latitude: String,
        @Query("input_coord") inputCoord: String = "WGS84" // 좌표계 타입
    ): Call<Coord2AddressResponse>
}