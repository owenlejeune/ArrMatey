package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ApplyTags
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.CommandResponse
import com.dnfapps.arrmatey.arr.api.model.DeleteEpisodeBody
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.MonitoredResponse
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.SeriesEditorBody
import com.dnfapps.arrmatey.arr.api.model.SeriesRelease
import com.dnfapps.arrmatey.arr.api.model.SonarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.SonarrHistoryResponse
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate

class SonarrClient(
    override val instance: Instance,
    httpClient: HttpClient
) : BaseArrClient(httpClient), ArrClient {

    override suspend fun getLibrary(): NetworkResult<List<ArrSeries>> =
        get<List<ArrSeries>>("series")
            .onSuccess { shows ->
                shows.map { series ->
                    series.copy(
                        images = series.images.map { image ->
                            if (image.remoteUrl?.startsWith("/") == true) {
                                image.copy(remoteUrl = "$baseUrl${image.remoteUrl}")
                            } else {
                                image
                            }
                        }
                    )
                }
            }

    override suspend fun getDetail(id: Long): NetworkResult<ArrSeries> =
        get("series/$id")

    override suspend fun update(item: ArrMedia): NetworkResult<ArrSeries> =
        put("series/${item.id}", item)

    override suspend fun edit(item: ArrMedia, moveFiles: Boolean): NetworkResult<Unit> {
        val series = item as? ArrSeries
            ?: return NetworkResult.Error(message = "Item must be an ArrSeries")
        val id = series.id
            ?: return NetworkResult.Error(message = "Item id cannot be null")
        val body = SeriesEditorBody(
            seriesIds = listOf(id),
            monitored = series.monitored,
            monitorNewItems = series.monitorNewItems,
            seriesType = series.seriesType,
            seasonFolder = series.seasonFolder,
            qualityProfileId = series.qualityProfileId,
            rootFolderPath = series.rootFolderPath,
            tags = series.tags,
            applyTags = ApplyTags.Replace,
            moveFiles = moveFiles,
        )
        return put("series/editor", body = body)
    }

    override suspend fun delete(
        id: Long,
        deleteFiles: Boolean,
        addImportListExclusion: Boolean
    ): NetworkResult<Unit> =
        delete(
            endpoint = "series/$id",
            params = mapOf(
                "deleteFiles" to deleteFiles,
                "addImportListExclusion" to addImportListExclusion
            )
        )

    override suspend fun lookup(query: String): NetworkResult<List<ArrSeries>> =
        get("series/lookup", mapOf("term" to query))

    override suspend fun addItemToLibrary(item: ArrMedia): NetworkResult<ArrSeries> =
        post<ArrMedia, ArrSeries>("series", item)

    override suspend fun getReleases(params: ReleaseParams): NetworkResult<List<SeriesRelease>> {
        if (params !is ReleaseParams.Series) {
            return NetworkResult.Error(message = "Non-series params type: $params")
        }

        val paramsMap = params.episodeId
            ?.let { epId -> mapOf("episodeId" to epId) }
            ?: buildMap<String, Any> {
                params.seriesId?.let { put("seriesId", it) }
                params.seasonNumber?.let { put("seasonNumber", it) }
            }

        return get("release", paramsMap)
    }

    override suspend fun setMonitorStatus(
        id: Long,
        monitorStatus: Boolean
    ): NetworkResult<List<MonitoredResponse>> =
        put("series/editor", mapOf(
            "monitored" to monitorStatus,
            "seriesIds" to listOf(id)
        ))

    override suspend fun getItemHistory(
        id: Long,
        page: Int,
        pageSize: Int
    ): NetworkResult<List<SonarrHistoryItem>> =
        get<SonarrHistoryResponse>("history", mapOf(
            "page" to page,
            "pageSize" to pageSize,
            "episodeId" to id
        )).map { it.records }

    override suspend fun performAutomaticSearch(id: Long): NetworkResult<CommandResponse> =
        post("command", CommandPayload.Series(id))

    override suspend fun getEpisodeCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<Episode>> =
        get<List<Episode>>("calendar", mapOf(
            "start" to start.toString(),
            "end" to end.toString(),
            "unmonitored" to true,
            "includeSeries" to true
        )).map { it.map { ep -> ep.copy(instanceId = instance.id) } }

    override suspend fun getMovieCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<ArrMovie>> = NetworkResult.Success(emptyList())

    override suspend fun getAlbumCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<ArrAlbum>> = NetworkResult.Success(emptyList())

    suspend fun updateEpisode(item: Episode): NetworkResult<Episode> =
        put("episode/${item.id}", item)

    suspend fun getEpisodes(
        seriesId: Long,
        seasonNumber: Int? = null,
        includeEpisodeFile: Boolean = true,
        includeImages: Boolean = true
    ): NetworkResult<List<Episode>> =
        get("episode", buildMap {
            put("seriesId", seriesId)
            seasonNumber?.let { put("seasonNumber", it) }
            if (includeEpisodeFile) put("includeEpisodeFile", true)
            if (includeImages) put("includeImages", true)
        })

    suspend fun deleteEpisodes(fileIds: List<Long>): NetworkResult<Unit> =
        delete(
            endpoint = "episodefile/bulk",
            body = DeleteEpisodeBody(fileIds)
        )

    suspend fun deleteEpisode(id: Long): NetworkResult<Unit> =
        delete("episodefile/$id")

}