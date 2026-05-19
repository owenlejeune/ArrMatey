package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.extensions.localToday
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

data class CalendarState(
    val filterState: CalendarFilterState = CalendarFilterState(),
    val items: Map<LocalDate, List<CalendarItem>> = emptyMap(),
    val dates: List<LocalDate> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingFuture: Boolean = false,
    val error: String? = null,
    val today: LocalDate = Clock.localToday()
) {
    constructor(): this(CalendarFilterState())
}