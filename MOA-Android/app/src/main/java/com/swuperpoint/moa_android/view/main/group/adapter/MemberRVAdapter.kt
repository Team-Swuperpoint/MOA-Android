package com.swuperpoint.moa_android.view.main.group.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemMemberMemberBinding
import com.swuperpoint.moa_android.view.main.group.data.MemberItem

/* 그룹원 목록 화면의 RV 어댑터 */
class MemberRVAdapter: RecyclerView.Adapter<MemberRVViewHolder>() {
    // 그룹원 리스트
    private lateinit var memberList: MutableList<MemberItem>

    // 삭제 버튼 클릭
    var onDeleteBtnClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberRVViewHolder {
        val binding: ItemMemberMemberBinding = ItemMemberMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MemberRVViewHolder(binding)
    }

    override fun getItemCount(): Int = memberList.size

    override fun onBindViewHolder(holder: MemberRVViewHolder, position: Int) {
        val item = memberList[position]
        holder.bind(item, position)

        // 취소 버튼 클릭 이벤트
        holder.binding.iBtnItemMemberDelete.setOnClickListener {
            onDeleteBtnClickListener?.invoke(position)
        }
    }

    // 그룹원 데이터 업데이트
    @SuppressLint("NotifyDataSetChanged")
    fun updateMembers(memberList: List<MemberItem>) {
        this.memberList = memberList.toMutableList()
        notifyDataSetChanged()
    }

    // 삭제버튼 숨기기&나타내기
    @SuppressLint("NotifyDataSetChanged")
    fun showDeleteBtn(isEdit: Boolean) {
        if (isEdit) {
            for (element in memberList) {
                element.isEdit = true
            }
        } else {
            for (element in memberList) {
                element.isEdit = false
            }
        }
        notifyDataSetChanged()
    }
}