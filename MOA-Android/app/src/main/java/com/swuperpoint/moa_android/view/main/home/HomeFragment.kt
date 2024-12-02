package com.swuperpoint.moa_android.view.main.home

import android.content.Context
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.home.HomeResponse
import com.swuperpoint.moa_android.databinding.FragmentHomeBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.home.adapter.HomeGroupRVAdapter
import com.swuperpoint.moa_android.viewmodel.main.home.HomeViewModel

/* 홈 화면 */
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private lateinit var callback: OnBackPressedCallback // 뒤로가기 콜백

    private val homeViewModel: HomeViewModel by activityViewModels() // Activity 스코프 사용

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // 핸드폰 뒤로가기 이벤트
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mainActivity!!.getBackPressedEvent()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove() // 콜백 제거
    }

    override fun initViewCreated() {
        // 바텀 네비게이션 띄우기
        mainActivity?.hideLBNV(false)

        // 상태바 색상 변경
        changeStatusbarColor(R.color.main_300, isLightMode = false)
    }

    override fun initAfterBinding() {
        // 어댑터 연결
        val adapter = HomeGroupRVAdapter(ArrayList())  // null 대신 빈 ArrayList 전달
        binding.rvHomeGroup.adapter = adapter

        // 업데이트 관찰
        observe(adapter)

        // 이전 모임으로 이동
        binding.iBtnHomePrev.setOnClickListener {
            homeViewModel.moveToPrevGathering()
        }

        // 다음 모임으로 이동
        binding.iBtnHomeNext.setOnClickListener {
            homeViewModel.moveToNextGathering()
        }
    }

    // VM 업데이트 관찰
    private fun observe(adapter: HomeGroupRVAdapter) {
        // 모임 리스트 업데이트 관찰
        homeViewModel.groupList.observe(viewLifecycleOwner) { items ->
            items?.let { adapter.updateItems(it) }  // null check 추가
        }

        // 닉네임 업데이트 관찰
        homeViewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            binding.tvHomeNickname.text = nickname ?: ""  // null check 추가
        }

        // 모임 정보 업데이트 관찰
        homeViewModel.gatheringInfo.observe(viewLifecycleOwner) { gatheringInfo ->
            if (gatheringInfo == null) {
                // null일 경우 빈 값으로 설정
                binding.tvHomeGroupName.text = ""
                binding.tvHomeGatheringName.text = ""
                binding.tvHomeTime.text = ""
                binding.tvHomeLocation.text = ""
                binding.tvHomeDday.text = ""
                return@observe
            }

            // null이 아닐 경우에만 값 설정
            binding.tvHomeGroupName.text = gatheringInfo.groupName
            binding.tvHomeGatheringName.text = gatheringInfo.gatheringName
            binding.tvHomeTime.text = gatheringInfo.date
            binding.tvHomeLocation.text = gatheringInfo.location
            binding.tvHomeDday.text = gatheringInfo.dDay.toString()
        }

        adapter.onItemClickListener = { position ->
            homeViewModel.fetchSelectedGroupGathering(position)
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.fetchHomeData()  // HomeViewModel에 fetchHomeData를 public으로 변경
    }
}