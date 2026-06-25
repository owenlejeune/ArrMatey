package com.dnfapps.arrmatey.bazarr.api.client

import com.dnfapps.arrmatey.bazarr.api.model.BazarrEpisode
import com.dnfapps.arrmatey.bazarr.api.model.BazarrEpisodesResponse
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMovie
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMoviesResponse
import com.dnfapps.arrmatey.bazarr.api.model.BazarrRelease
import com.dnfapps.arrmatey.bazarr.api.model.BazarrReleasesResponse
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeries
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeriesResponse
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSystem
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSystemStatus
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeGet
import com.dnfapps.arrmatey.client.safePatch
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class BazarrClient(
    val instance: Instance,
    private val httpClient: HttpClient
) {

    private val baseUrl: String
        get() {
            val cleanUrl = instance.getEffectiveBaseUrl().trim().trimEnd('|', '/', ' ')
            val apiBase = instance.type.apiBase.trim().trimStart('/', ' ')
            return "$cleanUrl/$apiBase"
        }

    suspend fun testConnection(): NetworkResult<Unit> =
        httpClient.safeGet("$baseUrl/${instance.type.testEndpoint}")

    suspend fun getSystemStatus(): NetworkResult<BazarrSystemStatus> =
        httpClient.safeGet("$baseUrl/system/status")

    suspend fun getSystemSettings(): NetworkResult<BazarrSystem> =
        httpClient.safeGet("$baseUrl/system/settings")

    suspend fun getSeries(): NetworkResult<List<BazarrSeries>> =
        httpClient.safeGet<BazarrSeriesResponse>("$baseUrl/series")
            .map { it.data }

    suspend fun getMovies(): NetworkResult<List<BazarrMovie>> =
        httpClient.safeGet<BazarrMoviesResponse>("$baseUrl/movies")
            .map { it.data }

    suspend fun getEpisodes(seriesId: Long): NetworkResult<List<BazarrEpisode>> =
        httpClient.safeGet<BazarrEpisodesResponse>("$baseUrl/episodes") {
            url { parameters.append("seriesid[]", seriesId.toString()) }
        }.map { it.data }

    suspend fun performAutomaticMovieSearch(id: Long): NetworkResult<Unit> =
        httpClient.safePatch("$baseUrl/movies") {
            setBody(buildJsonObject {
                put("action", "search-missing")
                put("radarrid", id)
            })
        }

    suspend fun performManualMovieSearch(id: Long): NetworkResult<List<BazarrRelease>> =
        httpClient.safeGet<BazarrReleasesResponse>("$baseUrl/providers/movies") {
            url { parameters.append("seriesid", id.toString()) }
        }.map { it.data }

    suspend fun performAutomaticSeriesSearch(id: Long): NetworkResult<Unit> =
        httpClient.safePatch("$baseUrl/series") {
            setBody(buildJsonObject {
                put("action", "search-missing")
                put("seriesid", id)
            })
        }

    suspend fun performManualEpisodeSearch(id: Long): NetworkResult<List<BazarrRelease>> =
        httpClient.safeGet<BazarrReleasesResponse>("$baseUrl/providers/episodes") {
            url { parameters.append("episodeid", id.toString()) }
        }.map { it.data }

}
