package com.swuperpoint.moa_android.view.main.group.adapter

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemGroupGroupBinding
import com.swuperpoint.moa_android.view.main.group.data.GroupItem
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext
import com.swuperpoint.moa_android.R

/* 그룹 화면의 그룹 RV 어댑터 */
class GroupRVViewHolder(val binding: ItemGroupGroupBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: GroupItem) {
        binding.group = item

        // bgColor 인덱스를 실제 색상 리소스 ID로 변환
        val colorResId = when (item.bgColor) {
            0 -> R.color.main_500
            1 -> R.color.sub_300
            2 -> R.color.sub_500
            3 -> R.color.main_50
            4 -> R.color.main_100
            5 -> R.color.sub_200
            else -> R.color.main_500
        }

        binding.tvItemGroupGroupEmoji.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext(), colorResId))
        binding.tvItemGroupGroupMemberNum.text = item.groupMemberNum.toString()
        binding.executePendingBindings()
    }
}