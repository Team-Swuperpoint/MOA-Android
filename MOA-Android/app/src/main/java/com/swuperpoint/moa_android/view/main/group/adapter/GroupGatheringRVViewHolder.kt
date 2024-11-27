package com.swuperpoint.moa_android.view.main.group.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.ItemGroupGatheringBinding
import com.swuperpoint.moa_android.view.main.group.data.GatheringItem
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext

/* 그룹 정보 화면의 모임 정보 RV 어댑터*/
class GroupGatheringRVViewHolder(val binding: ItemGroupGatheringBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: GatheringItem) {
        binding.gathering = item

        // 모임 이미지 로드
        Glide.with(applicationContext())
            .load(item.gatheringImgURL)
            .fallback(R.color.gray_200)
            .error(R.color.gray_200)
            .into(binding.ivItemGroupGatheringImage)

        binding.executePendingBindings()
    }
}