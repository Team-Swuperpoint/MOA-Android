package com.swuperpoint.moa_android.view.main.timeline.adapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelineResponse
import com.swuperpoint.moa_android.databinding.ItemTimelineBinding
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext
import okhttp3.internal.lockAndWaitNanos

/* 타임라인 화면의 타임라인 RV 어댑터 */
class TimelineRVViewHolder(val binding: ItemTimelineBinding): RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(item: TimelineResponse) {
        binding.timeline = item

        // 모임 이름
        binding.tvItemTimelineGatheringName.text = "by " + item.gatheringName

        // 그룹원 인원 수
        binding.tvItemTimelineMemberNum.text = item.groupMemberNum.toString()

        // 타임라인 사진
        Glide.with(applicationContext())
            .load(item.gatheringImgURL)
            .fallback(R.color.gray_200)
            .error(R.color.gray_200)
            .into(binding.ivItemTimelineImg)

        binding.executePendingBindings()
    }
}