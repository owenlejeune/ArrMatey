package com.dnfapps.arrmatey.monitoring.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamSession
import com.dnfapps.networking.NetworkResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GetPlaybackStreamsUseCase(
    private val instanceManager: InstanceManager,
) {
    suspend operator fun invoke(): List<PlaybackStreamSession> = coroutineScope {
        val repos = instanceManager.getAllPlaybackMonitoringRepositories()
        val results =
            repos.map { repo ->
                async {
                    when (val res = repo.refreshPlaybackStreams()) {
                        is NetworkResult.Success -> res.data
                        else -> emptyList()
                    }
                }
            }.awaitAll()
        results.flatten()
    }
}
