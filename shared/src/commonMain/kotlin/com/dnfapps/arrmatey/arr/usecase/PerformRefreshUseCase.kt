package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.CommandPayload.*
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.InstanceScopedRepository

class PerformRefreshUseCase {
    suspend operator fun invoke(
        mediaId: Long,
        type: InstanceType,
        repository: ArrInstanceRepository
    ): NetworkResult<Any> {
        if (type == InstanceType.Listenarr) {
            return repository.getMediaDetails(mediaId)
        }
        val payload = when (type) {
            InstanceType.Sonarr -> CommandPayload.RefreshSeries(listOf(mediaId))
            InstanceType.Radarr -> CommandPayload.RefreshMovie(listOf(mediaId))
            InstanceType.Lidarr -> CommandPayload.RefreshAlbum(listOf(mediaId))
            InstanceType.Booksehelf -> CommandPayload.RefreshAuthor(listOf(mediaId))
            else -> throw UnsupportedOperationException("Cannot perform refresh on an instance of type $type")
        }
        return repository.executeCommand(payload)
    }

    suspend fun bulkRefresh(
        ids: List<Long>,
        type: InstanceType,
        repository: ArrInstanceRepository
    ): NetworkResult<Any> {
        val payload = when (type) {
            InstanceType.Sonarr -> RefreshSeries(ids)
            InstanceType.Radarr -> RefreshMovie(ids)
            InstanceType.Lidarr -> RefreshAlbum(ids)
            InstanceType.Booksehelf -> RefreshAuthor(ids)
            else -> throw UnsupportedOperationException("Cannot perform refresh on an instance of type $type")
        }
        return repository.executeCommand(payload)
    }
}