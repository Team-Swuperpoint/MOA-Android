package com.swuperpoint.moa_android.view.main.group.adapter

import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemAddressBinding
import com.swuperpoint.moa_android.view.main.group.data.AddressItem

/* 주소 검색 화면의 RV 뷰홀더 */
class AddressRVViewHolder(val binding: ItemAddressBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: AddressItem) {
        val title = binding.tvItemAddressPlaceName // 장소명
        val address = binding.tvItemAddressRoadAddress // 도로명 주소 또는 지번주소

        // 뷰에 데이터 연결
        title.text = item.title
        if (item.roadAddress == null || item.roadAddress.isBlank()) {
            address.text = item.address
        }
        else {
            address.text = item.roadAddress
        }
    }
}