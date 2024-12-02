package com.swuperpoint.moa_android.view.main.home.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemHomeGroupBinding
import com.swuperpoint.moa_android.view.main.home.data.HomeGroupItem

/* 홈 화면의 그룹 RV 어댑터 */
class HomeGroupRVAdapter(
    private var groupList: ArrayList<HomeGroupItem> = ArrayList() // 기본값으로 null 대신 빈 ArrayList 사용
): RecyclerView.Adapter<HomeGroupRVViewHolder>() {
    // 그룹 클릭
    var onItemClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeGroupRVViewHolder {
        val binding: ItemHomeGroupBinding = ItemHomeGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HomeGroupRVViewHolder(binding)
    }

    override fun getItemCount(): Int = groupList?.size ?: 0

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: HomeGroupRVViewHolder, position: Int) {
        val item = groupList?.get(position)
        if (item != null) {
            holder.bind(item)
        }

        holder.binding.lLayoutItemHomeGroup.setOnClickListener {
            val previousSelectedPosition = groupList?.indexOfFirst { it.isSelected }
            // 선택 해제된 항목과 새로 선택된 항목만 갱신
            if (previousSelectedPosition != -1 && previousSelectedPosition != position) {
                groupList!![previousSelectedPosition!!].isSelected = false
                notifyItemChanged(previousSelectedPosition) // 이전 선택 항목 갱신
            }

            item!!.isSelected = true
            notifyItemChanged(position) // 새로 선택된 항목 갱신

            onItemClickListener?.invoke(position)
        }
    }

    // 아이템 업데이트
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: ArrayList<HomeGroupItem>) {
        groupList = newItems
        notifyDataSetChanged()
    }
}