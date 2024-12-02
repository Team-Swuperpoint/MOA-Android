package com.swuperpoint.moa_android.view.main.timeline.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.ItemTimelineInfoPhotoBinding
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext

/* 타임라인 정보 화면의 사진 타임라인(사진) RV 뷰홀더 */
class TimelineInfoPhotoRVViewHolder(val binding: ItemTimelineInfoPhotoBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: String) {
        // 이미지 로드
        Glide.with(applicationContext())
            .load(item)
            .fallback(R.color.gray_200)
            .error(R.color.gray_200)
            .into(binding.ivItemTimelineInfoPhoto)
    }
}