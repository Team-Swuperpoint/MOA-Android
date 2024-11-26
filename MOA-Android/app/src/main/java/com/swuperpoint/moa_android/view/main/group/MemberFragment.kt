package com.swuperpoint.moa_android.view.main.group

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentMemberBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.group.adapter.MemberRVAdapter
import com.swuperpoint.moa_android.viewmodel.main.group.MemberViewModel

/* 그룹원 목록 화면 */
class MemberFragment : BaseFragment<FragmentMemberBinding>(FragmentMemberBinding::inflate) {
    private val viewModel: MemberViewModel by viewModels() // 뷰모델
    private val adapter = MemberRVAdapter() // 어댑터
    private var isEdit = false // 편집 모드 여부

    @SuppressLint("SetTextI18n")
    override fun initViewCreated() {
        // 툴 바 이름 설정
        binding.toolbarMember.tvToolbarTitle.text = "그룹원 목록"

        // 상태바 색상 변경
        changeStatusbarColor(R.color.white, isLightMode = true)

        val args: MemberFragmentArgs by navArgs()
        // TODO: 전달받은 groupId(args.groupId)로 파이어베이스에서 그룹원 리스트 검색 -> 전달받은 정보로 화면 구성
        // 데이터 로드
        viewModel.fetchMembers(args.groupId)

        // LiveData 관찰
        observeViewModel()
    }

    override fun initAfterBinding() {
        // 클릭 이벤트
        onClickListener()

        // 그룹원 리스트 어댑터 연결
        binding.rvMemberList.adapter = adapter
    }

    // LiveData 관찰
    private fun observeViewModel() {
        viewModel.memberList.observe(viewLifecycleOwner) { members ->
            adapter.updateMembers(members)
            binding.tvMemberNum.text = adapter.itemCount.toString()
        }
    }

    // 클릭 이벤트
    @SuppressLint("SetTextI18n")
    private fun onClickListener() {
        // 뒤로가기 버튼 클릭 이벤트
        binding.toolbarMember.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 편집 버튼 클릭 이벤트
        binding.tvMemberEdit.setOnClickListener {
            isEdit = !isEdit
            if (isEdit) {
                binding.tvMemberEdit.text = "완료"
                adapter.showDeleteBtn(true)
            } else {
                binding.tvMemberEdit.text = "편집"
                adapter.showDeleteBtn(false)
            }
        }

        // 삭제 버튼 클릭 이벤트
        adapter.onDeleteBtnClickListener = { position ->
            // TODO: 팀원 삭제 api 연결 후, adapter에 있는 데이터 삭제하기
            viewModel.deleteMember(position)
            adapter.showDeleteBtn(false)
            binding.tvMemberNum.text = adapter.itemCount.toString()
            showToast("그룹원을 삭제했습니다")
        }
    }
}