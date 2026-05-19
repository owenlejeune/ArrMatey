package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.service.CalendarService
import com.dnfapps.arrmatey.arr.state.CalendarState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GetCalendarUseCase(
    private val calendarService: CalendarService
) {
    operator fun invoke(): Flow<CalendarState> =
        combine(
            calendarService.dates,
            calendarService.items,
            combine(
                calendarService.isLoading,
                calendarService.isLoadingFuture,
                calendarService.error
            ) { isLoading, isLoadingFuture, error ->
                Triple(isLoading, isLoadingFuture, error)
            }
        ) { dates, items, (isLoading, isLoadingFuture, error) ->
            CalendarState(
                items = items,
                dates = dates,
                isLoading = isLoading,
                isLoadingFuture = isLoadingFuture,
                error = error,
                today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            )
        }

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