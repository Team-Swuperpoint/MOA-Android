package com.swuperpoint.moa_android.view.main.timeline.adapter

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.data.remote.model.timeline.TimelinePhotoResponse
import com.swuperpoint.moa_android.databinding.ItemTimelineInfoDateBinding
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext

/* 타임라인 정보 화면의 사진 타임라인(날짜) RV 어댑터 */
class TimelineInfoDateRVAdapter: RecyclerView.Adapter<TimelineInfoDateRVViewHolder>() {
    private var timelineList = ArrayList<TimelinePhotoResponse>()
    private lateinit var photoAdapter: TimelineInfoPhotoRVAdapter

    // 사진 클릭 이벤트
    var onClickListener: ((String) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimelineInfoDateRVViewHolder {
        val binding: ItemTimelineInfoDateBinding = ItemTimelineInfoDateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TimelineInfoDateRVViewHolder(binding)
    }

    override fun getItemCount(): Int = timelineList.size

    override fun onBindViewHolder(holder: TimelineInfoDateRVViewHolder, position: Int) {
        holder.bind(timelineList[position])

        // 사진 어댑터 연결
        photoAdapter = TimelineInfoPhotoRVAdapter(timelineList[position].photoList)

        // 사진이 1개일 때
        if (timelineList[position].photoList.size == 1) {
            holder.binding.rvItemTimelineInfoPhoto.layoutManager = GridLayoutManager(applicationContext(), 1)
            holder.binding.rvItemTimelineInfoPhoto.addItemDecoration(SingleItemDecoration()) // 커스텀 데코레이션
        }
        // 사진이 2개 이상일 때
        else {
            holder.binding.rvItemTimelineInfoPhoto.layoutManager = GridLayoutManager(applicationContext(), 2)
            holder.binding.rvItemTimelineInfoPhoto.addItemDecoration(GridSpacingItemDecoration(2, 16)) // 기존 데코레이션
        }

        holder.binding.rvItemTimelineInfoPhoto.adapter = photoAdapter

        // 사진 클릭 이벤트(pos? 사진의 position)
        photoAdapter.onClickListener = { pos ->
            onClickListener?.invoke(timelineList[position].photoList[pos])
        }
    }

    // 데이터 업데이트
    @SuppressLint("NotifyDataSetChanged")
    fun updateTimelines(timelineList: ArrayList<TimelinePhotoResponse>) {
        this.timelineList = timelineList
        notifyDataSetChanged()
    }
}

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount

        if (position < spanCount) {
            outRect.top = spacing
        }
        outRect.bottom = spacing
    }
}

class SingleItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // 여백 설정 없음
        outRect.set(0, 0, 0, 0)
    }
}