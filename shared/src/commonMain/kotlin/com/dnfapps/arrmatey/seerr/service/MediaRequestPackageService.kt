package com.dnfapps.arrmatey.seerr.service

import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.seerr.api.client.SeerrClient
import com.dnfapps.arrmatey.seerr.api.model.MediaRequest
import com.dnfapps.arrmatey.seerr.api.model.MediaRequestPackage
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.ServiceDetails
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class MediaRequestPackageService(
    private val client: SeerrClient
) {
    suspend fun enrichMedia(request: MediaRequest): MediaRequestPackage = coroutineScope {
        val detailsDeferred = async {
            when (request.type) {
                RequestType.Movie -> fetchMovieDetails(request.media.tmdbId)
                RequestType.Tv -> fetchTvDetails(request.media.tmdbId)
            }
        }

        val serverDetailsDeferred = async {
            when (request.type) {
                RequestType.Movie -> fetchRadarrDetails(request.serverId ?: 0)
                RequestType.Tv -> fetchSonarrDetails(request.serverId ?: 0)
            }
        }

        MediaRequestPackage(request, detailsDeferred.await(), serverDetailsDeferred.await())
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
                details = movieDetails
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
                details = tvDetails
            }
            .onError { code, message, cause ->
                println("Error fetching tv details: $message, $code, $cause")
            }

        return details
    }
    
    private suspend fun fetchRadarrDetails(serverId: Long): ServiceDetails? {
        var details: ServiceDetails? = null
        
        client.getRadarrDetails(serverId)
            .onSuccess { radarrDetails ->
                details = radarrDetails
            }
            .onError { code, message, cause -> 
                println("Error fetching radarr details: $message, $code, $cause")
            }
        
        return details
    }

    private suspend fun fetchSonarrDetails(serverId: Long): ServiceDetails? {
        var details: ServiceDetails? = null

        client.getSonarrDetails(serverId)
            .onSuccess { sonarrDetails ->
                details = sonarrDetails
            }
            .onError { code, message, cause ->
                println("Error fetching radarr details: $message, $code, $cause")
            }

        return details
    }
}