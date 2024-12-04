package com.swuperpoint.moa_android.view.main.timeline.adapter

import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelinePhotoResponse
import com.swuperpoint.moa_android.databinding.ItemTimelineInfoDateBinding

/* 타임라인 정보 화면의 사진 타임라인(날짜) RV 뷰 홀더 */
class TimelineInfoDateRVViewHolder(val binding: ItemTimelineInfoDateBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: TimelinePhotoResponse) {
        binding.data = item
        binding.executePendingBindings()
    }
}