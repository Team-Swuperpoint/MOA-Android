package com.swuperpoint.moa_android.view.main.timeline

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentTimelineInfoBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.timeline.adapter.TimelineInfoDateRVAdapter
import com.swuperpoint.moa_android.viewmodel.main.timeline.TimelineInfoViewModel
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext

/* 타임라인 정보 화면 */
class TimelineInfoFragment : BaseFragment<FragmentTimelineInfoBinding>(FragmentTimelineInfoBinding::inflate) {
    private val viewModel: TimelineInfoViewModel by viewModels() // 뷰모델
    private var timelineId: String = "" // 타임라인 id
    private var adapter = TimelineInfoDateRVAdapter() // 날짜&사진 리스트 어댑터

    override fun initViewCreated() {
        // 다른 화면에서 전달받은 타임라인id 설정
        val args: TimelineInfoFragmentArgs by navArgs()
        timelineId = args.timelineId

        // 상태바 색상 변경
        changeStatusbarColor(R.color.main_300, false)

        // 삭제 버튼 띄우기
        binding.toolbarTimelineInfo.ivToolbarRight.visibility = View.VISIBLE
        binding.toolbarTimelineInfo.ivToolbarRight.setImageResource(R.drawable.ic_trash_24)

        // 뒤로가기 버튼 색상 변경
        binding.toolbarTimelineInfo.ivToolbarBack.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white))

        // 툴바 색상 변경
        binding.toolbarTimelineInfo.toolbar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.main_300))

        // 바텀 네비게이션 숨기기
        mainActivity?.hideLBNV(true)

        // LiveData 관찰
        observeViewModel()

        // 데이터 로드
        viewModel.fetchTimelineInfo(timelineId)
    }

    override fun initAfterBinding() {
        // 뒤로가기 버튼 클릭 이벤트
        binding.toolbarTimelineInfo.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 삭제 버튼 클릭 이벤트
        binding.toolbarTimelineInfo.ivToolbarRight.setOnClickListener {
            // TODO: 타임라인 삭제

            // TODO: 타임라인 삭제에 성공했다면, 타임라인 화면으로 이동
            val actionToTimeline = TimelineInfoFragmentDirections.actionTimelineInfoFrmToTimelineFrm()
            findNavController().navigate(actionToTimeline)
        }

        // 어댑터 연결
        binding.rvTimelineInfoPhotos.adapter = adapter
    }

    // LiveData 관찰
    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        // 모임 이름 정보 관찰
        viewModel.gatheringName.observe(viewLifecycleOwner) { name ->
            binding.tvTimelineInfoGatheringName.text = name
        }

        // 날짜 정보 관찰
        viewModel.date.observe(viewLifecycleOwner) { date ->
            binding.tvTimelineInfoDate.text = date
        }

        // 모임 중간 지점 장소 정보 관찰
        viewModel.placeName.observe(viewLifecycleOwner) { name ->
            binding.tvTimelineInfoPlace.text = name
        }

        // 그룹이름 정보 관찰
        viewModel.groupName.observe(viewLifecycleOwner) { name ->
            binding.tvTimelineInfoGroupName.text = "with $name"
        }

        // 사진 리스트 정보 관찰
        viewModel.timelineList.observe(viewLifecycleOwner) { list ->
            adapter.updateTimelines(list)
        }
    }
}