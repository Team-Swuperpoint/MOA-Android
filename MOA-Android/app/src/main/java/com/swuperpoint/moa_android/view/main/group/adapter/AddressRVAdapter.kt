package com.swuperpoint.moa_android.view.main.group.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemAddressBinding
import com.swuperpoint.moa_android.view.main.group.data.AddressItem

/* 주소 검색 화면의 RV 어댑터 */
class AddressRVAdapter(val addressList: ArrayList<AddressItem>): RecyclerView.Adapter<AddressRVViewHolder>() {
    // 출발 장소 선택 클릭 이벤트
    var onClickListener: ((AddressItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressRVViewHolder {
        val binding: ItemAddressBinding = ItemAddressBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AddressRVViewHolder(binding)
    }

    override fun getItemCount(): Int = addressList.size

    override fun onBindViewHolder(holder: AddressRVViewHolder, position: Int) {
        val item = addressList[position]
        holder.bind(item)

        // 주소 클릭 이벤트
        holder.binding.lLayoutItemAddress.setOnClickListener {
            onClickListener?.invoke(addressList[position])
        }
    }
}