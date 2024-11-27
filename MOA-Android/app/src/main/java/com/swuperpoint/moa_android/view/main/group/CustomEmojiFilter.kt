package com.swuperpoint.moa_android.view.main.group

import android.text.InputFilter
import android.text.Spanned

/* 이모지만 입력할 수 있는 EditText 필터 */
class CustomEmojiFilter: InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // 입력된 값이 있는 지 확인
        val currentText = dest?.toString() ?: ""

        // 1개의 이모지만 입력할 수 있도록 제한
        if (currentText.isNotEmpty()) {
            return ""
        }

        // 이모지만 입력할 수 있도록 제한
        for (i in start until end) {
            if (source != null) {
                val type = Character.getType(source[i]).toByte()
                if (type != Character.SURROGATE && type != Character.OTHER_SYMBOL) {
                    return ""
                }
            }
        }

        // 이모지 반환
        return source
    }
}