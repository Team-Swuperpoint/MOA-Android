package com.swuperpoint.moa_android.view.main.group

import androidx.fragment.app.activityViewModels
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.group.GroupResponse
import com.swuperpoint.moa_android.databinding.FragmentGroupBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.group.adapter.GroupRVAdapter
import com.swuperpoint.moa_android.viewmodel.main.group.GroupViewModel

/* 그룹 화면 */
class GroupFragment : BaseFragment<FragmentGroupBinding>(FragmentGroupBinding::inflate) {
    private val groupViewModel: GroupViewModel by activityViewModels()

    override fun initViewCreated() {
        // 바텀 네비게이션 띄우기
        mainActivity?.hideLBNV(false)

        // 상태바 색상 변경
        changeStatusbarColor(R.color.gray_100, isLightMode = true)
    }

    override fun initAfterBinding() {
        // 어댑터 연결
        val adapter = GroupRVAdapter(groupViewModel.groupList.value)
        binding.rvGroupGroup.adapter = adapter

        // 업데이트 관찰
        observe(adapter)

        // 더미 데이터 적용
        val sampleResponse = arrayListOf(
            GroupResponse(R.color.main_500, "🍔", "먹짱친구들", 5, "1일 전"),
            GroupResponse(R.color.sub_300, "🐶", "강쥐산책모임", 2, "12일 전"),
            GroupResponse(R.color.sub_500, "✈️", "여행", 4, "10월 30일"),
        )
        groupViewModel.setGroupResponse(sampleResponse)

        // 그룹 클릭 이벤트
        adapter.onItemClickListener = { position ->
            showToast("$position 번째 그룹 클릭!")
        }
    }

    // VM 업데이트 관찰
    private fun observe(adapter: GroupRVAdapter) {
        // 그룹 리스트 업데이트 관찰
        groupViewModel.groupList.observe(viewLifecycleOwner) { items ->
            adapter.updateItems(items)
            binding.tvGroupGroupCount.text = adapter.itemCount.toString() // 리스트 수 업데이트
        }
    }
}