package com.swuperpoint.moa_android.view.main.group

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.swuperpoint.moa_android.databinding.FragmentGroupJoinBottomSheetBinding
import com.swuperpoint.moa_android.view.main.MainActivity

/* 그룹 들어가기 바텀 시트 */
class GroupJoinBottomSheetFragment: BottomSheetDialogFragment() {
    private var mBinding: FragmentGroupJoinBottomSheetBinding? = null

    private val binding get() = mBinding!!
    private var mainActivity: MainActivity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentGroupJoinBottomSheetBinding.inflate(inflater, container, false)
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
            Log.d("GroupBJoinottomSheetFrm - onAttach", e.stackTraceToString())
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
        binding.iBtnGroupJoinBottomSheetCancel.setOnClickListener {
            dismiss()
        }
    }
}