package com.dnfapps.arrmatey.arr.service

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.notifications.NotificationCleanupUseCase
import com.dnfapps.arrmatey.notifications.ScheduleNotificationUseCase
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val instanceManager: InstanceManager,
    private val notificationCleanupUseCase: NotificationCleanupUseCase,
    private val scheduleNotificationUseCase: ScheduleNotificationUseCase
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

    private val _books = MutableStateFlow<Map<LocalDate, List<Book>>>(emptyMap())
    val books: StateFlow<Map<LocalDate, List<Book>>> = _books.asStateFlow()

    private val _audiobooks = MutableStateFlow<Map<LocalDate, List<Audiobook>>>(emptyMap())
    val audiobooks: StateFlow<Map<LocalDate, List<Audiobook>>> = _audiobooks.asStateFlow()

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
        if (_isLoadingFuture.value || _isLoading.value) return

        val lastDate = _dates.value.lastOrNull() ?: run {
            load()
            return
        }

        _isLoadingFuture.value = true

        val start = lastDate.plus(1, DateTimeUnit.DAY)
        val end = lastDate.plus(daysRange, DateTimeUnit.DAY)

        fetch(start, end)

        _isLoadingFuture.value = false
    }

    private suspend fun fetch(start: LocalDate, end: LocalDate) {
        val repositories = instanceManager.getAllArrRepositories()

        coroutineScope {
            repositories.forEach { repository ->
                launch {
                    repository.client.getCalendar(start, end)
                        .onSuccess { items ->
                            handleCalendarItems(repository, items)
                        }
                        .onError { _, message, _ ->
                            _error.value = message
                        }
                }
            }
        }

        insertDates(start, end)
    }

    private fun handleCalendarItems(
        repository: ArrInstanceRepository,
        items: List<CalendarItem>
    ) {
        val type = repository.instance.type
        val instance = repository.instance

        // Notifications
        scope.launch {
            val enrichedItems = if (type == InstanceType.Booksehelf) {
                val authors = (repository.client.getLibrary() as? NetworkResult.Success)?.data
                    ?.filterIsInstance<Author>()?.associateBy { it.id } ?: emptyMap()
                items.filterIsInstance<Book>().map { book ->
                    authors[book.authorId]?.let { author ->
                        book.copy(authorTitle = author.title)
                    } ?: book
                }
            } else items

            val fetchedIds = enrichedItems.map { it.calendarId.toInt() }.toSet()

            val snapshot: List<CalendarItem> = when (type) {
                InstanceType.Radarr -> _movies.value.values.flatten()
                InstanceType.Sonarr -> _episodes.value.values.flatten()
                InstanceType.Lidarr -> _albums.value.values.flatten()
                InstanceType.Booksehelf -> _books.value.values.flatten()
                InstanceType.Listenarr-> _audiobooks.value.values.flatten()
                else -> emptyList()
            }

            notificationCleanupUseCase.cleanup(
                instanceId = instance.id,
                currentItems = snapshot,
                fetchedIds = fetchedIds,
                getId = { it.calendarId.toInt() },
                getInstanceId = { it.instanceId }
            )

            enrichedItems.forEach { item ->
                item.notificationScheduledTime?.let { scheduledTime ->
                    scheduleNotificationUseCase(
                        instance = instance,
                        message = item.notificationMessage,
                        scheduledTime = scheduledTime,
                        notificationId = item.calendarId.toInt(),
                        releaseType = item.notificationReleaseType
                    )
                }
            }

            // State updates
            when (type) {
                InstanceType.Radarr -> {
                    val movies = enrichedItems.filterIsInstance<ArrMovie>()
                    _movies.update { current ->
                        val next = current.toMutableMap()
                        movies.forEach { movie ->
                            movie.getCalendarDates().forEach { date ->
                                upsertMovie(next, movie, date.toLocalDate())
                            }
                        }
                        next
                    }
                }
                InstanceType.Sonarr -> {
                    val episodes = enrichedItems.filterIsInstance<Episode>()
                    _episodes.update { current ->
                        val next = current.toMutableMap()
                        episodes.forEach { episode ->
                            episode.getCalendarDates().forEach { date ->
                                upsertEpisode(next, episode, date.toLocalDate())
                            }
                        }
                        next
                    }
                    updateEpisodeGroups()
                }
                InstanceType.Lidarr -> {
                    val albums = enrichedItems.filterIsInstance<ArrAlbum>()
                    _albums.update { current ->
                        val next = current.toMutableMap()
                        albums.forEach { album ->
                            album.getCalendarDates().forEach { date ->
                                upsertAlbum(next, album, date.toLocalDate())
                            }
                        }
                        next
                    }
                }
                InstanceType.Booksehelf -> {
                    val books = enrichedItems.filterIsInstance<Book>()
                    _books.update { current ->
                        val next = current.toMutableMap()
                        books.forEach { book ->
                            book.getCalendarDates().forEach { date ->
                                upsertBook(next, book, date.toLocalDate())
                            }
                        }
                        next
                    }
                }
                InstanceType.Listenarr -> {
                    val audiobooks = enrichedItems.filterIsInstance<Audiobook>()
                    _audiobooks.update { current ->
                        val next = current.toMutableMap()
                        audiobooks.forEach { audiobook ->
                            audiobook.getCalendarDates().forEach { date ->
                                upsertAudiobook(next, audiobook, date.toLocalDate())
                            }
                        }
                        next
                    }
                }
                else -> {}
            }
        }
    }

    private fun upsertMovie(
        map: MutableMap<LocalDate, List<ArrMovie>>,
        movie: ArrMovie,
        date: LocalDate
    ) {
        val currentList = map[date]?.toMutableList() ?: mutableListOf()

        // Use tmdbId for deduplication (same across Radarr instances)
        val existingIndex = currentList.indexOfFirst { it.tmdbId == movie.tmdbId }
        if (existingIndex >= 0) {
            currentList[existingIndex] = movie
        } else {
            currentList.add(movie)
        }

        map[date] = currentList
    }

    private fun upsertEpisode(
        map: MutableMap<LocalDate, List<Episode>>,
        episode: Episode,
        date: LocalDate
    ) {
        val currentList = map[date]?.toMutableList() ?: mutableListOf()

        // Use tvdbId for deduplication (same across Sonarr instances)
        // Fallback to series tvdbId + season + episode if tvdbId is null
        val existingIndex = currentList.indexOfFirst { existing ->
            when {
                existing.tvdbId != null && episode.tvdbId != null -> existing.tvdbId == episode.tvdbId
                existing.series?.tvdbId != null && episode.series?.tvdbId != null ->
                    existing.series.tvdbId == episode.series.tvdbId &&
                    existing.seasonNumber == episode.seasonNumber &&
                    existing.episodeNumber == episode.episodeNumber
                else -> existing.id == episode.id && existing.instanceId == episode.instanceId
            }
        }
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

    private fun upsertAlbum(
        map: MutableMap<LocalDate, List<ArrAlbum>>,
        album: ArrAlbum,
        date: LocalDate
    ) {
        val currentList = map[date]?.toMutableList() ?: mutableListOf()

        // Use foreignAlbumId (MusicBrainz ID) for deduplication (same across Lidarr instances)
        val existingIndex = currentList.indexOfFirst { it.foreignAlbumId == album.foreignAlbumId }
        if (existingIndex >= 0) {
            currentList[existingIndex] = album
        } else {
            currentList.add(album)
        }

        map[date] = currentList
    }

    private fun upsertBook(
        map: MutableMap<LocalDate, List<Book>>,
        book: Book,
        date: LocalDate
    ) {
        val currentList = map[date]?.toMutableList() ?: mutableListOf()

        val existingIndex = currentList.indexOfFirst { it.foreignBookId == book.foreignBookId }
        if (existingIndex >= 0) {
            currentList[existingIndex] = book
        } else {
            currentList.add(book)
        }

        map[date] = currentList
    }

    private fun upsertAudiobook(
        map: MutableMap<LocalDate, List<Audiobook>>,
        audiobook: Audiobook,
        date: LocalDate
    ) {
        val currentList = map[date]?.toMutableList() ?: mutableListOf()

        val existingIndex = currentList.indexOfFirst { it.asin == audiobook.asin }
        if (existingIndex >= 0) {
            currentList[existingIndex] = audiobook
        } else {
            currentList.add(audiobook)
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
        _books.value = emptyMap()
        _dates.value = emptyList()
        _error.value = null
    }

    fun cleanup() {
        scope.cancel()
    }
}
