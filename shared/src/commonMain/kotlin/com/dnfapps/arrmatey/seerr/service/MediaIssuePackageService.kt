package com.dnfapps.arrmatey.seerr.service

import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.seerr.api.client.SeerrClient
import com.dnfapps.arrmatey.seerr.api.model.Issue
import com.dnfapps.arrmatey.seerr.api.model.MediaIssuePackage
import com.dnfapps.arrmatey.seerr.api.model.RequestMediaDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class MediaIssuePackageService(
    private val client: SeerrClient
) {
    suspend fun enrichIssue(issue: Issue): MediaIssuePackage {
        val details = when (issue.media?.mediaType) {
            RequestType.Movie -> fetchMovieDetails(issue.media.tmdbId)
            RequestType.Tv -> fetchTvDetails(issue.media.tmdbId)
            else -> throw IllegalStateException("Issue media cannot be null")
        }

        return MediaIssuePackage(issue, details)
    }

    suspend fun enrichIssues(issues: List<Issue>): List<MediaIssuePackage> {
        return coroutineScope {
            issues.map { issue ->
                async { enrichIssue(issue) }
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
            .onError { _, message, _ ->
                println("Error fetching tv details: $message")
            }

        return details
    }
}