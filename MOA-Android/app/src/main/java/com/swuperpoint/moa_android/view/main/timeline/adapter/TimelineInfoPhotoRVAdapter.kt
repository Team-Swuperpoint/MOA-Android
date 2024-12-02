package com.swuperpoint.moa_android.view.main.timeline.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemTimelineInfoPhotoBinding

/* 타임라인 정보 화면의 사진 타임라인(사진) RV 어댑터 */
class TimelineInfoPhotoRVAdapter(private val photoList: ArrayList<String>): RecyclerView.Adapter<TimelineInfoPhotoRVViewHolder>() {
    // 사진 클릭 이벤트
    var onClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimelineInfoPhotoRVViewHolder {
        val binding: ItemTimelineInfoPhotoBinding = ItemTimelineInfoPhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TimelineInfoPhotoRVViewHolder(binding)
    }

    override fun getItemCount(): Int = photoList.size

    override fun onBindViewHolder(holder: TimelineInfoPhotoRVViewHolder, position: Int) {
        holder.bind(photoList[position])

        if (itemCount == 1) {
            val layoutParams = holder.binding.cvItemTimelineInfoPhoto.layoutParams
            layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT
            layoutParams.height = 400
            holder.binding.cvItemTimelineInfoPhoto.layoutParams = layoutParams
        }

        // 사진 클릭 이벤트
        holder.binding.cvItemTimelineInfoPhoto.setOnClickListener {
            onClickListener?.invoke(position)
        }
    }
}