package com.dnfapps.arrmatey.instances.usecase

import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.instances.repository.PlaybackMonitoringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPlaybackMonitoringRepositoryUseCase(
    private val instanceManager: InstanceManager,
) {
    fun observeSelected(): Flow<PlaybackMonitoringRepository?> = instanceManager.getSelectedPlaybackMonitoringRepository()

    fun observeAll(): Flow<List<PlaybackMonitoringRepository>> = instanceManager.instanceRepositories.map { repos ->
        repos.values
            .filterIsInstance<PlaybackMonitoringRepository>()
            .filter { (it.instance.type == InstanceType.Tracearr || it.instance.type == InstanceType.Tautulli) && it.instance.enabled }
    }
}
