package com.swuperpoint.moa_android.view.main.group.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.data.remote.model.group.RecommendPlaceResponse
import com.swuperpoint.moa_android.databinding.ItemGatheringPlaceBinding

/* 중간지점 찾기 화면의 추천 중간지점 RV 뷰홀더 */
class RecommendPlaceRVViewHolder(val binding: ItemGatheringPlaceBinding): RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(item: RecommendPlaceResponse, position: Int) {
        binding.place = item

        // 번호 추가
        binding.tvItemGatheringPlaceIndex.text = (position + 1).toString()

        binding.executePendingBindings()
    }
}