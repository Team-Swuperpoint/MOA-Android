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
    private val args: MemberFragmentArgs by navArgs() // groupId를 클래스 레벨로 이동

    @SuppressLint("SetTextI18n")
    override fun initViewCreated() {
        // 툴 바 이름 설정
        binding.toolbarMember.tvToolbarTitle.text = "그룹원 목록"

        // 상태바 색상 변경
        changeStatusbarColor(R.color.white, isLightMode = true)

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
        // 멤버 목록 관찰
        viewModel.memberList.observe(viewLifecycleOwner) { members ->
            adapter.updateMembers(members)
            binding.tvMemberNum.text = adapter.itemCount.toString()
        }

        // 삭제 결과 관찰
        viewModel.deleteResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                adapter.showDeleteBtn(false)
                binding.tvMemberEdit.text = "편집"
                isEdit = false
                showToast("그룹원을 삭제했습니다")
            } else {
                showToast("삭제에 실패했습니다")
            }
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
            showDeleteConfirmDialog(position)
        }
    }

    // 삭제 확인 다이얼로그
    private fun showDeleteConfirmDialog(position: Int) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("그룹원 삭제")
            .setMessage("정말로 이 그룹원을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                viewModel.deleteMember(args.groupId, position)
            }
            .setNegativeButton("취소", null)
            .show()
    }
}