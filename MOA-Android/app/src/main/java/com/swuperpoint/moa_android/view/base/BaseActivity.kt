package com.swuperpoint.moa_android.view.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.swuperpoint.moa_android.R

abstract class BaseActivity<T : ViewBinding>(private val inflate: (LayoutInflater) -> T): AppCompatActivity() {
    private var mBinding: T? = null
    protected val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        beforeSetContentView()
        super.onCreate(savedInstanceState)

        mBinding = inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 색상
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        // ViewDataBinding인 경우
        if (binding is ViewDataBinding) {
            (binding as ViewDataBinding).lifecycleOwner = this
        }

        initAfterBinding()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBinding != null) {
            mBinding = null
        }
    }

    protected abstract fun beforeSetContentView()

    protected abstract fun initAfterBinding()

    // Activity 이동
    fun startNextActivity(activity: Class<*>?) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }

    // activity 이동 시 별도 설정
    fun startActivityWithClear(activity: Class<*>?) {
        val intent = Intent(this, activity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    // fragment 이동
    fun startNextFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, fragment, tag)
            .commitNowAllowingStateLoss()
    }

    // 토스트 메시지 띄우기
    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // 키보드 숨기기
    fun getHideKeyboard(view: View) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // 키보드 띄우기
    fun getShowKeyboard(edt: EditText) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        edt.requestFocus()
        imm.showSoftInput(edt, InputMethodManager.SHOW_IMPLICIT)
    }
}