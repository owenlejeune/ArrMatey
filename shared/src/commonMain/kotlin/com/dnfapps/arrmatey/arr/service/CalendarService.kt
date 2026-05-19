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

    private val _items = MutableStateFlow<Map<LocalDate, List<CalendarItem>>>(emptyMap())
    val items: StateFlow<Map<LocalDate, List<CalendarItem>>> = _items.asStateFlow()

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

            val snapshot: List<CalendarItem> = _items.value.values.flatten().filter {
                isItemOfInstanceType(it, type)
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
            _items.update { current ->
                val next = current.toMutableMap()
                enrichedItems.forEach { item ->
                    item.getCalendarDates().forEach { date ->
                        upsertItem(next, item, date.toLocalDate())
                    }
                }
                if (type == InstanceType.Sonarr) {
                    applyGrouping(next)
                }
                next
            }
        }
    }

    private fun isItemOfInstanceType(item: CalendarItem, type: InstanceType): Boolean {
        return when (type) {
            InstanceType.Radarr -> item is ArrMovie
            InstanceType.Sonarr -> item is Episode || item is EpisodeGroup
            InstanceType.Lidarr -> item is ArrAlbum
            InstanceType.Booksehelf -> item is Book
            InstanceType.Listenarr -> item is Audiobook
            else -> false
        }
    }

    private fun upsertItem(
        map: MutableMap<LocalDate, List<CalendarItem>>,
        item: CalendarItem,
        date: LocalDate
    ) {
        val currentList = map[date]?.toMutableList() ?: mutableListOf()

        val existingIndex = currentList.indexOfFirst { existing ->
            isSameItem(existing, item)
        }
        if (existingIndex >= 0) {
            currentList[existingIndex] = item
        } else {
            currentList.add(item)
        }

        map[date] = currentList
    }

    private fun isSameItem(a: CalendarItem, b: CalendarItem): Boolean {
        if (a::class != b::class) return false
        return when (a) {
            is ArrMovie if b is ArrMovie -> a.tmdbId == b.tmdbId
            is Episode if b is Episode -> {
                when {
                    a.tvdbId != null && b.tvdbId != null -> a.tvdbId == b.tvdbId
                    a.series?.tvdbId != null && b.series?.tvdbId != null ->
                        a.series.tvdbId == b.series.tvdbId &&
                                a.seasonNumber == b.seasonNumber &&
                                a.episodeNumber == b.episodeNumber

                    else -> a.id == b.id && a.instanceId == b.instanceId
                }
            }

            is ArrAlbum if b is ArrAlbum -> a.foreignAlbumId == b.foreignAlbumId
            is Book if b is Book -> a.foreignBookId == b.foreignBookId
            is Audiobook if b is Audiobook -> a.asin == b.asin
            else -> a.calendarId == b.calendarId && a.instanceId == b.instanceId
        }
    }

    private fun applyGrouping(map: MutableMap<LocalDate, List<CalendarItem>>) {
        map.keys.forEach { date ->
            val items = map[date] ?: return@forEach
            val episodes = items.filterIsInstance<Episode>()
            if (episodes.isEmpty()) return@forEach

            val nonEpisodes = items.filter { it !is Episode && it !is EpisodeGroup }

            val grouped = episodes
                .groupBy { it.series?.id }
                .map { (_, episodeList) ->
                    if (episodeList.size > 1) {
                        val sorted = episodeList.sortedWith(
                            compareBy<Episode> { it.seasonNumber }
                                .thenBy { it.episodeNumber }
                        )
                        EpisodeGroup(
                            first = sorted.first(),
                            additional = sorted.drop(1),
                            totalCount = sorted.size
                        )
                    } else {
                        episodeList.first()
                    }
                }

            map[date] = nonEpisodes + grouped
        }
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
        _items.value = emptyMap()
        _dates.value = emptyList()
        _error.value = null
    }

    fun cleanup() {
        scope.cancel()
    }
}
