package com.swuperpoint.moa_android.data.remote.model.group.kakaomap

import com.google.gson.annotations.SerializedName

/* 카카오 맵 API 검색 응답 모델 */
data class SearchAddressResponse(
    var documents: ArrayList<PlaceResponse> // 검색 결과
)

data class PlaceResponse(
    @SerializedName(value = "place_name") var place_name: String,                // 장소명, 업체명
    @SerializedName(value = "address_name") var address_name: String,            // 지번 주소
    @SerializedName(value = "road_address_name") var road_address_name: String,  // 전체 도로명 주소
    @SerializedName(value = "x") var x: String,                                  // 경도
    @SerializedName(value = "y") var y: String                                   // 위도
)
