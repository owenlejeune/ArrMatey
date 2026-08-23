package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.arr.state.CalendarFilterState
import com.dnfapps.arrmatey.arr.state.CalendarState
import com.dnfapps.arrmatey.arr.state.CalendarViewMode
import com.dnfapps.arrmatey.arr.state.ContentFilter
import com.dnfapps.arrmatey.arr.usecase.GetCalendarUseCase
import com.dnfapps.arrmatey.database.InstanceRepository
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.usecase.UpdateCalendarFilterPreferenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class CalendarViewModel(
    private val getCalendarUseCase: GetCalendarUseCase,
    private val updateCalendarFilterStateUseCase: UpdateCalendarFilterPreferenceUseCase,
    preferencesStore: PreferencesStore,
    instanceRepository: InstanceRepository
) : ViewModel() {

    val calendarState = combine(
        getCalendarUseCase(),
        preferencesStore.observeCalendarFilterState()
    ) { calendar, filter ->
        calendar.copy(
            filterState = filter,
            items = filterItems(calendar.items, filter)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarState()
    )

    val instances = instanceRepository.observeAllInstances()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            getCalendarUseCase.load()
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            getCalendarUseCase.loadMore()
        }
    }

    fun reset() {
        getCalendarUseCase.reset()
    }

    fun toggleViewMode() {
        val current = calendarState.value.filterState.viewMode
        val new = when (current) {
            CalendarViewMode.List -> CalendarViewMode.Month
            CalendarViewMode.Month -> CalendarViewMode.List
        }
        safeSaveFilter { it.copy(viewMode = new) }
    }

    fun setContentFilter(contentFilter: ContentFilter) {
        safeSaveFilter {
            it.copy(contentFilter = contentFilter)
        }
    }

    fun toggleShowMonitoredOnly() {
        safeSaveFilter {
            it.copy(showMonitoredOnly = !it.showMonitoredOnly)
        }
    }

    fun toggleShowPremiersOnly() {
        val current = calendarState.value.filterState.showPremiersOnly
        safeSaveFilter {
            it.copy(
                showPremiersOnly = !current,
                showFinalesOnly = if (!current) false else it.showFinalesOnly
            )
        }
    }

    fun toggleShowFinalesOnly() {
        val current = calendarState.value.filterState.showFinalesOnly
        safeSaveFilter {
            it.copy(
                showFinalesOnly = !current,
                showPremiersOnly = if (!current) false else it.showPremiersOnly
            )
        }
    }

    fun setFilterInstanceId(id: Long?) {
        safeSaveFilter {
            it.copy(instanceId = id)
        }
    }

    private fun safeSaveFilter(transform: (CalendarFilterState) -> CalendarFilterState) {
        viewModelScope.launch {
            val filterState = calendarState.value.filterState
            val updatedState = transform(filterState)
            updateCalendarFilterStateUseCase(updatedState)
        }
    }

    private fun filterItems(
        items: Map<LocalDate, List<CalendarItem>>,
        filter: CalendarFilterState
    ): Map<LocalDate, List<CalendarItem>> {
        return items.mapValues { (_, list) ->
            list.mapNotNull { item ->
                when (item) {
                    is ArrMovie -> {
                        if (filter.contentFilter != ContentFilter.All && filter.contentFilter != ContentFilter.MoviesOnly) return@mapNotNull null
                        if (filter.showFinalesOnly || filter.showPremiersOnly) return@mapNotNull null
                        if (filterMovie(item, filter)) item else null
                    }

                    is Episode -> {
                        if (filter.contentFilter != ContentFilter.All && filter.contentFilter != ContentFilter.EpisodesOnly) return@mapNotNull null
                        if (filterEpisode(item, filter)) item else null
                    }

                    is EpisodeGroup -> {
                        if (filter.contentFilter != ContentFilter.All && filter.contentFilter != ContentFilter.EpisodesOnly) return@mapNotNull null
                        val allEpisodes = listOf(item.first) + item.additional
                        val filteredEpisodes = allEpisodes.filter { episode ->
                            filterEpisode(episode, filter)
                        }
                        if (filteredEpisodes.isNotEmpty()) {
                            EpisodeGroup(
                                first = filteredEpisodes.first(),
                                additional = filteredEpisodes.drop(1),
                                totalCount = filteredEpisodes.size
                            )
                        } else {
                            null
                        }
                    }

                    is ArrAlbum -> {
                        if (filter.contentFilter != ContentFilter.All && filter.contentFilter != ContentFilter.AlbumsOnly) return@mapNotNull null
                        if (filter.showFinalesOnly || filter.showPremiersOnly) return@mapNotNull null
                        if (filterAlbum(item, filter)) item else null
                    }

                    is Book -> {
                        if (filter.contentFilter != ContentFilter.All && filter.contentFilter != ContentFilter.BooksOnly) return@mapNotNull null
                        if (filter.showFinalesOnly || filter.showPremiersOnly) return@mapNotNull null
                        if (filterBook(item, filter)) item else null
                    }

                    is Audiobook -> {
                        if (filter.contentFilter != ContentFilter.All && filter.contentFilter != ContentFilter.AudiobooksOnly) return@mapNotNull null
                        if (filter.showFinalesOnly || filter.showPremiersOnly) return@mapNotNull null
                        if (filterAudiobook(item, filter)) item else null
                    }
                }
            }
        }.filterValues { it.isNotEmpty() }
    }

    private fun filterMovie(movie: ArrMovie, filter: CalendarFilterState): Boolean {
        return (!filter.showMonitoredOnly || movie.monitored) &&
                (filter.instanceId == null || movie.instanceId == filter.instanceId)
    }

    private fun filterEpisode(episode: Episode, filter: CalendarFilterState): Boolean {
        return (!filter.showMonitoredOnly || episode.monitored) &&
                (!filter.showPremiersOnly || (episode.seasonNumber == 1 && episode.episodeNumber == 1)) &&
                (!filter.showFinalesOnly || episode.finaleType != null) &&
                (filter.instanceId == null || episode.instanceId == filter.instanceId)
    }

    private fun filterAlbum(album: ArrAlbum, filter: CalendarFilterState): Boolean {
        return (!filter.showMonitoredOnly || album.monitored) &&
                (filter.instanceId == null || album.instanceId == filter.instanceId)
    }

    private fun filterBook(book: Book, filter: CalendarFilterState): Boolean {
        return (!filter.showMonitoredOnly || book.monitored) &&
                (filter.instanceId == null || book.instanceId == filter.instanceId)
    }

    private fun filterAudiobook(audiobook: Audiobook, filter: CalendarFilterState): Boolean {
        return (!filter.showMonitoredOnly || audiobook.monitored) &&
                (filter.instanceId == null || audiobook.instanceId == filter.instanceId)
    }
}