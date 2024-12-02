package com.swuperpoint.moa_android.view.main.group.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemGroupMemberBinding
import com.swuperpoint.moa_android.view.main.group.data.MemberItem

/* 그룹 정보 화면의 그룹원 RV 어댑터 */
class GroupMemberRVAdapter: RecyclerView.Adapter<GroupMemberRVViewHolder>() {
    // 그룹원 리스트 초기화
    private var memberList: List<MemberItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberRVViewHolder {
        val binding: ItemGroupMemberBinding = ItemGroupMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GroupMemberRVViewHolder(binding)
    }

    override fun getItemCount(): Int = memberList.size

    override fun onBindViewHolder(holder: GroupMemberRVViewHolder, position: Int) {
        val item = memberList[position]
        holder.bind(item, position)
    }

    // 그룹원 데이터 업데이트
    @SuppressLint("NotifyDataSetChanged")
    fun updateMembers(memberList: List<MemberItem>) {
        this.memberList = memberList
        notifyDataSetChanged()
    }
}