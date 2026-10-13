package com.dnfapps.arrmatey.monitoring.usecase

import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.monitoring.model.PlaybackStreamSession
import com.dnfapps.networking.NetworkResult

class TerminatePlaybackStreamUseCase(
    private val instanceManager: InstanceManager,
) {
    suspend operator fun invoke(
        instanceId: Long,
        sessionId: String,
        message: String? = null,
    ): NetworkResult<Unit> {
        val repo =
            instanceManager.getAllPlaybackMonitoringRepositories().find { it.instance.id == instanceId }
                ?: return NetworkResult.Error(code = 404, message = "Instance not found")
        return repo.terminatePlaybackStream(sessionId = sessionId, message = message)
    }

    suspend operator fun invoke(
        session: PlaybackStreamSession,
        message: String? = null,
    ): NetworkResult<Unit> = invoke(session.instanceId, session.sessionId, message)
}
