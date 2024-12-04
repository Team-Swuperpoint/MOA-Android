package com.swuperpoint.moa_android.view.main.onboarding

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.ActivityNicknameBinding
import com.swuperpoint.moa_android.view.base.BaseActivity
import com.swuperpoint.moa_android.view.main.MainActivity
import com.swuperpoint.moa_android.widget.utils.saveNickname
import java.lang.Exception
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.UUID

/* 닉네임 입력 화면 */
class NicknameActivity : BaseActivity<ActivityNicknameBinding>(ActivityNicknameBinding::inflate) {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private lateinit var naverEmail: String // 네이버 이메일
    private lateinit var naverProfile: String // 네이버 프로필

    override fun beforeSetContentView() {
    }

    override fun initAfterBinding() {
        // 앞선 화면에서 받은 데이터 가져오기
        try {
            naverEmail = intent.getStringExtra("Email").toString()
            naverProfile = intent.getStringExtra("Profile").toString()
        } catch (e: Exception) {
            Log.e("NicknameActivity - intent", e.stackTraceToString())
        }

        // 닉네임을 입력했다면 버튼 활성화 여부 확인
        binding.edtNickname.doOnTextChanged { text, start, before, count ->
            if (text.toString().isNotEmpty()) {
                binding.edtNickname.setBackgroundResource(R.drawable.ic_text_field_selected_358)
            }
            else {
                binding.edtNickname.setBackgroundResource(R.drawable.ic_text_field_unselected_358)
            }

            // 버튼 UI 변경
            updateButtonUI()
        }

        // 회원가입 버튼 클릭 이벤트
        binding.btnNicknameSignup.setOnClickListener {
            if (binding.edtNickname.text.toString().isNotEmpty()) {
                // 먼저 로그인 시도
                auth.signInWithEmailAndPassword(naverEmail, generateSecurePassword())
                    .addOnSuccessListener { authResult ->
                        // Firestore에 사용자 정보 저장/업데이트
                        val user = hashMapOf(
                            "email" to naverEmail,
                            "profile" to naverProfile,
                            "nickname" to binding.edtNickname.text.toString()
                        )

                        // UID를 문서 ID로 사용하여 저장
                        db.collection("users")
                            .document(authResult.user?.uid ?: "")
                            .set(user)
                            .addOnSuccessListener {
                                saveNickname(binding.edtNickname.text.toString())
                                showToast("가입이 완료되었습니다!")
                                startActivityWithClear(MainActivity::class.java)
                            }
                            .addOnFailureListener { e ->
                                Log.e("가입 실패", "Error: ${e.message}")
                                showToast("오류가 발생했습니다. 다시 시도해주세요.")
                            }
                    }
                    .addOnFailureListener { loginError ->
                        // 로그인 실패시 회원가입 시도
                        auth.createUserWithEmailAndPassword(naverEmail, generateSecurePassword())
                            .addOnSuccessListener { authResult ->
                                // Firestore에 사용자 정보 저장
                                val user = hashMapOf(
                                    "email" to naverEmail,
                                    "profile" to naverProfile,
                                    "nickname" to binding.edtNickname.text.toString()
                                )

                                db.collection("users")
                                    .document(authResult.user?.uid ?: "")  // 이메일 대신 UID 사용
                                    .set(user)
                                    .addOnSuccessListener {
                                        saveNickname(binding.edtNickname.text.toString())
                                        showToast("가입이 완료되었습니다!")
                                        startActivityWithClear(MainActivity::class.java)
                                    }
                                    .addOnFailureListener { firestoreError ->
                                        Log.e("가입 실패", "Error: ${firestoreError.message}")
                                        showToast("오류가 발생했습니다. 다시 시도해주세요.")
                                    }
                            }
                            .addOnFailureListener { signUpError ->
                                showToast("회원가입 중 오류가 발생했습니다.")
                            }
                    }
            }
        }

    }
    private fun generateSecurePassword(): String {
        // 네이버 로그인을 사용하기 때문에 실제 비밀번호는 사용되지 않음
        // 임의의 문자열 생성
        return UUID.randomUUID().toString()
    }

    // 버튼 UI 변경
    private fun updateButtonUI() {
        // 회원가입 가능
        if (binding.edtNickname.text.toString().isNotEmpty()) {
            binding.btnNicknameSignup.setBackgroundResource(R.drawable.ic_button_wide_selected_358)
            binding.btnNicknameSignup.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        // 회원가입 불가능
        else {
            binding.btnNicknameSignup.setBackgroundResource(R.drawable.ic_button_wide_unselected_358)
            binding.btnNicknameSignup.setTextColor(ContextCompat.getColor(this, R.color.gray_400))
        }
    }
}