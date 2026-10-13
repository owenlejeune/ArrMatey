package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.monitoring.model.PlaybackHistoryItem
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamSession
import com.dnfapps.arrmatey.monitoring.model.PlaybackSummaryStats
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.flow.StateFlow

interface PlaybackMonitoringRepository : InstanceScopedRepository {
    val streamSessions: StateFlow<List<PlaybackStreamSession>>
    val summaryStats: StateFlow<PlaybackSummaryStats?>

    suspend fun refreshPlaybackStreams(): NetworkResult<List<PlaybackStreamSession>>
    suspend fun refreshPlaybackStats(): NetworkResult<PlaybackSummaryStats>
    suspend fun getPlaybackHistory(pageOrStart: Int = 0, pageSize: Int = 25): NetworkResult<List<PlaybackHistoryItem>>
    suspend fun terminatePlaybackStream(sessionId: String, message: String? = null): NetworkResult<Unit>
}
