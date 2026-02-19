package com.dnfapps.arrmatey.arr.service

import com.dnfapps.arrmatey.arr.api.client.ArrClient
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.InstanceScopedRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

class CalendarService(
    private val instanceManager: InstanceManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _movies = MutableStateFlow<Map<LocalDate, List<ArrMovie>>>(emptyMap())
    val movies: StateFlow<Map<LocalDate, List<ArrMovie>>> = _movies.asStateFlow()

    private val _episodes = MutableStateFlow<Map<LocalDate, List<Episode>>>(emptyMap())
    val episodes: StateFlow<Map<LocalDate, List<Episode>>> = _episodes.asStateFlow()

    private val _episodeGroups = MutableStateFlow<Map<LocalDate, List<EpisodeGroup>>>(emptyMap())
    val episodeGroups: StateFlow<Map<LocalDate, List<EpisodeGroup>>> = _episodeGroups.asStateFlow()

    private val _albums = MutableStateFlow<Map<LocalDate, List<ArrAlbum>>>(emptyMap())
    val albums: StateFlow<Map<LocalDate, List<ArrAlbum>>> = _albums.asStateFlow()

    private val _dates = MutableStateFlow<List<LocalDate>>(emptyList())
    val dates: StateFlow<List<LocalDate>> = _dates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingFuture = MutableStateFlow(false)
    val isLoadingFuture: StateFlow<Boolean> = _isLoadingFuture.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val daysRange = 45

    suspend fun load() {
        if (_isLoading.value) return

        _isLoading.value = true
        _error.value = null

        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val start = now.minus(daysRange, DateTimeUnit.DAY)
        val end = now.plus(daysRange, DateTimeUnit.DAY)

        fetch(start, end)

        _isLoading.value = false
    }

    suspend fun loadMoreDates() {
        if (_isLoadingFuture.value) return

        val lastDate = _dates.value.lastOrNull() ?: return

        _isLoadingFuture.value = true

        val start = lastDate
        val end = lastDate.plus(daysRange, DateTimeUnit.DAY)

        fetch(start, end)

        _isLoadingFuture.value = false
    }

    private suspend fun fetch(start: LocalDate, end: LocalDate) {
        val repositories = instanceManager.getAllRepositories()

        coroutineScope {
            repositories.forEach { repository ->
                launch {
                    when (repository.instance.type) {
                        InstanceType.Radarr -> fetchMovies(repository, start, end)
                        InstanceType.Sonarr -> fetchEpisodes(repository, start, end)
                        InstanceType.Lidarr -> fetchAlbums(repository, start, end)
                        InstanceType.Prowlarr -> {}
                    }
                }
            }
        }

        insertDates(start, end)
    }

    private suspend fun fetchMovies(
        repository: InstanceScopedRepository,
        start: LocalDate,
        end: LocalDate
    ) {
        val client = repository.client as? ArrClient ?: return
        client.getMovieCalendar(start, end)
            .onSuccess { movies ->
                val currentMovies = _movies.value.toMutableMap()

                movies.forEach { movie ->
                    movie.digitalRelease?.let { instant ->
                        val date = instant.toLocalDate()
                        upsertMovie(currentMovies, movie, date)
                    }

                    movie.physicalRelease?.let { instant ->
                        val date = instant.toLocalDate()
                        upsertMovie(currentMovies, movie, date)
                    }

                    movie.inCinemas?.let { instant ->
                        val date = instant.toLocalDate()
                        upsertMovie(currentMovies, movie, date)
                    }
                }

                _movies.value = currentMovies
            }
            .onError { _, message, _ ->
                _error.value = message
            }
    }

    private fun upsertMovie(
        map: MutableMap<LocalDate, List<ArrMovie>>,
        movie: ArrMovie,
        date: LocalDate
    ) {
        val currentList = map[date]?.toMutableList() ?: mutableListOf()

        val existingIndex = currentList.indexOfFirst { it.id == movie.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = movie
        } else {
            currentList.add(movie)
        }

        map[date] = currentList
    }

    private suspend fun fetchEpisodes(
        repository: InstanceScopedRepository,
        start: LocalDate,
        end: LocalDate
    ) {
        val client = repository.client as? ArrClient ?: return
        client.getEpisodeCalendar(start, end)
            .onSuccess { episodes ->
                val currentEpisodes = _episodes.value.toMutableMap()

                episodes.forEach { episode ->
                    episode.airDateUtc?.let { instant ->
                        val date = instant.toLocalDate()
                        upsertEpisode(currentEpisodes, episode, date)
                    }
                }

                _episodes.value = currentEpisodes
                updateEpisodeGroups()
            }
            .onError { _, message, cause ->
                _error.value = message
            }
    }

    private fun upsertEpisode(
        map: MutableMap<LocalDate, List<Episode>>,
        episode: Episode,
        date: LocalDate
    ) {
        val currentList = map[date]?.toMutableList() ?: mutableListOf()

        val existingIndex = currentList.indexOfFirst { it.id == episode.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = episode
        } else {
            currentList.add(episode)
        }

        map[date] = currentList
    }

    private fun updateEpisodeGroups() {
        val grouped = _episodes.value.mapValues { (_, episodes) ->
            episodes
                .groupBy { it.series?.id }
                .mapNotNull { (_, episodeList) ->
                    if (episodeList.isEmpty()) return@mapNotNull null

                    val sorted = episodeList.sortedWith(
                        compareBy<Episode> { it.seasonNumber }
                            .thenBy { it.episodeNumber }
                    )

                    EpisodeGroup(
                        first = sorted.first(),
                        additional = sorted.drop(1),
                        totalCount = sorted.size
                    )
                }
                .sortedBy { it.first.series?.title }
        }

        _episodeGroups.value = grouped
    }

    private suspend fun fetchAlbums(
        repository: InstanceScopedRepository,
        start: LocalDate,
        end: LocalDate
    ) {
        val client = repository.client as? ArrClient ?: return
        client.getAlbumCalendar(start, end)
            .onSuccess { albums ->
                val currentAlbums = _albums.value.toMutableMap()

                albums.forEach { album ->
                    album.releaseDate?.let { instant ->
                        val date = instant.toLocalDate()
                        upsertAlbum(currentAlbums, album, date)
                    }
                }

                _albums.value = currentAlbums
            }
            .onError { _, message, cause ->
                _error.value = message
            }
    }

    private fun upsertAlbum(
        map: MutableMap<LocalDate, List<ArrAlbum>>,
        album: ArrAlbum,
        date: LocalDate
    ) {
        val currentList = map[date]?.toMutableList() ?: mutableListOf()

        val existingIndex = currentList.indexOfFirst { it.id == album.id }
        if (existingIndex >= 0) {
            currentList[existingIndex] = album
        } else {
            currentList.add(album)
        }

        map[date] = currentList
    }

    private fun insertDates(start: LocalDate, end: LocalDate) {
        val currentDates = _dates.value.toMutableList()
        var current = start

        while (current <= end) {
            if (!currentDates.contains(current)) {
                currentDates.add(current)
            }
            current = current.plus(1, DateTimeUnit.DAY)
        }

        _dates.value = currentDates.sorted()
    }

    private fun Instant.toLocalDate(): LocalDate {
        return this.toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    fun reset() {
        _movies.value = emptyMap()
        _episodes.value = emptyMap()
        _albums.value = emptyMap()
        _dates.value = emptyList()
        _error.value = null
    }

    fun cleanup() {
        scope.cancel()
    }
}