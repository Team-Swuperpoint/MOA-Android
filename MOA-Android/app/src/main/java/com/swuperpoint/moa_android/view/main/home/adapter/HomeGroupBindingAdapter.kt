package com.swuperpoint.moa_android.view.main.home.adapter

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.swuperpoint.moa_android.R
import com.swuperpoint.moa_android.widget.ApplicationClass.Companion.applicationContext

@BindingAdapter("isSelectedBackground")
// 홈 화면의 그룹 선택 이벤트
fun setSelectedHomeGroup(cardView: CardView, isSelected: Boolean) {
    if (isSelected) {
        cardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext(), R.color.white))
    }
    else {
        cardView.setCardBackgroundColor(ContextCompat.getColor(applicationContext(), R.color.transparent))
    }
}

@BindingAdapter("backgroundTintResource")
// 홈 화면의 그룹 색상 적용
fun setHomeGroupColor(cardView: CardView, color: Int) {
    val colorResId = when (color) {
        0 -> R.color.main_500
        1 -> R.color.sub_300
        2 -> R.color.sub_500
        3 -> R.color.main_50
        4 -> R.color.main_100
        5 -> R.color.sub_200
        else -> R.color.main_500
    }

    cardView.backgroundTintList = ColorStateList.valueOf(
        ContextCompat.getColor(applicationContext(), colorResId)
    )
}

@BindingAdapter("isSelectedTextColor")
// 홈 화면의 그룹 선택 이벤트
fun setSelectedHomeGroupText(textView: TextView, isSelected: Boolean) {
    if (isSelected) {
        textView.setTextColor(ContextCompat.getColor(applicationContext(), R.color.main_500))
    }
    else {
        textView.setTextColor(ContextCompat.getColor(applicationContext(), R.color.main_50))
    }
}