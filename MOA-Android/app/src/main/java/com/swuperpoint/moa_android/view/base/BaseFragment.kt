package com.swuperpoint.moa_android.view.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.view.main.MainActivity
import com.swuperpoint.moa_android.widget.utils.Inflate

abstract class BaseFragment<VB: ViewBinding>(private val inflate: Inflate<VB>) : Fragment() {
    private var mBinding: VB? = null
    protected val binding get() = mBinding!!
    protected var mainActivity: MainActivity? = null

    private val TAG = "BaseFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            mainActivity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)
            WindowInsetsControllerCompat(mainActivity?.window!!, mainActivity?.window?.decorView!!).isAppearanceLightStatusBars = true
        } catch (e: Exception) {
            Log.d("$TAG - onViewCreated", e.stackTraceToString())
        }

        // ViewDataBinding인 경우
        if (binding is ViewDataBinding) {
            (binding as ViewDataBinding).lifecycleOwner = this
        }

        initViewCreated()
    }

    protected abstract fun initViewCreated()

    override fun onStart() {
        super.onStart()
        initAfterBinding()
    }

    protected abstract fun initAfterBinding()

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mainActivity = context as MainActivity
        } catch (e: Exception) {
            Log.d("$TAG - onAttach", e.stackTraceToString())
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity = null
    }

    // 키보드 숨기기
    fun getHideKeyboard(view: View) {
        mainActivity!!.getHideKeyboard(view)
    }

    // 키보드 띄우기
    fun getShowKeyboard(edt: EditText) {
        mainActivity!!.getShowKeyboard(edt)
    }

    // 토스트 메시지 띄우기
    fun showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(requireContext(), message, length).show()
    }

    // 툴 바 제목 설정
    fun setToolbarTitle(toolbar: TextView, title: String) {
        toolbar.text = title
    }
}