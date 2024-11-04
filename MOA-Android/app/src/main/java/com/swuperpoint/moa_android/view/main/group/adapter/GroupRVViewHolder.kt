package com.swuperpoint.moa_android.view.main.group.adapter

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemGroupGroupBinding
import com.swuperpoint.moa_android.view.main.group.data.GroupItem
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext

/* 그룹 화면의 그룹 RV 어댑터 */
class GroupRVViewHolder(val binding: ItemGroupGroupBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: GroupItem) {
        binding.group = item
        binding.tvItemGroupGroupEmoji.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext(), item.bgColor))
        binding.tvItemGroupGroupMemberNum.text = item.groupMemberNum.toString()
        binding.executePendingBindings()
    }
}