package com.dnfapps.arrmatey.arr.service

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.EpisodeGroup
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.notifications.NotificationCleanupUseCase
import com.dnfapps.arrmatey.notifications.ScheduleNotificationUseCase
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
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
    private val scheduleNotificationUseCase: ScheduleNotificationUseCase,
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

    private val _hasLoaded = MutableStateFlow(false)
    val hasLoaded: StateFlow<Boolean> = _hasLoaded.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val daysRange = 45

    suspend fun load() {
        if (_isLoading.value) return

        _isLoading.value = true
        _error.value = null

        val now =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        val start = now.minus(daysRange, DateTimeUnit.DAY)
        val end = now.plus(daysRange, DateTimeUnit.DAY)

        insertDates(start, end)

        fetch(start, end)

        _isLoading.value = false
        _hasLoaded.value = true
    }

    suspend fun loadMoreDates() {
        if (_isLoadingFuture.value || _isLoading.value) return

        val lastDate =
            _dates.value.lastOrNull() ?: run {
                load()
                return
            }

        _isLoadingFuture.value = true

        val start = lastDate.plus(1, DateTimeUnit.DAY)
        val end = lastDate.plus(daysRange, DateTimeUnit.DAY)

        insertDates(start, end)

        fetch(start, end)

        _isLoadingFuture.value = false
    }

    private suspend fun fetch(
        start: LocalDate,
        end: LocalDate,
    ) {
        val repositories = instanceManager.getAllArrRepositories()

        coroutineScope {
            repositories.forEach { repository ->
                launch {
                    repository.client
                        .getCalendar(start, end)
                        .onSuccess { items ->
                            handleCalendarItems(repository, items)
                        }.onError { _, message, _ ->
                            _error.value = message
                        }
                }
            }
        }
    }

    private fun handleCalendarItems(
        repository: ArrInstanceRepository,
        items: List<CalendarItem>,
    ) {
        val type = repository.instance.type
        val instance = repository.instance
        val instanceId = instance.id

        val itemsWithId =
            items.map { item ->
                when (item) {
                    is ArrMovie -> item.copy(instanceId = instanceId, instanceIds = listOf(instanceId))
                    is Episode -> item.copy(instanceId = instanceId, instanceIds = listOf(instanceId))
                    is ArrAlbum -> item.copy(instanceId = instanceId, instanceIds = listOf(instanceId))
                    is Book -> item.copy(instanceId = instanceId, instanceIds = listOf(instanceId))
                    is Audiobook -> item.copy(instanceId = instanceId, instanceIds = listOf(instanceId))
                    else -> item
                }
            }

        // State updates immediately as this repository succeeds
        _items.update { current ->
            val next = current.toMutableMap()
            itemsWithId.forEach { item ->
                item.getCalendarDates().forEach { date ->
                    upsertItem(next, item, date.toLocalDate())
                }
            }
            if (type == InstanceType.Sonarr) {
                applyGrouping(next)
            }
            next
        }

        // Notifications in background scope
        scope.launch {
            val fetchedIds = itemsWithId.map { it.calendarId.toInt() }.toSet()

            val snapshot: List<CalendarItem> =
                _items.value.values.flatten().filter {
                    isItemOfInstanceType(it, type)
                }

            notificationCleanupUseCase.cleanup(
                instanceId = instance.id,
                currentItems = snapshot,
                fetchedIds = fetchedIds,
                getId = { it.calendarId.toInt() },
                getInstanceId = { it.instanceId },
            )

            itemsWithId.forEach { item ->
                scheduleNotificationUseCase(
                    instance = instance,
                    item = item,
                )
            }
        }
    }

    private fun isItemOfInstanceType(
        item: CalendarItem,
        type: InstanceType,
    ): Boolean =
        when (type) {
            InstanceType.Radarr -> item is ArrMovie
            InstanceType.Sonarr -> item is Episode || item is EpisodeGroup
            InstanceType.Lidarr -> item is ArrAlbum
            InstanceType.Bookshelf -> item is Book
            InstanceType.Listenarr -> item is Audiobook
            else -> false
        }

    private fun upsertItem(
        map: MutableMap<LocalDate, List<CalendarItem>>,
        item: CalendarItem,
        date: LocalDate,
    ) {
        val currentList = map[date]?.toMutableList() ?: mutableListOf()

        val existingIndex =
            currentList.indexOfFirst { existing ->
                isSameItem(existing, item)
            }
        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            val combinedIds = (existing.instanceIds + item.instanceIds).distinct()
            currentList[existingIndex] = mergeItems(existing, item, combinedIds)
        } else {
            currentList.add(item)
        }

        map[date] = currentList
    }

    private fun mergeItems(
        existing: CalendarItem,
        newItem: CalendarItem,
        instanceIds: List<Long>,
    ): CalendarItem {
        return when (newItem) {
            is ArrMovie -> newItem.copy(instanceIds = instanceIds)
            is Episode -> newItem.copy(instanceIds = instanceIds)
            is ArrAlbum -> newItem.copy(instanceIds = instanceIds)
            is Book -> newItem.copy(instanceIds = instanceIds)
            is Audiobook -> newItem.copy(instanceIds = instanceIds)
            is EpisodeGroup -> {
                val existingEpisodes =
                    if (existing is EpisodeGroup) {
                        listOf(existing.first) + existing.additional
                    } else if (existing is Episode) {
                        listOf(existing)
                    } else {
                        emptyList()
                    }

                val newEpisodes = listOf(newItem.first) + newItem.additional

                val allEpisodes =
                    (existingEpisodes + newEpisodes)
                        .groupBy {
                            it.tvdbId ?: it.id
                        }.map { (_, eps) ->
                            val first = eps.first()
                            val ids = eps.flatMap { it.instanceIds }.distinct()
                            first.copy(instanceIds = ids)
                        }.sortedWith(
                            compareBy<Episode> { it.seasonNumber }
                                .thenBy { it.episodeNumber },
                        )

                if (allEpisodes.isEmpty()) return newItem

                if (allEpisodes.size > 1) {
                    EpisodeGroup(
                        first = allEpisodes.first(),
                        additional = allEpisodes.drop(1),
                        totalCount = allEpisodes.size,
                    )
                } else {
                    allEpisodes.first()
                }
            }
        }
    }

    private fun isSameItem(
        a: CalendarItem,
        b: CalendarItem,
    ): Boolean {
        if (a is EpisodeGroup && b is EpisodeGroup) {
            return a.first.seriesId == b.first.seriesId && a.first.getCalendarDates() == b.first.getCalendarDates()
        }
        if (a::class != b::class) return false
        return when (a) {
            is ArrMovie -> if (b is ArrMovie) a.tmdbId == b.tmdbId else false
            is Episode ->
                if (b is Episode) {
                    when {
                        a.tvdbId != null && b.tvdbId != null -> a.tvdbId == b.tvdbId
                        a.series?.tvdbId != null && b.series?.tvdbId != null ->
                            a.series.tvdbId == b.series.tvdbId &&
                                a.seasonNumber == b.seasonNumber &&
                                a.episodeNumber == b.episodeNumber

                        else -> a.id == b.id && a.instanceId == b.instanceId
                    }
                } else {
                    false
                }

            is ArrAlbum -> if (b is ArrAlbum) a.foreignAlbumId == b.foreignAlbumId else false
            is Book -> if (b is Book) a.foreignBookId == b.foreignBookId else false
            is Audiobook -> if (b is Audiobook) a.asin == b.asin else false
            else -> a.calendarId == b.calendarId && a.instanceId == b.instanceId
        }
    }

    private fun applyGrouping(map: MutableMap<LocalDate, List<CalendarItem>>) {
        map.keys.forEach { date ->
            val items = map[date] ?: return@forEach

            val allEpisodes =
                items
                    .flatMap { item ->
                        when (item) {
                            is Episode -> listOf(item)
                            is EpisodeGroup -> listOf(item.first) + item.additional
                            else -> emptyList()
                        }
                    }.groupBy { it.tvdbId ?: it.id }
                    .map { (_, eps) ->
                        val first = eps.first()
                        val ids = eps.flatMap { it.instanceIds }.distinct()
                        first.copy(instanceIds = ids)
                    }

            if (allEpisodes.isEmpty()) return@forEach

            val nonEpisodeItems = items.filter { it !is Episode && it !is EpisodeGroup }

            val grouped =
                allEpisodes
                    .groupBy { it.series?.tvdbId ?: it.series?.id }
                    .map { (_, episodeList) ->
                        if (episodeList.size > 1) {
                            val sorted =
                                episodeList.sortedWith(
                                    compareBy<Episode> { it.seasonNumber }
                                        .thenBy { it.episodeNumber },
                                )
                            EpisodeGroup(
                                first = sorted.first(),
                                additional = sorted.drop(1),
                                totalCount = sorted.size,
                            )
                        } else {
                            episodeList.first()
                        }
                    }

            map[date] = nonEpisodeItems + grouped
        }
    }

    private fun insertDates(
        start: LocalDate,
        end: LocalDate,
    ) {
        val currentDates = _dates.value.toMutableSet()
        var current = start

        while (current <= end) {
            currentDates.add(current)
            current = current.plus(1, DateTimeUnit.DAY)
        }

        _dates.value = currentDates.sorted()
    }

    private fun Instant.toLocalDate(): LocalDate = this.toLocalDateTime(TimeZone.currentSystemDefault()).date

    fun reset() {
        _items.value = emptyMap()
        _dates.value = emptyList()
        _error.value = null
        _hasLoaded.value = false
    }

    fun cleanup() {
        scope.cancel()
    }
}
