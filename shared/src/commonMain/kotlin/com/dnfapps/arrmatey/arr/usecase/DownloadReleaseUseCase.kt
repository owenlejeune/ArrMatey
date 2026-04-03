package com.dnfapps.arrmatey.arr.usecase

import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.BookshelfRelease
import com.dnfapps.arrmatey.arr.api.model.DownloadReleasePayload
import com.dnfapps.arrmatey.arr.api.model.LidarrRelease
import com.dnfapps.arrmatey.arr.api.model.MovieRelease
import com.dnfapps.arrmatey.arr.api.model.SeriesRelease
import com.dnfapps.arrmatey.instances.repository.InstanceManager
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.InstanceType
import kotlinx.coroutines.flow.firstOrNull

class DownloadReleaseUseCase(
    private val instanceManager: InstanceManager
) {
    suspend operator fun invoke(
        type: InstanceType,
        release: ArrRelease,
        force: Boolean = false
    ): NetworkResult<Any> {
        val repository = instanceManager.getSelectedArrRepository(type).firstOrNull()
            ?: return NetworkResult.Error(message = "No instance selected")

        val payload = when (release) {
            is SeriesRelease -> buildSonarrPayload(release, force)
            is MovieRelease -> buildRadarrPayload(release, force)
            is LidarrRelease -> buildLidarrPayload(release, force)
            is BookshelfRelease -> buildBookshelfPayload(release)
        }
        return repository.downloadRelease(payload)
    }

    private fun buildSonarrPayload(release: SeriesRelease, force: Boolean): DownloadReleasePayload =
        DownloadReleasePayload.Series(
            guid = release.guid,
            indexerId = release.indexerId,
            seriesId = release.seriesId,
            seasonNumber = release.seasonNumber,
            episodeId = release.episodeId
        )

    private fun buildRadarrPayload(release: MovieRelease, force: Boolean): DownloadReleasePayload =
        DownloadReleasePayload.Movie(
            guid = release.guid,
            indexerId = release.indexerId,
            movieId = if (force) release.movieId else null
        )

    private fun buildLidarrPayload(release: LidarrRelease, force: Boolean): DownloadReleasePayload =
        DownloadReleasePayload.Album(
            guid = release.guid,
            indexerId = release.indexerId,
            albumId = if (force) release.albumId else null
        )

    private fun buildBookshelfPayload(release: BookshelfRelease): DownloadReleasePayload =
        DownloadReleasePayload.Book(
            guid = release.guid,
            indexerId = release.indexerId
        )
}