package com.swuperpoint.moa_android.view.main.timeline.adapter

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.data.remote.model.group.kakaomap.Coord2AddressResponse
import com.swuperpoint.moa_android.data.remote.model.group.kakaomap.KakaoMapClass
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelinePhotoResponse
import com.swuperpoint.moa_android.databinding.ItemTimelineInfoDateBinding

class TimelineInfoDateRVViewHolder(val binding: ItemTimelineInfoDateBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: TimelinePhotoResponse) {
        binding.data = item

        if (item.latitude != null && item.longitude != null
            && item.latitude != 0.0 && item.longitude != 0.0) {

            KakaoMapClass.api.getAddressFromCoords(
                key = KakaoMapClass.API_KEY,
                longitude = item.longitude.toString(),
                latitude = item.latitude.toString()
            ).enqueue(object : retrofit2.Callback<Coord2AddressResponse> {
                override fun onResponse(
                    call: retrofit2.Call<Coord2AddressResponse>,
                    response: retrofit2.Response<Coord2AddressResponse>
                ) {
                    val locationName = response.body()?.documents?.firstOrNull()?.let { document ->
                        when {
                            // 1. 건물명이 있는 경우
                            document.road_address?.building_name?.isNotEmpty() == true ->
                                document.road_address.building_name

                            // 2. 건물명이 없는 경우 동(region_3depth_name)을 표시
                            document.address?.region_3depth_name?.isNotEmpty() == true -> {
                                val dong = document.address.region_3depth_name
                                // 번지수가 있으면 함께 표시
                                if (document.address.address_name.contains(dong)) {
                                    val number = document.address.address_name.substringAfter(dong).trim()
                                    "$dong $number"
                                } else {
                                    dong
                                }
                            }

                            // 3. 도로명 주소가 있는 경우
                            document.road_address?.road_name?.isNotEmpty() == true ->
                                document.road_address.road_name

                            // 4. 그 외의 경우
                            else -> "위치 정보 없음"
                        }
                    } ?: "위치 정보 없음"

                    binding.tvItemTimelineInfoPlace.text = locationName
                }

                override fun onFailure(call: retrofit2.Call<Coord2AddressResponse>, t: Throwable) {
                    Log.e("Timeline", "카카오맵 API 호출 실패", t)
                    binding.tvItemTimelineInfoPlace.text = "위치 정보 없음"
                }
            })
        } else {
            binding.tvItemTimelineInfoPlace.text = "위치 정보 없음"
        }

        binding.executePendingBindings()
    }
}