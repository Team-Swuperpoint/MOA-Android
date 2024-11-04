package com.swuperpoint.moa_android.view.main.home.adapter

import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemHomeGroupBinding
import com.swuperpoint.moa_android.view.main.home.data.HomeGroupItem

/* 홈 화면의 그룹 RV 뷰홀더 */
class HomeGroupRVViewHolder(val binding: ItemHomeGroupBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: HomeGroupItem) {
        binding.group = item
        binding.executePendingBindings() //  렌더링 이후 바뀐 값 적용
    }
}