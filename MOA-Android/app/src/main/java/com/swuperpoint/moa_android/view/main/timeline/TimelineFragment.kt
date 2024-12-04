package com.swuperpoint.moa_android.view.main.timeline

import android.util.Log
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentTimelineBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.timeline.adapter.TimelineRVAdapter
import com.swuperpoint.moa_android.viewmodel.main.timeline.TimelineViewModel

/* 타임라인 화면 */
class TimelineFragment : BaseFragment<FragmentTimelineBinding>(FragmentTimelineBinding::inflate) {
    private val viewModel: TimelineViewModel by viewModels() // 뷰모델
    private val timelineAdapter = TimelineRVAdapter() // 타임라인 리스트 어댑터

    override fun initAfterBinding() {
        // 어댑터 연결
        binding.rvTimeline.adapter = timelineAdapter

        // FAB 버튼 클릭 이벤트
        binding.fabTimelineAdd.setOnClickListener {
            try {
                val actionToCreateTimeline = TimelineFragmentDirections.actionTimelineFrmToCreateTimelineFrm()
                findNavController().navigate(actionToCreateTimeline)
            } catch (e: Exception) {
                Log.e("Timeline", "타임라인 생성 화면 이동 중 오류 발생", e)
            }
        }

        // 타임라인 1개 클릭 이벤트
        timelineAdapter.onClickListener = { pos ->
            try {
                val timelineId = timelineAdapter.getTimelineId(pos)
                val actionToTimelineInfo = TimelineFragmentDirections.actionTimelineFrmToTimelineInfoFrm(timelineId)
                findNavController().navigate(actionToTimelineInfo)
            } catch (e: Exception) {
                Log.e("Timeline", "타임라인 상세 화면 이동 중 오류 발생", e)
            }
        }
    }

    override fun initViewCreated() {
        // 바텀 네비게이션 띄우기
        mainActivity?.hideLBNV(false)

        // 상태바 색상 변경
        changeStatusbarColor(R.color.gray_100, isLightMode = true)

        // LiveData 관찰 설정
        viewModel.timelineList.observe(viewLifecycleOwner) { timelines ->
            timelines?.let {
                timelineAdapter.updateTimelines(it)
                binding.tvTimelineCount.text = it.size.toString()
            }
        }

        // 데이터 로드
        viewModel.fetchTimeline()
    }
}