package com.swuperpoint.moa_android.widget

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.navercorp.nid.NaverIdLoginSDK
import com.swuperpoint.moa_android.R

/* 공통적으로 사용하는 데이터를 관리하는 파일 */
class ApplicationClass: Application() {
    init {
        instance = this
    }

    companion object {
        lateinit var instance: ApplicationClass

        fun applicationContext(): Context {
            return instance.applicationContext
        }

        const val X_AUTH_TOKEN: String = "xAuthToken" // token 키 값
        const val TAG: String = "AUTH" // SharedPreferences 키 값

        lateinit var mSharedPreferences: SharedPreferences
    }

    override fun onCreate() {
        super.onCreate()
        // 네이버 로그인 SDK 초기화
        NaverIdLoginSDK.initialize(this, getString(R.string.naver_login_client_id) , getString(R.string.naver_login_client_secret), getString(R.string.app_name))

        mSharedPreferences = applicationContext.getSharedPreferences(TAG, Context.MODE_PRIVATE)
    }
}