package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.extensions.localToday
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

data class CalendarState(
    val filterState: CalendarFilterState = CalendarFilterState(),
    val items: Map<LocalDate, List<CalendarItem>> = emptyMap(),
    val dates: List<LocalDate> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingFuture: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: String? = null,
    val today: LocalDate = Clock.localToday(),
    val startOfWeekMonday: Boolean = false,
) {
    constructor() : this(CalendarFilterState())

    fun weekdayHeaders(): List<StringResource> = if (startOfWeekMonday) {
        listOf(
            MR.strings.mon,
            MR.strings.tues,
            MR.strings.wed,
            MR.strings.thu,
            MR.strings.fri,
            MR.strings.sat,
            MR.strings.sun,
        )
    } else {
        listOf(
            MR.strings.sun,
            MR.strings.mon,
            MR.strings.tues,
            MR.strings.wed,
            MR.strings.thu,
            MR.strings.fri,
            MR.strings.sat,
        )
    }

    fun firstDayOfWeek(currentMonth: LocalDate): Int {
        val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, 1)
        return if (startOfWeekMonday) {
            firstDayOfMonth.dayOfWeek.ordinal
        } else {
            (firstDayOfMonth.dayOfWeek.ordinal + 1) % 7
        }
    }
}
