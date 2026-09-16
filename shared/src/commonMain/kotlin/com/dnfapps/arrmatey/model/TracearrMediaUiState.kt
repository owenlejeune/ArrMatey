package com.dnfapps.arrmatey.model

import com.dnfapps.arrmatey.tracearr.api.model.TracearrHistoryItem
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaDetails
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaServerStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaStats
import com.dnfapps.arrmatey.tracearr.api.model.TracearrMediaWatchers

enum class TracearrStatsWindowType {
    AllTime,
    Last30,
    Last7,
}

data class TracearrMediaUiState(
    val isTracearrConfigured: Boolean = false,
    val mediaDetails: TracearrMediaDetails? = null,
    val stats: TracearrMediaStats? = null,
    val watchers: TracearrMediaWatchers? = null,
    val historyItems: List<TracearrHistoryItem> = emptyList(),
    val nextHistoryCursor: String? = null,
    val isLoadingHistoryMore: Boolean = false,
    val selectedStatsWindow: TracearrStatsWindowType = TracearrStatsWindowType.AllTime,
    val isLoading: Boolean = false,
) {
    constructor() : this(false) // empty constructor for ios

    val totalPlays: Long
        get() =
            when (selectedStatsWindow) {
                TracearrStatsWindowType.AllTime ->
                    stats
                        ?.windows
                        ?.allTime
                        ?.combined
                        ?.plays ?: 0
                TracearrStatsWindowType.Last30 ->
                    stats
                        ?.windows
                        ?.last30
                        ?.combined
                        ?.plays ?: 0
                TracearrStatsWindowType.Last7 ->
                    stats
                        ?.windows
                        ?.last7
                        ?.combined
                        ?.plays ?: 0
            }

    val totalWatchTimeMs: Long
        get() =
            when (selectedStatsWindow) {
                TracearrStatsWindowType.AllTime ->
                    stats
                        ?.windows
                        ?.allTime
                        ?.combined
                        ?.watchTimeMs ?: 0
                TracearrStatsWindowType.Last30 ->
                    stats
                        ?.windows
                        ?.last30
                        ?.combined
                        ?.watchTimeMs ?: 0
                TracearrStatsWindowType.Last7 ->
                    stats
                        ?.windows
                        ?.last7
                        ?.combined
                        ?.watchTimeMs ?: 0
            }

    val uniqueUsers: Int
        get() =
            when (selectedStatsWindow) {
                TracearrStatsWindowType.AllTime ->
                    stats
                        ?.windows
                        ?.allTime
                        ?.combined
                        ?.uniqueUsers ?: 0
                TracearrStatsWindowType.Last30 ->
                    stats
                        ?.windows
                        ?.last30
                        ?.combined
                        ?.uniqueUsers ?: 0
                TracearrStatsWindowType.Last7 ->
                    stats
                        ?.windows
                        ?.last7
                        ?.combined
                        ?.uniqueUsers ?: 0
            }

    val perServerStats: List<TracearrMediaServerStats>
        get() =
            when (selectedStatsWindow) {
                TracearrStatsWindowType.AllTime -> stats?.windows?.allTime?.perServer ?: emptyList()
                TracearrStatsWindowType.Last30 -> stats?.windows?.last30?.perServer ?: emptyList()
                TracearrStatsWindowType.Last7 -> stats?.windows?.last7?.perServer ?: emptyList()
            }
}
