package com.swuperpoint.moa_android.view.main.group.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemGroupGatheringBinding
import com.swuperpoint.moa_android.view.main.group.data.GatheringItem

/* 그룹 정보 화면의 모임 정보 RV 어댑터 */
class GroupGatheringRVAdapter: RecyclerView.Adapter<GroupGatheringRVViewHolder>() {
    // 모임 리스트
    private lateinit var gatheringList: List<GatheringItem>

    // 모임 버튼 클릭 이벤트
    var onClickListener: ((Int, Long) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupGatheringRVViewHolder {
        val binding: ItemGroupGatheringBinding = ItemGroupGatheringBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GroupGatheringRVViewHolder(binding)
    }

    override fun getItemCount(): Int = gatheringList.size

    override fun onBindViewHolder(holder: GroupGatheringRVViewHolder, position: Int) {
        val item = gatheringList[position]
        holder.bind(item)

        // 모임 레이아웃 클릭 이벤트
        holder.binding.lLayoutItemGroupGathering.setOnClickListener {
            onClickListener?.invoke(position, item.gatheringId)
        }
    }

    // 모임 데이터 업데이트
    @SuppressLint("NotifyDataSetChanged")
    fun updateGatherings(gatheringList: List<GatheringItem>) {
        this.gatheringList = gatheringList
        notifyDataSetChanged()
    }
}