package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ApplyTags
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.CommandResponse
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.arr.api.model.MonitoredResponse
import com.dnfapps.arrmatey.arr.api.model.MovieEditorBody
import com.dnfapps.arrmatey.arr.api.model.MovieRelease
import com.dnfapps.arrmatey.arr.api.model.RadarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate

class RadarrClient(
    override val instance: Instance,
    httpClient: HttpClient
): BaseArrClient(httpClient), ArrClient {

    override suspend fun getLibrary(): NetworkResult<List<ArrMovie>> =
        get<List<ArrMovie>>("movie")
            .onSuccess { movies ->
                movies.map { movie ->
                    movie.copy(
                        images = movie.images.map { image ->
                            if (image.remoteUrl?.startsWith("/") == true) {
                                image.copy(remoteUrl = "$baseUrl${image.remoteUrl}")
                            } else {
                                image
                            }
                        }
                    )
                }
            }

    override suspend fun getDetail(id: Long): NetworkResult<ArrMovie> =
        get("movie/$id")

    override suspend fun update(item: ArrMedia): NetworkResult<ArrMovie> =
        put("movie/${item.id}", item)

    override suspend fun edit(item: ArrMedia, moveFiles: Boolean): NetworkResult<Unit> {
        val movie = item as? ArrMovie
            ?: return NetworkResult.Error(message = "Item must be an ArrMovie")
        val id = item.id
            ?: return NetworkResult.Error(message = "Id must not be null")
        val body = MovieEditorBody(
            movieIds = listOf(id),
            monitored = movie.monitored,
            qualityProfileId = movie.qualityProfileId,
            minimumAvailability = movie.minimumAvailability,
            rootFolderPath = movie.rootFolderPath,
            tags = movie.tags,
            applyTags = ApplyTags.Replace,
            moveFiles = moveFiles,
        )
        return put("movie/editor", body = body)
    }

    override suspend fun delete(
        id: Long,
        deleteFiles: Boolean,
        addImportListExclusion: Boolean
    ): NetworkResult<Unit> =
        delete(
            endpoint = "movie/$id",
            params = mapOf(
                "deleteFiles" to deleteFiles,
                "addImportListExclusion" to addImportListExclusion
            )
        )

    override suspend fun setMonitorStatus(
        id: Long,
        monitorStatus: Boolean
    ): NetworkResult<List<MonitoredResponse>> =
        put("movie/editor", mapOf(
            "monitored" to monitorStatus,
            "movieIds" to listOf(id)
        ))

    override suspend fun lookup(query: String): NetworkResult<List<ArrMovie>> =
        get("movie/lookup", mapOf("term" to query))

    override suspend fun addItemToLibrary(item: ArrMedia): NetworkResult<ArrMovie> =
        post("movie", item)

    override suspend fun getReleases(params: ReleaseParams): NetworkResult<List<MovieRelease>> {
        if (params !is ReleaseParams.Movie) {
            return NetworkResult.Error(message = "Non-movie params type: $params")
        }
        return get("release", mapOf("movieId" to params.movieId))
    }

    override suspend fun getItemHistory(
        id: Long,
        page: Int,
        pageSize: Int
    ): NetworkResult<List<RadarrHistoryItem>> =
        get("history/movie", mapOf(
            "page" to page,
            "pageSize" to pageSize,
            "movieId" to id
        ))

    override suspend fun performAutomaticSearch(id: Long): NetworkResult<CommandResponse> =
        post("command", CommandPayload.Movie(listOf(id)))

    override suspend fun getMovieCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<ArrMovie>> =
        get<List<ArrMovie>>("calendar", mapOf(
            "start" to start.toString(),
            "end" to end.toString(),
            "unmonitored" to true
        )).map { it.map { movie -> movie.copy(instanceId = instance.id) } }

    override suspend fun getEpisodeCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<Episode>> = NetworkResult.Success(emptyList())

    override suspend fun getAlbumCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<ArrAlbum>> = NetworkResult.Success(emptyList())

    suspend fun getMovieExtraFile(id: Long): NetworkResult<List<ExtraFile>> =
        get("extrafile", mapOf("movieId" to id))

}