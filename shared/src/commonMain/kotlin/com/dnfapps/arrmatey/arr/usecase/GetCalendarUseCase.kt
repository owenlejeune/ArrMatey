package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.service.CalendarService
import com.dnfapps.arrmatey.arr.state.CalendarState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GetCalendarUseCase(
    private val calendarService: CalendarService,
) {
    operator fun invoke(): Flow<CalendarState> =
        combine(
            calendarService.dates,
            calendarService.items,
            combine(
                calendarService.isLoading,
                calendarService.isLoadingFuture,
                calendarService.hasLoaded,
                calendarService.error,
            ) { isLoading, isLoadingFuture, hasLoaded, error ->
                LoadingStatus(isLoading, isLoadingFuture, hasLoaded, error)
            },
        ) { dates, items, status ->
            CalendarState(
                items = items,
                dates = dates,
                isLoading = status.isLoading,
                isLoadingFuture = status.isLoadingFuture,
                hasLoaded = status.hasLoaded,
                error = status.error,
                today =
                    Clock.System
                        .now()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .date,
            )
        }

    private data class LoadingStatus(
        val isLoading: Boolean,
        val isLoadingFuture: Boolean,
        val hasLoaded: Boolean,
        val error: String?,
    )

    suspend fun load() {
        calendarService.load()
    }

    suspend fun loadMore() {
        calendarService.loadMoreDates()
    }

    fun reset() {
        calendarService.reset()
    }
}
