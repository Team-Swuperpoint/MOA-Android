package com.swuperpoint.moa_android.view.main.group

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.swuperpoint.moa_android.databinding.FragmentGroupJoinBottomSheetBinding
import com.swuperpoint.moa_android.view.main.MainActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.activity.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.DocumentSnapshot
import com.swuperpoint.moa_android.viewmodel.main.group.GroupViewModel

/* 그룹 들어가기 바텀 시트 */
class GroupJoinBottomSheetFragment: BottomSheetDialogFragment() {
    private var mBinding: FragmentGroupJoinBottomSheetBinding? = null
    private val binding get() = mBinding!!
    private var mainActivity: MainActivity? = null
    private val db = FirebaseFirestore.getInstance()

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

        initClickListeners()
    }

    private fun initClickListeners() {
        // 취소 버튼
        binding.iBtnGroupJoinBottomSheetCancel.setOnClickListener {
            dismiss()
        }

        // 그룹 참여 버튼
        binding.btnGroupJoinBottomSheetJoin.setOnClickListener {
            handleGroupJoin()
        }
    }

    private fun handleGroupJoin() {
        val groupCode = binding.edtGroupJoinBottomSheetCode.text.toString().trim()

        // 코드 유효성 검사
        if (groupCode.isEmpty()) {
            showError("그룹 코드를 입력해주세요")
            return
        }

        // 중복 클릭 방지
        binding.btnGroupJoinBottomSheetJoin.isEnabled = false

        // 그룹 검색
        searchGroup(groupCode)
    }

    private fun searchGroup(groupCode: String) {
        db.collection("groups")
            .whereGreaterThanOrEqualTo("groupId", groupCode)
            .whereLessThan("groupId", groupCode + "\uf8ff")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    handleGroupFound(documents.documents[0], groupCode)
                } else {
                    showError("존재하지 않는 코드입니다")
                }
            }
            .addOnFailureListener {
                showError("그룹 검색에 실패했습니다")
            }
    }

    private fun handleGroupFound(groupDoc: DocumentSnapshot, groupCode: String) {
        val fullGroupId = groupDoc.getString("groupId") ?: ""

        if (!fullGroupId.startsWith(groupCode)) {
            showError("존재하지 않는 코드입니다")
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 사용자 정보 가져오기
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                joinGroup(groupDoc, userId, userDoc)
            }
            .addOnFailureListener {
                showError("사용자 정보를 가져오는데 실패했습니다")
            }
    }

    private fun joinGroup(groupDoc: DocumentSnapshot, userId: String, userDoc: DocumentSnapshot) {
        // 그룹 멤버 추가
        db.collection("groups").document(groupDoc.id).update(
            mapOf(
                "memberUIDs" to FieldValue.arrayUnion(userId),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener {
            // 멤버 상세 정보 추가
            db.collection("groups").document(groupDoc.id)
                .collection("members")
                .document(userId)
                .set(hashMapOf(
                    "joinedAt" to FieldValue.serverTimestamp(),
                    "email" to (userDoc.getString("email") ?: ""),
                    "nickname" to (userDoc.getString("nickname") ?: ""),
                    "profile" to (userDoc.getString("profile") ?: "")
                ))
                .addOnSuccessListener {
                    handleJoinSuccess(groupDoc.id)
                }
                .addOnFailureListener {
                    showError("그룹 참여에 실패했습니다")
                }
        }
    }

    private fun handleJoinSuccess(groupId: String) {
        Log.d("그룹참여", "그룹 참여 성공: $groupId")
        Toast.makeText(context, "그룹 참여에 성공했습니다", Toast.LENGTH_SHORT).show()

        // ViewModel 갱신
        (activity as? MainActivity)?.let { mainActivity ->
            val groupViewModel: GroupViewModel by mainActivity.viewModels()
            groupViewModel.refreshGroups()
        }

        // 지연 후 화면 이동
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            findNavController().navigate(
                GroupJoinBottomSheetFragmentDirections
                    .actionGroupJoinBottomSheetFrmToGroupFrm(groupId = groupId)
            )
            dismiss()
        }, 500)
    }

    private fun showError(message: String) {
        binding.tvGroupJoinBottomSheetError.apply {
            text = message
            visibility = View.VISIBLE
        }
        binding.btnGroupJoinBottomSheetJoin.isEnabled = true
    }
}