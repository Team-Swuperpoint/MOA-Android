package com.swuperpoint.moa_android.view.main.timeline.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelineResponse
import com.swuperpoint.moa_android.databinding.ItemTimelineBinding

/* 타임라인 화면의 타임라인 리스트 RV 어댑터 */
class TimelineRVAdapter: RecyclerView.Adapter<TimelineRVViewHolder>() {
    // 타임라인 리스트
    private lateinit var timelineList: List<TimelineResponse>

    // 타임라인 선택 클릭 이벤트
    var onClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineRVViewHolder {
        val binding: ItemTimelineBinding = ItemTimelineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TimelineRVViewHolder(binding)
    }

    override fun getItemCount(): Int = timelineList.size

    override fun onBindViewHolder(holder: TimelineRVViewHolder, position: Int) {
        val item = timelineList[position]
        holder.bind(item)

        // 타임라인 레이아웃 1개 클릭 이벤트
        holder.binding.cLayoutItemTimeline.setOnClickListener {
            onClickListener?.invoke(position)
        }
    }

    // 데이터 업데이트
    @SuppressLint("NotifyDataSetChanged")
    fun updateTimelines(timelineList: List<TimelineResponse>) {
        this.timelineList = timelineList
        notifyDataSetChanged()
    }

    // 타임라인 id 반환
    fun getTimelineId(position: Int): String {
        return timelineList[position].timelineId
    }
}