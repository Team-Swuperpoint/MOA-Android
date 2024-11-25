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
        for (i in start until end) {
            if (source != null) {
                val type = Character.getType(source[i]).toByte()
                if (type != Character.SURROGATE && type != Character.OTHER_SYMBOL) {
                    return ""
                }
            }
        }
        return source
    }
}