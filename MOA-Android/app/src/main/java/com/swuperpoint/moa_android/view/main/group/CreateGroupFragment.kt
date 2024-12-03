package com.swuperpoint.moa_android.view.main.group

import android.annotation.SuppressLint
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentCreateGroupBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/* 그룹 추가 화면 */
class CreateGroupFragment : BaseFragment<FragmentCreateGroupBinding>(FragmentCreateGroupBinding::inflate) {
    // 추가하기 버튼 활성화 여부
    private var isEnable = false
    private var db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()


    @SuppressLint("SetTextI18n")
    override fun initViewCreated() {
        // 바텀 네비게이션 숨기기
        mainActivity?.hideLBNV(true)

        // 앱 바 타이틀 설정
        binding.toolbarCreateGroup.tvToolbarTitle.text = "그룹 만들기"
    }

    override fun initAfterBinding() {
        // 클릭 이벤트
        clickEvent()

        // EditText에 이모지만 입력할 수 있도록 필터 적용
        binding.edtCreateGroupEmoji.filters = arrayOf(CustomEmojiFilter())

        // 이모지를 입력했다면 버튼 활성화 여부 확인
        binding.edtCreateGroupEmoji.doOnTextChanged { text, start, before, count ->
            if (text.toString().isNotEmpty()) {
                binding.edtCreateGroupEmoji.setBackgroundResource(R.drawable.ic_emoji_selected_120)
            }
            else {
                binding.edtCreateGroupEmoji.setBackgroundResource(R.drawable.ic_emoji_unselected_120)
            }

            // 버튼 UI 변경
            updateButtonUI()
        }

        // 그룹 이름을 입력했다면 버튼 활성화 여부 확인
        binding.edtCreateGroupTitle.doOnTextChanged { text, start, before, count ->
            if (text.toString().isNotEmpty()) {
                binding.edtCreateGroupTitle.setBackgroundResource(R.drawable.ic_text_field_selected_358)
            }
            else {
                binding.edtCreateGroupTitle.setBackgroundResource(R.drawable.ic_text_field_unselected_358)
            }

            // 버튼 UI 변경
            updateButtonUI()
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
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    showToast("사용자 정보를 찾을 수 없습니다.")
                    return@setOnClickListener
                }

                val emoji = binding.edtCreateGroupEmoji.text.toString()
                val groupName = binding.edtCreateGroupTitle.text.toString()

                // Firestore에 저장할 그룹 데이터(그룹 아이템 데이터 클래스 참조)
                val group = hashMapOf(
                    "emoji" to emoji,
                    "groupName" to groupName,
                    "groupMemberNum" to 1,
                    "bgColor" to (0..5).random(),
                    "recentGathering" to "아직 모임이 없어요",
                    "createdAt" to com.google.firebase.Timestamp.now(),  // 생성 시간 추가
                    "memberUIDs" to arrayListOf(currentUser.uid),  // memberEmails 대신 memberUIDs 사용
                    "createdBy" to currentUser.uid  // 이메일 대신 UID 저장
                )

                // Firestore에 그룹 정보 저장
                db.collection("groups")
                    .add(group)
                    .addOnSuccessListener { documentReference ->
                        val groupId = documentReference.id
                        documentReference.update("groupId", groupId)
                            .addOnSuccessListener {
                                // Fragment가 아직 유효한지 확인
                                if (isAdded && activity != null) {
                                    showToast("새로운 그룹을 만들었습니다")
                                    val actionToGroup: NavDirections = CreateGroupFragmentDirections.actionCreateGroupFrmToGroupFrm()
                                    findNavController().navigate(actionToGroup)
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        // Fragment가 아직 유효한지 확인
                        if (isAdded && activity != null) {
                            Log.e("CreateGroup", "Error creating group", e)
                            showToast("그룹 생성 중 오류가 발생했습니다. 다시 시도해주세요.")
                        }
                    }

                // TODO: 데이터 전송에 성공했다면 그룹 화면으로 이동
                val actionToGroup: NavDirections = CreateGroupFragmentDirections.actionCreateGroupFrmToGroupFrm()
                findNavController().navigate(actionToGroup)
            }
        }
    }

    // 버튼 UI 변경
    private fun updateButtonUI() {
        // 추가 가능
        if (binding.edtCreateGroupTitle.text.toString().isNotEmpty() && binding.edtCreateGroupEmoji.text.toString().isNotEmpty()) {
            binding.btnCreateGroupCreate.setBackgroundResource(R.drawable.ic_button_wide_selected_358)
            binding.btnCreateGroupCreate.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            isEnable = true
        }
        // 추가 불가능
        else {
            binding.btnCreateGroupCreate.setBackgroundResource(R.drawable.ic_button_wide_unselected_358)
            binding.btnCreateGroupCreate.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_400))
            isEnable = false
        }
    }
}