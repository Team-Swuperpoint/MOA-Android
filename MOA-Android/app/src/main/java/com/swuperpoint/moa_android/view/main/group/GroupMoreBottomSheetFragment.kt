package com.swuperpoint.moa_android.view.main.group

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentGroupJoinBottomSheetBinding
import com.swuperpoint.moa_android.databinding.FragmentGroupMoreBottomSheetBinding
import com.swuperpoint.moa_android.view.main.MainActivity

class GroupMoreBottomSheetFragment: BottomSheetDialogFragment() {
    private var mBinding: FragmentGroupMoreBottomSheetBinding? = null

    private val binding get() = mBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentGroupMoreBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // group id 데이터 받기
        val args: GroupMoreBottomSheetFragmentArgs by navArgs()

        // 취소 버튼 클릭 이벤트
        binding.btnGroupMoreBottomSheetCancel.setOnClickListener {
            dismiss()
        }

        // 그룹 수정하기 버튼 클릭 이벤트
        binding.tvGroupMoreBottomSheetGroupModify.setOnClickListener {
            // TODO: 그룹 수정 화면으로 이동
        }

        // 그룹원 수정하기 버튼 클릭 이벤트
        binding.tvGroupMoreBottomSheetMemberModify.setOnClickListener {
            // TODO: 그룹원 목록 화면으로 이동
            val actionToMember = GroupMoreBottomSheetFragmentDirections.actionGroupInfoFrmToMemberFrm(args.groupId)
            findNavController().navigate(actionToMember)
        }

        // 모임 만들기 버튼 클릭 이벤트
        binding.tvGroupMoreBottomSheetGatheringCreate.setOnClickListener {
            // TODO: 모임 만들기 화면으로 이동
        }
    }
}