package com.swuperpoint.moa_android.view.main.timeline

import androidx.fragment.app.viewModels
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentTimelineBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.timeline.adapter.TimelineRVAdapter
import com.swuperpoint.moa_android.viewmodel.main.timeline.TimelineViewModel

/* 타임라인 화면 */
class TimelineFragment : BaseFragment<FragmentTimelineBinding>(FragmentTimelineBinding::inflate) {
    private val viewModel: TimelineViewModel by viewModels() // 뷰모델
    private var timelineAdapter = TimelineRVAdapter() // 타임라인 리스트 어댑터

    override fun initViewCreated() {
        // 바텀 네비게이션 띄우기
        mainActivity?.hideLBNV(false)

        // 상태바 색상 변경
        changeStatusbarColor(R.color.gray_100, isLightMode = true)

        // LiveData 관찰
        observeViewModel()

        // 데이터 로드
        viewModel.fetchTimeline()
    }

    override fun initAfterBinding() {
        // 어댑터 연결
        binding.rvTimeline.adapter = timelineAdapter

        // FAB 버튼 클릭 이벤트
        binding.fabTimelineAdd.setOnClickListener {
            // TODO: 타임라인 만들기 화면으로 이동
        }

        // 타임라인 1개 클릭 이벤트
        timelineAdapter.onClickListener = { pos ->
            // TODO: 타임라인 정보 화면으로 이동
        }
    }

    // LiveData 관찰
    private fun observeViewModel() {
        // 타임라인 리스트 정보 관찰
        viewModel.timelineList.observe(viewLifecycleOwner) { timelines ->
            if (timelines != null) {
                timelineAdapter.updateTimelines(timelines)
                binding.tvTimelineCount.text = timelineAdapter.itemCount.toString()
            }
        }
    }
}