package com.swuperpoint.moa_android.view.main.onboarding

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.ActivityLoginBinding
import com.swuperpoint.moa_android.view.base.BaseActivity
import com.swuperpoint.moa_android.view.main.MainActivity
import com.swuperpoint.moa_android.widget.utils.getNickname
import com.swuperpoint.moa_android.widget.utils.getToken
import com.swuperpoint.moa_android.widget.utils.saveNickname
import com.swuperpoint.moa_android.widget.utils.saveToken

/* 로그인 화면 */
class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {
    private lateinit var naverEmail: String // 네이버 이메일
    private lateinit var naverProfile: String // 네이버 프로필

    override fun beforeSetContentView() {
    }

    override fun initAfterBinding() {
        // status bar 숨기기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        }
        else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // 네이버 로그인 버튼 클릭 이벤트
        binding.cLayoutNaverLogin.setOnClickListener {
            val oAuthLoginCallback = object : OAuthLoginCallback {
                // 인증 성공
                override fun onSuccess() {
                    // 네이버api에서 프로필 정보 가져오기
                    NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
                        // 호출 성공
                        override fun onSuccess(result: NidProfileResponse) {

                            // 네이버에서 가져온 데이터 저장
                            naverEmail = result.profile?.email.toString()
                            naverProfile = result.profile?.profileImage.toString()

                            // access 토큰 저장
                            NaverIdLoginSDK.getAccessToken()?.let { it1 -> saveToken(it1) }

                            Log.d("네이버 로그인", "${naverEmail}, ${naverProfile} ${getToken()}")

                            // 닉네임 정보가 있다면, 단순 로그인
                            if (getNickname() != null) {
                                // 홈 화면으로 이동
                                startActivityWithClear(MainActivity::class.java)
                            }
                            // 닉네임 정보가 없다면, 닉네임 입력 화면으로 이동
                            else {
                                // 닉네임 설정 화면으로 이동
                                val intent = Intent(this@LoginActivity, NicknameActivity::class.java)

                                // 다음 화면에서 회원가입을 위해 이메일과 프로필 이미지 값 전달
                                intent.putExtra("Email", naverEmail)
                                intent.putExtra("Profile", naverProfile)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                startActivity(intent)
                            }
                        }

                        // 호출 실패
                        override fun onFailure(httpStatus: Int, message: String) {
                        }

                        // 호출 오류
                        override fun onError(errorCode: Int, message: String) {
                        }
                    })
                }

                // 인증 실패
                override fun onFailure(httpStatus: Int, message: String) {
                    val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                    val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                    Log.e("LoginActivity - Naver", "errorCode : $errorCode errorDescription: $errorDescription")
                    showToast("로그인이 취소되었습니다")
                }

                // 인증 오류
                override fun onError(errorCode: Int, message: String) {
                    onFailure(errorCode, message)
                }
            }

            NaverIdLoginSDK.authenticate(this, oAuthLoginCallback)
        }
    }
}