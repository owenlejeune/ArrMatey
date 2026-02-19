package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.InstanceScopedRepository

class PerformRefreshUseCase {
    suspend operator fun invoke(
        mediaId: Long,
        type: InstanceType,
        repository: InstanceScopedRepository
    ): NetworkResult<Any> {
        val payload = when (type) {
            InstanceType.Sonarr -> CommandPayload.RefreshSeries(mediaId)
            InstanceType.Radarr -> CommandPayload.RefreshMovie(listOf(mediaId))
            InstanceType.Lidarr -> CommandPayload.RefreshAlbum(mediaId)
            InstanceType.Prowlarr, InstanceType.QBittorrent, InstanceType.Sabnzbd -> return NetworkResult.Error(message = "Not supported for this instance type")
        }
        return repository.executeCommand(payload)
    }
}