package com.swuperpoint.moa_android.view.main.timeline.adapter

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemCreateTimelinePhotoBinding

/* 타임라인 만들기 화면의 이미지 리스트 RV 뷰홀더 */
class CreateTimelinePhotoRVViewHolder(val binding: ItemCreateTimelinePhotoBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Uri) {
        // 사진
        binding.ivItemCreateTimelinePhoto.setImageURI(item)
    }
}