package com.dnfapps.arrmatey.bazarr.api.client

import com.dnfapps.arrmatey.bazarr.api.model.AutoSearchBody
import com.dnfapps.arrmatey.bazarr.api.model.BazarrBadges
import com.dnfapps.arrmatey.bazarr.api.model.BazarrEpisodesResponse
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMovie
import com.dnfapps.arrmatey.bazarr.api.model.BazarrMoviesResponse
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeries
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSeriesResponse
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSystem
import com.dnfapps.arrmatey.bazarr.api.model.BazarrSystemStatus
import com.dnfapps.arrmatey.bazarr.api.model.ProviderSubtitle
import com.dnfapps.arrmatey.bazarr.api.model.ProviderSubtitlesResponse
import com.dnfapps.arrmatey.bazarr.api.model.ProvidersResponse
import com.dnfapps.arrmatey.bazarr.api.model.WantedEpisodesResponse
import com.dnfapps.arrmatey.bazarr.api.model.WantedMoviesResponse
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.safeDelete
import com.dnfapps.arrmatey.client.safeGet
import com.dnfapps.arrmatey.client.safePatch
import com.dnfapps.arrmatey.client.safePost
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.component.KoinComponent

/**
 * Bazarr's manual/automatic subtitle searches and downloads query external providers
 * synchronously and routinely take longer than the default 60s request timeout, so those
 * specific calls use a more generous timeout.
 */
private const val SEARCH_TIMEOUT_MS = 180_000L

/**
 * Typed client for the Bazarr REST API (`/api`, authenticated with the `X-API-KEY`
 * header which ArrMatey injects per-instance). Bazarr accepts request arguments via the
 * query string for its subtitle endpoints, and returns 204 No Content on mutations.
 */
interface BazarrClient {
    suspend fun testConnection(): NetworkResult<Unit>
    suspend fun getSystemStatus(): NetworkResult<BazarrSystemStatus>
    suspend fun getSystemSettings(): NetworkResult<BazarrSystem>
    suspend fun getBadges(): NetworkResult<BazarrBadges>

    suspend fun getWantedEpisodes(): NetworkResult<WantedEpisodesResponse>
    suspend fun getWantedMovies(): NetworkResult<WantedMoviesResponse>

    suspend fun getEpisodes(seriesId: Long): NetworkResult<BazarrEpisodesResponse>
    suspend fun getMovie(radarrId: Long): NetworkResult<BazarrMoviesResponse>

    suspend fun getMovies(): NetworkResult<List<BazarrMovie>>
    suspend fun getSeries(): NetworkResult<List<BazarrSeries>>

    suspend fun getProviders(): NetworkResult<ProvidersResponse>
    suspend fun resetProviders(): NetworkResult<Unit>

    /** Manual provider search for a given episode/movie. */
    suspend fun searchEpisodeSubtitles(episodeId: Long): NetworkResult<List<ProviderSubtitle>>
    suspend fun searchMovieSubtitles(radarrId: Long): NetworkResult<List<ProviderSubtitle>>

    suspend fun autoSearchSeriesSubtitles(seriesId: Long): NetworkResult<Unit>
    suspend fun autoSearchMovieSubtitles(radarrId: Long): NetworkResult<Unit>

    /** Download a specific subtitle returned by a manual search. */
    suspend fun downloadEpisodeSubtitle(
        seriesId: Long,
        episodeId: Long,
        hi: Boolean,
        forced: Boolean,
        originalFormat: Boolean,
        provider: String,
        subtitle: String
    ): NetworkResult<Unit>

    suspend fun downloadMovieSubtitle(
        radarrId: Long,
        hi: Boolean,
        forced: Boolean,
        originalFormat: Boolean,
        provider: String,
        subtitle: String
    ): NetworkResult<Unit>

    /** Trigger Bazarr's automatic best-match search & download for a language. */
    suspend fun autoSearchEpisodeSubtitles(
        seriesId: Long,
        episodeId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean
    ): NetworkResult<Unit>

    suspend fun autoSearchMovieSubtitles(
        radarrId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean
    ): NetworkResult<Unit>

