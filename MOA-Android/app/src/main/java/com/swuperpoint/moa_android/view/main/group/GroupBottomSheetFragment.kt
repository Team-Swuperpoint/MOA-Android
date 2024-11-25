package com.swuperpoint.moa_android.view.main.group

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.swuperpoint.moa_android.databinding.FragmentGroupBottomSheetBinding
import com.swuperpoint.moa_android.view.main.MainActivity

/* 그룹 화면 바텀 시트 */
class GroupBottomSheetFragment: BottomSheetDialogFragment() {
    private var mBinding: FragmentGroupBottomSheetBinding? = null
    private val binding get() = mBinding!!
    private var mainActivity: MainActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentGroupBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 네비게이션 바 나타내기
        mainActivity?.hideLBNV(false)

        mBinding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mainActivity = context as MainActivity
        } catch (e: java.lang.Exception) {
            Log.d("GroupBottomSheetFrm - onAttach", e.stackTraceToString())
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 네비게이션 바 숨기기
        mainActivity?.hideLBNV(true)

        // 취소 버튼 클릭 이벤트
        binding.btnGroupBottomSheetCancel.setOnClickListener {
            dismiss()
        }

        // 그룹 들어가기 버튼 클릭 이벤트
        binding.tvGroupBottomSheetGroupJoin.setOnClickListener {
            // 그룹 들어가기 바텀 시트 띄우기
            val actionToGroupJoinBottomSheet = GroupBottomSheetFragmentDirections.actionGroupBottomSheetFrmToGroupJoinBottomSheetFrm()
            findNavController().navigate(actionToGroupJoinBottomSheet)
        }

        // 그룹 만들기 버튼 클릭 이벤트
        binding.tvGroupBottomSheetGroupCreate.setOnClickListener {
            val actionToCreateGroup = GroupBottomSheetFragmentDirections.actionGroupBottomSheetFrmToCreateGroupFrm()
            findNavController().navigate(actionToCreateGroup)
        }
    }
}