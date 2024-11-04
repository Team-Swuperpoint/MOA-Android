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
        val adapter = HomeGroupRVAdapter(homeViewModel.groupList.value)
        binding.rvHomeGroup.adapter = adapter

        // 업데이트 관찰
        observe(adapter)

        // 샘플 데이터
        // TODO: 파이어베이스 연결시 삭제
        val sampleResponse = HomeResponse(
            nickname = "윤지",
            groupList = homeViewModel.fetchGroupList(),
            groupInfo = homeViewModel.fetchGatheringInfo()
        )
        homeViewModel.setHomeResponse(sampleResponse)

        // 이전 모임으로 이동
        binding.iBtnHomePrev.setOnClickListener {

        }

        // 다음 모임으로 이동
        binding.iBtnHomeNext.setOnClickListener {
            
        }
    }

    // VM 업데이트 관찰
    private fun observe(adapter: HomeGroupRVAdapter) {
        // 모임 리스트 업데이트 관찰
        homeViewModel.groupList.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
        }

        // 닉네임 업데이트 관찰
        homeViewModel.nickname.observe(viewLifecycleOwner) { nickname ->
            binding.tvHomeNickname.text = nickname
        }
    }
}