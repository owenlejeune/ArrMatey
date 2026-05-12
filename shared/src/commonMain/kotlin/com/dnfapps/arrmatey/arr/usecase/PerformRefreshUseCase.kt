package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository

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
            InstanceType.Sonarr -> CommandPayload.RefreshSeries(mediaId)
            InstanceType.Radarr -> CommandPayload.RefreshMovie(listOf(mediaId))
            InstanceType.Lidarr -> CommandPayload.RefreshAlbum(mediaId)
            InstanceType.Booksehelf -> CommandPayload.RefreshAuthor(mediaId)
            else -> throw UnsupportedOperationException("Cannot perform refresh on an instance of type $type")
        }
        return repository.executeCommand(payload)
    }
}