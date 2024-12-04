package com.swuperpoint.moa_android.view.main.timeline.adapter

import android.annotation.SuppressLint
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelineResponse
import com.swuperpoint.moa_android.databinding.ItemTimelineBinding
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext

/* 타임라인 화면의 타임라인 RV 어댑터 */
class TimelineRVViewHolder(val binding: ItemTimelineBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val db = FirebaseFirestore.getInstance()

    fun bind(timeline: TimelineResponse) {
        binding.timeline = timeline

        // 이미지 로딩
        if (!timeline.gatheringImgURL.isNullOrEmpty()) {
            Glide.with(binding.root.context)
                .load(timeline.gatheringImgURL)
                .centerCrop() // scaleType이 centerCrop인 것 처럼 로딩
                .into(binding.ivItemTimelineImg)
        }

        // Firebase에서 실제 멤버 수 가져오기
        db.collection("groups").document(timeline.groupId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val memberUIDs = document.get("memberUIDs") as? List<*>
                    binding.tvItemTimelineMemberNum.text = memberUIDs?.size?.toString() ?: "0"
                }
            }
            .addOnFailureListener { e ->
                binding.tvItemTimelineMemberNum.text = "0"
            }

        binding.executePendingBindings()
    }
}