package com.swuperpoint.moa_android.view.main.onboarding

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.ActivityNicknameBinding
import com.swuperpoint.moa_android.view.base.BaseActivity
import com.swuperpoint.moa_android.view.main.MainActivity
import com.swuperpoint.moa_android.widget.utils.saveNickname
import java.lang.Exception

/* 닉네임 입력 화면 */
class NicknameActivity : BaseActivity<ActivityNicknameBinding>(ActivityNicknameBinding::inflate) {
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
                // TODO: 파이어베이스에 회원가입 데이터 전송
                // 전송 데이터: naverEmail, naverProfile, binding.edtNickname.text.toString()
                Log.d("회원가입 성공!", "${naverEmail} ${naverProfile} ${binding.edtNickname.text}")

                // TODO: 회원가입 데이터 전송에 성공했다면 닉네임 저장
                saveNickname(binding.edtNickname.text.toString())

                // TODO: 회원가입 데이터 전송에 성공했다면 홈 화면으로 이동
                showToast("회원가입이 완료되었습니다!")
                startActivityWithClear(MainActivity::class.java)
            }
        }
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