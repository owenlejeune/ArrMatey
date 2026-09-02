package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.CommandPayload.RefreshAlbum
import com.dnfapps.arrmatey.arr.api.model.CommandPayload.RefreshAuthor
import com.dnfapps.arrmatey.arr.api.model.CommandPayload.RefreshMovie
import com.dnfapps.arrmatey.arr.api.model.CommandPayload.RefreshSeries
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.networking.NetworkResult

class PerformRefreshUseCase {
    suspend operator fun invoke(
        mediaId: Long,
        type: InstanceType,
        repository: ArrInstanceRepository,
    ): NetworkResult<Any> {
        if (type == InstanceType.Listenarr) {
            return repository.getMediaDetails(mediaId)
        }
        val payload =
            when (type) {
                InstanceType.Sonarr -> RefreshSeries(listOf(mediaId))
                InstanceType.Radarr -> RefreshMovie(listOf(mediaId))
                InstanceType.Lidarr -> RefreshAlbum(listOf(mediaId))
                InstanceType.Bookshelf -> RefreshAuthor(listOf(mediaId))
                else -> throw UnsupportedOperationException("Cannot perform refresh on an instance of type $type")
            }
        return repository.executeCommand(payload)
    }

    suspend fun bulkRefresh(
        ids: List<Long>,
        type: InstanceType,
        repository: ArrInstanceRepository,
    ): NetworkResult<Any> {
        val payload =
            when (type) {
                InstanceType.Sonarr -> RefreshSeries(ids)
                InstanceType.Radarr -> RefreshMovie(ids)
                InstanceType.Lidarr -> RefreshAlbum(ids)
                InstanceType.Bookshelf -> RefreshAuthor(ids)
                else -> throw UnsupportedOperationException("Cannot perform refresh on an instance of type $type")
            }
        return repository.executeCommand(payload)
    }
}
