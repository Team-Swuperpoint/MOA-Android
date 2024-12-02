package com.swuperpoint.moa_android.view.main.group

import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.data.remote.model.group.GroupResponse
import com.swuperpoint.moa_android.databinding.FragmentGroupBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.group.adapter.GroupRVAdapter
import com.swuperpoint.moa_android.viewmodel.main.group.GroupViewModel

/* 그룹 화면 */
class GroupFragment : BaseFragment<FragmentGroupBinding>(FragmentGroupBinding::inflate) {
    private val groupViewModel: GroupViewModel by activityViewModels()
    private lateinit var groupAdapter: GroupRVAdapter  // 어댑터를 클래스 레벨 변수로 선언

    override fun initViewCreated() {
        // 바텀 네비게이션 띄우기
        mainActivity?.hideLBNV(false)

        // 상태바 색상 변경
        changeStatusbarColor(R.color.gray_100, isLightMode = true)
    }

    override fun initAfterBinding() {
        // 어댑터 연결
        groupAdapter = GroupRVAdapter(groupViewModel.groupList.value)
        binding.rvGroupGroup.adapter = groupAdapter

        // 업데이트 관찰
        observe(groupAdapter)

        // 그룹 클릭 이벤트
        groupAdapter.onItemClickListener = { position ->
            groupViewModel.groupList.value?.let { list ->
                if (position < list.size) {
                    val actionToGroupInfo = GroupFragmentDirections.actionGroupFrmToGroupInfoFrm(list[position].groupId)
                    findNavController().navigate(actionToGroupInfo)
                }
            }
        }

        // FAB 버튼 클릭 이벤트
        binding.fabGroupAdd.setOnClickListener {
            // 그룹 바텀 시트 띄우기
            val actionToGroupBottomSheet = GroupFragmentDirections.actionGroupFrmToGroupBottomSheetFrm()
            findNavController().navigate(actionToGroupBottomSheet)
        }
    }

    // 다시 돌아올 때 마다 데이터 새로고침
    override fun onResume() {
        super.onResume()
        groupViewModel.refreshGroups()
    }

    // VM 업데이트 관찰
    private fun observe(adapter: GroupRVAdapter) {
        // 그룹 리스트 업데이트 관찰
        groupViewModel.groupList.observe(viewLifecycleOwner) { items ->
            Log.d("GroupFragment", "Received ${items?.size ?: 0} items")
            if (items != null) { // null 체크 추가
                adapter.updateItems(items)
                binding.tvGroupGroupCount.text = adapter.itemCount.toString() // 리스트 수 업데이트
            }
        }
    }
}