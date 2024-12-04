package com.swuperpoint.moa_android.view.main.mypage

import android.content.Intent
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.databinding.FragmentMypageBinding
import com.swuperpoint.moa_android.view.base.BaseFragment
import com.swuperpoint.moa_android.view.main.onboarding.LoginActivity
import com.swuperpoint.moa_android.viewmodel.main.mypage.MypageViewModel
import com.swuperpoint.moa_android.widget.utils.removeToken

/* 마이페이지 화면 */
class MypageFragment : BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::inflate) {
    private val viewModel: MypageViewModel by viewModels()

    override fun initViewCreated() {
        // LiveData 관찰
        observeViewModel()

        // 데이터 로드
        viewModel.fetchMypage()
    }

    override fun initAfterBinding() {
        // 로그아웃 버튼 클릭 이벤트
        binding.tvMypageLogout.setOnClickListener {
            removeToken() // 토큰 삭제

            // 로그인 화면으로 이동
            showToast("로그아웃했습니다")
            requireActivity().let {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
            }
        }
    }

    // LiveData 관찰
    private fun observeViewModel() {
        // 프로필 사진 정보 관찰
        viewModel.profileImgURL.observe(viewLifecycleOwner) { img ->
            Glide.with(requireContext())
                .load(img)
                .fallback(R.color.gray_200)
                .error(R.color.gray_200)
                .into(binding.ivMypageProfile)
        }

        // 닉네임 정보 관찰
        viewModel.nickname.observe(viewLifecycleOwner) { name ->
            binding.tvMypageName.text = name
        }
    }
}