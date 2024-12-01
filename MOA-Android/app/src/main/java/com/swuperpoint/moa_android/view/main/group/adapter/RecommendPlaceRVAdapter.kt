package com.swuperpoint.moa_android.view.main.group.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.data.remote.model.group.RecommendPlaceResponse
import com.swuperpoint.moa_android.databinding.ItemGatheringPlaceBinding

/* 중간지점 찾기 화면의 추천 중간지점 RV 어댑터 */
class RecommendPlaceRVAdapter: RecyclerView.Adapter<RecommendPlaceRVViewHolder>() {
    // 추천 중간 지점 리스트
    private lateinit var placeList: List<RecommendPlaceResponse>

    // 중간지점 선택 클릭 이벤트
    var onClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendPlaceRVViewHolder {
        val binding: ItemGatheringPlaceBinding = ItemGatheringPlaceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecommendPlaceRVViewHolder(binding)
    }

    override fun getItemCount(): Int = placeList.size

    override fun onBindViewHolder(holder: RecommendPlaceRVViewHolder, position: Int) {
        val item = placeList[position]
        holder.bind(item, position)

        // 중간지점 레이아웃 클릭 이벤트
        holder.binding.cLayoutItemGatheringPlaceTop.setOnClickListener {
            onClickListener?.invoke(position)
        }
    }

    // 데이터 업데이트
    @SuppressLint("NotifyDataSetChanged")
    fun updatePlaces(placeList: List<RecommendPlaceResponse>) {
        this.placeList = placeList
        notifyDataSetChanged()
    }
}