package com.dnfapps.arrmatey.arr.state

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.extensions.localToday
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

data class CalendarState(
    val filterState: CalendarFilterState = CalendarFilterState(),
    val movies: Map<LocalDate, List<ArrMovie>> = emptyMap(),
    val episodes: Map<LocalDate, List<Episode>> = emptyMap(),
    val groupedEpisodes: Map<LocalDate, List<EpisodeGroup>> = emptyMap(),
    val albums: Map<LocalDate, List<ArrAlbum>> = emptyMap(),
    val books: Map<LocalDate, List<Book>> = emptyMap(),
    val dates: List<LocalDate> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingFuture: Boolean = false,
    val error: String? = null,
    val today: LocalDate = Clock.localToday()
) {
    constructor(): this(CalendarFilterState())
}