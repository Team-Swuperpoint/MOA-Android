package com.swuperpoint.moa_android.view.main.timeline

import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentTimelinePhotoBinding
import com.swuperpoint.moa_android.view.base.BaseFragment

/* 타임라인 사진 확대 화면 */
class TimelinePhotoFragment : BaseFragment<FragmentTimelinePhotoBinding>(FragmentTimelinePhotoBinding::inflate) {
    private var photo: String = "" // 선택한 사진

    override fun initViewCreated() {
        // 다른 화면에서 전달받은 타임라인id 설정
        val args: TimelinePhotoFragmentArgs by navArgs()
        photo = args.photo

        // 상태바 색상 변경
        changeStatusbarColor(R.color.black, false)


        // 뒤로가기 버튼 색상 변경
        binding.toolbarTimelinePhoto.ivToolbarBack.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))

        // 툴바 색상 변경
        binding.toolbarTimelinePhoto.toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))

        // 사진 다운로드 버튼 띄우기
        binding.toolbarTimelinePhoto.ivToolbarRight.visibility = View.VISIBLE
        binding.toolbarTimelinePhoto.ivToolbarRight.setImageResource(R.drawable.ic_download_24)
    }

    override fun initAfterBinding() {
        // 뒤로가기 버튼 클릭 이벤트
        binding.toolbarTimelinePhoto.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 다운로드 버튼 클릭 이벤트
        binding.toolbarTimelinePhoto.ivToolbarRight.setOnClickListener {
            // TODO: 갤러리에 사진 저장해야하는데... 시간이 없으니 넘어갈게..!
        }

        // 선택한 사진 띄우기
        Glide.with(requireContext())
            .load(photo)
            .fallback(R.color.gray_200)
            .error(R.color.gray_200)
            .into(binding.ivTimelinePhoto)
    }
}