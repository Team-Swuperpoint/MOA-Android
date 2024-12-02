package com.swuperpoint.moa_android.view.main.group.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressItem(
    val title: String?,       // 장소명, 업체명
    val address: String,     // 지번 주소
    val roadAddress: String, // 도로명 주소
    val longitude: String,   // 경도
    val latitude: String     // 위도
) : Parcelable