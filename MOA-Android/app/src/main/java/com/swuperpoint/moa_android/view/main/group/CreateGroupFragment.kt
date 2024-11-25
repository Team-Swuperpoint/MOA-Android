package com.swuperpoint.moa_android.view.main.group

import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentCreateGroupBinding
import com.swuperpoint.moa_android.view.base.BaseFragment

/* 그룹 추가 화면 */
class CreateGroupFragment : BaseFragment<FragmentCreateGroupBinding>(FragmentCreateGroupBinding::inflate) {
    // 추가하기 버튼 활성화 여부
    private var isEnable = false

    override fun initViewCreated() {
        // 바텀 네비게이션 숨기기
        mainActivity?.hideLBNV(true)
    }

    override fun initAfterBinding() {
        // 클릭 이벤트
        clickEvent()

        // EditText에 이모지만 입력할 수 있도록 필터 적용
        binding.edtCreateGroupEmoji.filters = arrayOf(CustomEmojiFilter())

        // 모든 값을 입력했다면 추가하기 버튼 활성화
        binding.edtCreateGroupTitle.doOnTextChanged { text, start, before, count ->
            if (text.toString().isNotEmpty()) {
                // 그룹 추가 가능
                isEnable = true

                // UI 변경
                updateComponentUI()
            }
            else {
                // 그룹 추가 불가능
                isEnable = false

                // UI 변경
                updateComponentUI()
            }
        }
    }

    // 클릭 이벤트
    private fun clickEvent() {
        // 뒤로가기 버튼 클릭 이벤트
        binding.toolbarCreateGroup.ivToolbarBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // 화면 바깥 클릭 시
        binding.cLayoutCreateGroup.setOnClickListener {
            getHideKeyboard(binding.root) // 키보드 닫기
        }

        // 그룹 추가하기 버튼 클릭 이벤트
        binding.btnCreateGroupCreate.setOnClickListener {
            // 추가 가능한 상태라면
            if (isEnable) {
                showToast("그룹 추가! ${binding.edtCreateGroupEmoji.text} ${binding.edtCreateGroupTitle.text}")
                // TODO: 파이어베이스에 데이터 전송

                // TODO: 데이터 전송에 성공했다면 그룹 화면으로 이동
                val actionToGroup = CreateGroupFragmentDirections.actionCreateGroupFrmToGroupFrm()
                findNavController().navigate(actionToGroup)
            }
        }
    }

    // 컴포넌트 UI 변경
    private fun updateComponentUI() {
        // 추가 가능
        if (isEnable) {
            binding.edtCreateGroupTitle.setBackgroundResource(R.drawable.ic_text_field_selected_358)
            binding.btnCreateGroupCreate.setBackgroundResource(R.drawable.ic_button_wide_selected_358)
            binding.btnCreateGroupCreate.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
        // 추가 불가능
        else {
            binding.edtCreateGroupTitle.setBackgroundResource(R.drawable.ic_text_field_unselected_358)
            binding.btnCreateGroupCreate.setBackgroundResource(R.drawable.ic_button_wide_unselected_358)
            binding.btnCreateGroupCreate.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_400))
        }
    }
}