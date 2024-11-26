package com.swuperpoint.moa_android.view.main.group.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.ItemMemberMemberBinding
import com.swuperpoint.moa_android.view.main.group.data.MemberItem
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext

/* 그룹원 목록 화면의 RV 어댑터 */
class MemberRVViewHolder(val binding: ItemMemberMemberBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: MemberItem, position: Int) {
        // 프로필 이미지 로드
        Glide.with(applicationContext())
            .load(item.profileImgURL)
            .fallback(R.color.gray_200)
            .error(R.color.gray_200)
            .into(binding.ivItemMember)

        // 모임짱에게만 테두리 추가
        if (position == 0) {
            binding.fLayoutItemMember.setBackgroundResource(R.drawable.stroke_center)
        }

        // 그룹원 이름
        binding.tvItemMemberName.text = item.memberName

        // deleteBtn 보이기 숨기기
        if (item.isEdit == true) {
            binding.iBtnItemMemberDelete.visibility = View.VISIBLE
        }
        else {
            binding.iBtnItemMemberDelete.visibility = View.INVISIBLE
        }
    }
}