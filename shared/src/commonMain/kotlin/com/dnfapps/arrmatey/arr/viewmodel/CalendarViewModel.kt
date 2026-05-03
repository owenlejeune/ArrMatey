package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Book
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
        CalendarState(
            filterState = filter,
            movies = filterMovies(calendar.movies, filter),
            episodes = filterEpisodes(calendar.episodes, filter),
            groupedEpisodes = filterEpisodeGroups(calendar.groupedEpisodes, filter),
            albums = filterAlbums(calendar.albums, filter),
            books = filterBooks(calendar.books, filter),
            dates = calendar.dates,
            isLoading = calendar.isLoading,
            isLoadingFuture = calendar.isLoadingFuture,
            error = calendar.error,
            today = calendar.today
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

    private fun filterMovies(
        movieMap: Map<LocalDate, List<ArrMovie>>,
        filter: CalendarFilterState
    ): Map<LocalDate, List<ArrMovie>> =
        if (
            (filter.contentFilter != ContentFilter.MoviesOnly &&
                    filter.contentFilter != ContentFilter.All)
            || filter.showFinalesOnly
        ) {
            emptyMap()
        } else {
            movieMap.mapValues { (_, movies) ->
                movies.filter { movie ->
                    filterMovie(movie, filter)
                }
            }
        }

    private fun filterEpisodes(
        episodesMap: Map<LocalDate, List<Episode>>,
        filter: CalendarFilterState
    ): Map<LocalDate, List<Episode>> =
        if (
            filter.contentFilter != ContentFilter.EpisodesOnly &&
            filter.contentFilter != ContentFilter.All
        ) {
            emptyMap()
        } else {
            episodesMap.mapValues { (_, episodes) ->
                episodes
                    .filter { episode ->
                        filterEpisode(episode, filter)
                    }
                    .sortedBy { it.airDateUtc }
            }
        }

    private fun filterEpisodeGroups(
        episodeGroups: Map<LocalDate, List<EpisodeGroup>>,
        filter: CalendarFilterState
    ): Map<LocalDate, List<EpisodeGroup>> =
        if (
            filter.contentFilter != ContentFilter.EpisodesOnly &&
            filter.contentFilter != ContentFilter.All
        ) {
            emptyMap()
        } else {
            episodeGroups.mapValues { (_, groups) ->
                groups.mapNotNull { group ->
                    val allEpisodes = listOf(group.first) + group.additional
                    val filteredEpisodes = allEpisodes.filter { episode ->
                        filterEpisode(episode, filter)
                    }
                    if (filteredEpisodes.isNotEmpty()) {
                        EpisodeGroup(
                            first = filteredEpisodes.first(),
                            additional = filteredEpisodes.drop(1)
                        )
                    } else {
                        null
                    }
                }
                    .sortedBy { it.first.airDateUtc }
            }
        }

    private fun filterAlbums(
        albumsMap: Map<LocalDate, List<ArrAlbum>>,
        filter: CalendarFilterState
    ): Map<LocalDate, List<ArrAlbum>> =
        if (filter.contentFilter != ContentFilter.All &&
            filter.contentFilter != ContentFilter.AlbumsOnly
        ) {
            emptyMap()
        } else {
            albumsMap.mapValues { (_, albums) ->
                albums.filter { album ->
                    filterAlbum(album, filter)
                }
            }
        }

    private fun filterBooks(
        booksMap: Map<LocalDate, List<Book>>,
        filter: CalendarFilterState
    ): Map<LocalDate, List<Book>> =
        if (filter.contentFilter != ContentFilter.All &&
            filter.contentFilter != ContentFilter.BooksOnly
        ) {
            emptyMap()
        } else {
            booksMap.mapValues { (_, books) ->
                books.filter { book ->
                    filterBook(book, filter)
                }
            }
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
}