    /** Delete an existing subtitle file. */
    suspend fun deleteEpisodeSubtitle(
        seriesId: Long,
        episodeId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean,
        path: String
    ): NetworkResult<Unit>

    suspend fun deleteMovieSubtitle(
        radarrId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean,
        path: String
    ): NetworkResult<Unit>
}

class BazarrClientImpl(
    private val instance: Instance,
    private val httpClient: HttpClient
) : KoinComponent, BazarrClient {

    private val baseUrl: String
        get() = "${instance.getEffectiveBaseUrl()}/${instance.type.apiBase}"

    override suspend fun testConnection(): NetworkResult<Unit> =
        get(instance.type.testEndpoint)

    override suspend fun getSystemStatus(): NetworkResult<BazarrSystemStatus> =
        httpClient.safeGet("$baseUrl/system/status")

    override suspend fun getSystemSettings(): NetworkResult<BazarrSystem> =
        httpClient.safeGet("$baseUrl/system/settings")

    override suspend fun getBadges(): NetworkResult<BazarrBadges> =
        get("badges")

    override suspend fun getWantedEpisodes(): NetworkResult<WantedEpisodesResponse> =
        get("episodes/wanted")

    override suspend fun getWantedMovies(): NetworkResult<WantedMoviesResponse> =
        get("movies/wanted")

    override suspend fun getEpisodes(seriesId: Long): NetworkResult<BazarrEpisodesResponse> =
        get("episodes", mapOf("seriesid[]" to seriesId))

    override suspend fun getMovie(radarrId: Long): NetworkResult<BazarrMoviesResponse> =
        get("movies", mapOf("radarrid[]" to radarrId))

    override suspend fun getSeries(): NetworkResult<List<BazarrSeries>> =
        get<BazarrSeriesResponse>("series").map { it.data }

    override suspend fun getMovies(): NetworkResult<List<BazarrMovie>> =
        get<BazarrMoviesResponse>("movies").map { it.data }

    override suspend fun getProviders(): NetworkResult<ProvidersResponse> =
        get("providers")

    override suspend fun resetProviders(): NetworkResult<Unit> =
        post("providers", mapOf("action" to "reset"))

    override suspend fun searchEpisodeSubtitles(episodeId: Long): NetworkResult<List<ProviderSubtitle>> =
        get<ProviderSubtitlesResponse>(
            "providers/episodes",
            mapOf("episodeid" to episodeId),
            timeoutMillis = SEARCH_TIMEOUT_MS
        ).map { it.data }

    override suspend fun searchMovieSubtitles(radarrId: Long): NetworkResult<List<ProviderSubtitle>> =
        get<ProviderSubtitlesResponse>(
            "providers/movies",
            mapOf("radarrid" to radarrId),
            timeoutMillis = SEARCH_TIMEOUT_MS
        ).map { it.data }

    override suspend fun downloadEpisodeSubtitle(
        seriesId: Long,
        episodeId: Long,
        hi: Boolean,
        forced: Boolean,
        originalFormat: Boolean,
        provider: String,
        subtitle: String
    ): NetworkResult<Unit> =
        post(
            "providers/episodes",
            mapOf(
                "seriesid" to seriesId,
                "episodeid" to episodeId,
                "hi" to hi.asPyBool(),
                "forced" to forced.asPyBool(),
                "original_format" to originalFormat.asPyBool(),
                "provider" to provider,
                "subtitle" to subtitle
            ),
            timeoutMillis = SEARCH_TIMEOUT_MS
        )

    override suspend fun downloadMovieSubtitle(
        radarrId: Long,
        hi: Boolean,
        forced: Boolean,
        originalFormat: Boolean,
        provider: String,
        subtitle: String
    ): NetworkResult<Unit> =
        post(
            "providers/movies",
            mapOf(
                "radarrid" to radarrId,
                "hi" to hi.asPyBool(),
                "forced" to forced.asPyBool(),
                "original_format" to originalFormat.asPyBool(),
                "provider" to provider,
                "subtitle" to subtitle
            ),
            timeoutMillis = SEARCH_TIMEOUT_MS
        )

    override suspend fun autoSearchEpisodeSubtitles(
        seriesId: Long,
        episodeId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean
    ): NetworkResult<Unit> =
        patch(
            "episodes/subtitles",
            mapOf(
                "seriesid" to seriesId,
                "episodeid" to episodeId,
                "language" to language,
                "forced" to forced.asPyBool(),
                "hi" to hi.asPyBool()
            ),
            timeoutMillis = SEARCH_TIMEOUT_MS
        )

    override suspend fun autoSearchMovieSubtitles(
        radarrId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean
    ): NetworkResult<Unit> =
        patch(
            "movies/subtitles",
            mapOf(
                "radarrid" to radarrId,
                "language" to language,
                "forced" to forced.asPyBool(),
                "hi" to hi.asPyBool()
            ),
            timeoutMillis = SEARCH_TIMEOUT_MS
        )

    override suspend fun autoSearchSeriesSubtitles(seriesId: Long): NetworkResult<Unit> =
        patch("series", body = AutoSearchBody(seriesid = seriesId))

    override suspend fun autoSearchMovieSubtitles(radarrId: Long): NetworkResult<Unit> =
        patch("movies", body = AutoSearchBody(radarrid = radarrId))

    override suspend fun deleteEpisodeSubtitle(
        seriesId: Long,
        episodeId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean,
        path: String
    ): NetworkResult<Unit> =
        delete(
            "episodes/subtitles",
            mapOf(
                "seriesid" to seriesId,
                "episodeid" to episodeId,
                "language" to language,
                "forced" to forced.asPyBool(),
                "hi" to hi.asPyBool(),
                "path" to path
            )
        )

    override suspend fun deleteMovieSubtitle(
        radarrId: Long,
        language: String,
        forced: Boolean,
        hi: Boolean,
        path: String
    ): NetworkResult<Unit> =
        delete(
            "movies/subtitles",
            mapOf(
                "radarrid" to radarrId,
                "language" to language,
                "forced" to forced.asPyBool(),
                "hi" to hi.asPyBool(),
                "path" to path
            )
        )

    /**
     * Helpers — Bazarr reads arguments from the query string for these endpoints, so all
     * params are appended to the URL regardless of HTTP method.
     */

    private fun Boolean.asPyBool(): String = if (this) "True" else "False"

    private fun HttpRequestBuilder.applyParams(
        params: Map<String, Any>,
        timeoutMillis: Long?
    ) {
        timeoutMillis?.let { ms ->
            timeout {
                requestTimeoutMillis = ms
                socketTimeoutMillis = ms
            }
        }
        url { params.forEach { (key, value) -> parameters.append(key, value.toString()) } }
    }

    private suspend inline fun <reified T> get(
        endpoint: String,
        params: Map<String, Any> = emptyMap(),
        timeoutMillis: Long? = null
    ): NetworkResult<T> =
        httpClient.safeGet<T>("$baseUrl/$endpoint") { applyParams(params, timeoutMillis) }

    private suspend inline fun <reified T> post(
        endpoint: String,
        params: Map<String, Any> = emptyMap(),
        timeoutMillis: Long? = null
    ): NetworkResult<T> =
        httpClient.safePost<T>("$baseUrl/$endpoint") { applyParams(params, timeoutMillis) }

    private suspend inline fun <reified T> patch(
        endpoint: String,
        params: Map<String, Any> = emptyMap(),
        body: Any? = null,
        timeoutMillis: Long? = null
    ): NetworkResult<T> =
        httpClient.safePatch("$baseUrl/$endpoint") {
            applyParams(params, timeoutMillis)
            body?.let {
                contentType(ContentType.Application.Json)
                setBody(it)
            }
        }

    private suspend inline fun <reified T> delete(
        endpoint: String,
        params: Map<String, Any> = emptyMap()
    ): NetworkResult<T> =
        httpClient.safeDelete<T>("$baseUrl/$endpoint") {
            url { params.forEach { (key, value) -> parameters.append(key, value.toString()) } }
        }
}
