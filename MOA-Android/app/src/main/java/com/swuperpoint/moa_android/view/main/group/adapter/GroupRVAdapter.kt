package com.swuperpoint.moa_android.view.main.group.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemGroupGroupBinding
import com.swuperpoint.moa_android.view.main.group.data.GroupItem

/* 그룹 화면의 그룹 RV 어댑터 */
class GroupRVAdapter(private var groupList: ArrayList<GroupItem>?): RecyclerView.Adapter<GroupRVViewHolder>() {
    // 그룹 클릭
    var onItemClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupRVViewHolder {
        val binding: ItemGroupGroupBinding = ItemGroupGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GroupRVViewHolder(binding)
    }

    override fun getItemCount(): Int = groupList?.size ?: 0

    override fun onBindViewHolder(holder: GroupRVViewHolder, position: Int) {
        val item = groupList?.get(position)
        if (item != null) {
            holder.bind(item)
        }

        // 그룹 아이템 클릭 이벤트
        holder.binding.lLayoutItemGroupGroup.setOnClickListener {
            onItemClickListener?.invoke(position)
        }
    }

    // 아이템 업데이트
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: ArrayList<GroupItem>?) {
        groupList?.clear()
        groupList = newItems
        notifyDataSetChanged()
    }
}