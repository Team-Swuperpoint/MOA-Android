package com.swuperpoint.moa_android.view.main.group.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.ItemGroupMemberBinding
import com.swuperpoint.moa_android.view.main.group.data.MemberItem
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext

/* 그룹 정보 화면의 그룹원 RV 어댑터 */
class GroupMemberRVViewHolder(val binding: ItemGroupMemberBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: MemberItem, position: Int) {
        binding.member = item

        // 프로필 이미지 로드
        Glide.with(applicationContext())
            .load(item.profileImgURL)
            .fallback(R.color.gray_200)
            .error(R.color.gray_200)
            .into(binding.ivItemGroupMember)

        // 모임짱에게만 테두리 추가
        if (position == 0) {
            binding.fLayoutItemGroupMember.setBackgroundResource(R.drawable.stroke_center)
        }

        binding.executePendingBindings()
    }
}
