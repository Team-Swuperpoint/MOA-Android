package com.swuperpoint.moa_android.view.main.timeline.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.swuperpoint.moa_android.databinding.ItemCreateTimelinePhotoBinding

class CreateTimelinePhotoRVAdapter: RecyclerView.Adapter<CreateTimelinePhotoRVViewHolder>() {
    // 이미지 리스트
    private var photoList: ArrayList<Uri> = arrayListOf()

    // 이미지 삭제 버튼 클릭 이벤트
    var onClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CreateTimelinePhotoRVViewHolder {
        val binding: ItemCreateTimelinePhotoBinding = ItemCreateTimelinePhotoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CreateTimelinePhotoRVViewHolder(binding)
    }

    override fun getItemCount(): Int = photoList.size

    override fun onBindViewHolder(holder: CreateTimelinePhotoRVViewHolder, position: Int) {
        val item = photoList[position]
        holder.bind(item)

        // 이미지 삭제 버튼 클릭 이벤트
        holder.binding.ivItemCreateTimelinePhotoDelete.setOnClickListener {
            onClickListener?.invoke(position)
        }
    }

    // 데이터 업데이트
    @SuppressLint("NotifyDataSetChanged")
    fun updatePhotos(photoUri: Uri) {
        this.photoList.add(photoUri)
        notifyDataSetChanged()
    }

    // 사진 삭제
    fun deletePhoto(position: Int) {
        this.photoList.removeAt(position)
        notifyItemRemoved(position)
    }

    // 사진 데이터 가져오기
    fun getPhotos(): ArrayList<Uri> {
        return this.photoList
    }
}