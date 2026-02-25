package com.dnfapps.arrmatey.seerr.service

import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.seerr.api.client.SeerrClient
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class MediaRequestPackageService(
    private val client: SeerrClient
) {
    suspend fun enrichMedia(request: MediaRequest): MediaRequestPackage {
        val details = when (request.type) {
            RequestType.Movie -> fetchMovieDetails(request.media.tmdbId)
            RequestType.Tv -> fetchTvDetails(request.media.tmdbId)
        }

        return MediaRequestPackage(request, details)
    }

    suspend fun enrichRequests(requests: List<MediaRequest>): List<MediaRequestPackage> {
        return coroutineScope {
            requests.map { request ->
                async { enrichMedia(request) }
            }.awaitAll()
        }
    }

    private suspend fun fetchMovieDetails(tmdbId: Long): RequestMediaDetails? {
        var details: RequestMediaDetails? = null

        client.getMovieDetails(tmdbId)
            .onSuccess { movieDetails ->
                details = RequestMediaDetails.Movie(movieDetails)
            }
            .onError { _, message, _ ->
                println("Error fetching movie details: $message")
            }

        return details
    }

    private suspend fun fetchTvDetails(tmdbId: Long): RequestMediaDetails? {
        var details: RequestMediaDetails? = null

        client.getTvDetails(tmdbId)
            .onSuccess { tvDetails ->
                details = RequestMediaDetails.Tv(tvDetails)
            }
            .onError { _, message, _ ->
                println("Error fetching tv details: $message")
            }

        return details
    }
}