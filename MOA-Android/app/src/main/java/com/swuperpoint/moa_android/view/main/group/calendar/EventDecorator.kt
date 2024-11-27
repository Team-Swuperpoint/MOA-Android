package com.swuperpoint.moa_android.view.main.group.calendar

import android.content.Context
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

// 월간 캘린더 - 한달 단위로 날짜에 대하여 decorator 넣어주기
class EventDecorator(
    val context: Context,
    private val eventColor: Int,
    private val date: CalendarDay
): DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay?): Boolean = day?.equals(date)!!

    override fun decorate(view: DayViewFacade?) {
        // 각 색상에 맞춰서 날짜 밑에 dot 찍기
        view?.addSpan(DotSpan(6F, ContextCompat.getColor(context, eventColor)))
    }

    // decorator 삭제하기
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventDecorator

        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        return date.hashCode()
    }
}