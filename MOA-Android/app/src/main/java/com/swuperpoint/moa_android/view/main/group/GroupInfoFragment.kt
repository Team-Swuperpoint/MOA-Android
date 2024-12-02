package com.swuperpoint.moa_android.view.main.group

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentGroupInfoBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.group.adapter.GroupGatheringRVAdapter
import com.swuperpoint.moa_android.view.main.group.adapter.GroupMemberRVAdapter
import com.swuperpoint.moa_android.viewmodel.main.group.GroupInfoViewModel

/* 그룹 정보 화면 */
class GroupInfoFragment : BaseFragment<FragmentGroupInfoBinding>(FragmentGroupInfoBinding::inflate) {
    private val viewModel: GroupInfoViewModel by viewModels() // 뷰모델
    private var memberAdapter = GroupMemberRVAdapter() // 그룹원 리스트 어댑터
    private var gatheringAdapter = GroupGatheringRVAdapter() // 모임 리스트 어댑터
    private var groupId: String = "" // 그룹 id

    override fun initViewCreated() {
        // 바텀 네비게이션 숨기기
        mainActivity?.hideLBNV(true)

        // 그룹 나가기 버튼 띄우기
        binding.toolbarCreateGroup.ivToolbarRight.visibility = View.VISIBLE

        // 상태바 색상 변경
        changeStatusbarColor(R.color.gray_200, isLightMode = true)

        // initAfterBinding()에 있던 거 여기로 이동시킴
        // 그룹원 리스트 어댑터 연결
        binding.rvGroupInfoMember.adapter = memberAdapter
        // 모임 리스트 어댑터 연결
        binding.rvGroupInfoGathering.adapter = gatheringAdapter

        val args: GroupInfoFragmentArgs by navArgs()
        groupId = args.groupId
        // TODO: 전달받은 groupId(args.groupId)로 파이어베이스에서 그룹 정보 검색 -> 전달받은 정보로 화면 구성

        // LiveData 관찰 위치 이동 -> 데이터 로드보다 먼저
        observeViewModel()

        // 데이터 로드
        viewModel.fetchGroupInfo(groupId)
    }

    override fun initAfterBinding() {
        // 클릭 이벤트
        onClickListener()
    }

    // LiveData 관찰
    private fun observeViewModel() {
        // 이모지 정보 관찰
        viewModel.bgColor.observe(viewLifecycleOwner) { color ->
            binding.tvGroupInfoEmoji.backgroundTintList = ContextCompat.getColorStateList(requireContext(), color)
        }
        viewModel.emoji.observe(viewLifecycleOwner) { emoji ->
            binding.tvGroupInfoEmoji.text = emoji
        }

        // 그룹 이름 관찰
        viewModel.groupName.observe(viewLifecycleOwner) { name ->
            binding.tvGroupInfoTitle.text = name
        }

        // 최근 모임 정보 관찰
        viewModel.recentGathering.observe(viewLifecycleOwner) { date ->
            binding.tvGroupInfoRecentDate.text = date
        }

        // 그룹 코드 정보 관찰
        viewModel.groupCode.observe(viewLifecycleOwner) { code ->
            binding.tvGroupInfoCode.text = code
        }

        // 그룹원 리스트 정보 관찰
        viewModel.memberList.observe(viewLifecycleOwner) { members ->
            memberAdapter.updateMembers(members)
            binding.tvGroupInfoMemberNum.text = memberAdapter.itemCount.toString()
        }

        // 함께 한 모임 정보 관찰
        viewModel.gatheringList.observe(viewLifecycleOwner) { gather ->
            if (gather != null) {
                gatheringAdapter.updateGatherings(gather)
                binding.tvGroupInfoGatheringNum.text = gatheringAdapter.itemCount.toString()
            }
        }
    }

    // 클릭 이벤트
    private fun onClickListener() {
        // 뒤로가기 버튼 클릭 이벤트
        binding.toolbarCreateGroup.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 그룹 나가기 버튼 클릭 이벤트
        binding.toolbarCreateGroup.ivToolbarRight.setOnClickListener {
            // TODO: 그룹 나가기 API 연동
        }

        // 더보기 버튼 클릭 이벤트
        binding.iBtnGroupInfoMore.setOnClickListener {
            // 그룹 더보기 바텀 시트 띄우기
            val actionToGroupMoreBottomSheet = GroupInfoFragmentDirections.actionGroupInfoFrmToGroupMoreBottomSheetFrm(groupId)
            findNavController().navigate(actionToGroupMoreBottomSheet)
        }

        // 코드 복사 버튼 클릭 이벤트
        binding.btnGroupInfoCodeCopy.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("GROUP CODE", binding.tvGroupInfoCode.text.toString())
            clipboard.setPrimaryClip(clip)
        }

        // 모임 버튼 클릭 이벤트
        gatheringAdapter.onClickListener = { position, gatheringId ->
            // 모임 정보 화면으로 이동
            val actionToGatheringInfo = GroupInfoFragmentDirections.actionGroupInfoFrmToGatheringInfoFrm(gatheringId)
            findNavController().navigate(actionToGatheringInfo)
        }
    }
}