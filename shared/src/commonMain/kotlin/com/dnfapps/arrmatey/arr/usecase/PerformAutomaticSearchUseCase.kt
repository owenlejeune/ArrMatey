package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository

class PerformAutomaticSearchUseCase {
    suspend operator fun invoke(
        mediaId: Long,
        type: InstanceType,
        repository: ArrInstanceRepository,
        episodeId: Long? = null,
        seasonNumber: Int? = null,
        albumId: Long? = null,
        bookId: Long? = null
    ): NetworkResult<Any> {
        val payload = when (type) {
            InstanceType.Sonarr -> {
                when {
                    episodeId != null -> CommandPayload.Episode(listOf(episodeId))
                    seasonNumber != null -> CommandPayload.Season(mediaId, seasonNumber)
                    else -> CommandPayload.Series(mediaId)
                }
            }
            InstanceType.Radarr -> CommandPayload.Movie(listOf(mediaId))
            InstanceType.Lidarr -> {
                when {
                    albumId != null -> CommandPayload.Album(listOf(albumId))
                    else -> CommandPayload.Artist(mediaId)
                }
            }
            InstanceType.Booksehelf -> {
                when {
                    bookId != null -> CommandPayload.Book(listOf(bookId))
                    else -> CommandPayload.Author(mediaId)
                }
            }
            InstanceType.Listenarr -> {
                CommandPayload.Audiobook(mediaId)
            }
            else -> throw UnsupportedOperationException("Cannot perform automatic search on instance of type $type")
        }
        return repository.executeCommand(payload)
    }
}