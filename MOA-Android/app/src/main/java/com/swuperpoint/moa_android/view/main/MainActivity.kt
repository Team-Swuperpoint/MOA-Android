package com.swuperpoint.moa_android.view.main

import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.ActivityMainBinding
import com.swuperpoint.moa_android.view.base.BaseActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swuperpoint.moa_android.view.main.onboarding.LoginActivity
import com.swuperpoint.moa_android.widget.utils.saveNickname
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse

class MainActivity: BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private var backPressedTime: Long = 0
    private val db = Firebase.firestore

    override fun beforeSetContentView() {
        FirebaseApp.initializeApp(this)

        // 네이버 로그인 토큰 유효성 체크
        val accessToken = NaverIdLoginSDK.getAccessToken()

        if (accessToken == null) {
            // 토큰이 없으면 로그인 화면으로
            startActivityWithClear(LoginActivity::class.java)
            return
        }

        // 토큰 유효성 체크
        NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(result: NidProfileResponse) {
                // 토큰이 유효하면 사용자 정보 가져오기
                val email = result.profile?.email
                if (email != null) {
                    db.collection("users")
                        .document(email)
                        .get()
                        .addOnSuccessListener { document ->
                            document.getString("nickname")?.let { nickname ->
                                saveNickname(nickname)
                            }
                        }
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                // 토큰이 만료되었으면 로그인 화면으로
                startActivityWithClear(LoginActivity::class.java)
            }

            override fun onError(errorCode: Int, message: String) {
                startActivityWithClear(LoginActivity::class.java)
            }
        })
    }

    override fun initAfterBinding() {
        // 네비게이션 설정
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.main_frm) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.mainBnv, navController)
    }

    // 바텀 네비게이션 바 숨기기
    fun hideLBNV(isHide: Boolean) {
        if (isHide) {
            binding.mainBnv.visibility = View.GONE
        } else {
            binding.mainBnv.visibility = View.VISIBLE
        }
    }

    // 뒤로가기 버튼 이벤트
    fun getBackPressedEvent() {
        for (fragment: Fragment in supportFragmentManager.fragments) {
            if (fragment.isVisible) {
                if (System.currentTimeMillis() > backPressedTime + 2000) {
                    backPressedTime = System.currentTimeMillis()
                    showToast("\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다")
                } else if (System.currentTimeMillis() <= backPressedTime + 2000) {
                    // 홈 화면에서 2초 안에 두번 뒤로가기 누를 경우 앱 종료
                    finish()
                    finishAffinity()
                }
            }
        }
    }
}