package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.AddAudiobookBody
import com.dnfapps.arrmatey.arr.api.model.AddAudiobookResponse
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.AudiobookEditResponse
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataBody
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataResponse
import com.dnfapps.arrmatey.arr.api.model.AudiobookPreviewPaths
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.DownloadReleasePayload
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.ListenarrCommandResponse
import com.dnfapps.arrmatey.arr.api.model.ListenarrConfiguration
import com.dnfapps.arrmatey.arr.api.model.ListenarrIndexer
import com.dnfapps.arrmatey.arr.api.model.ListenarrQueueResponse
import com.dnfapps.arrmatey.arr.api.model.ListenarrRelease
import com.dnfapps.arrmatey.arr.api.model.MonitorBody
import com.dnfapps.arrmatey.arr.api.model.MonitoredResponse
import com.dnfapps.arrmatey.arr.api.model.PreviewPathBody
import com.dnfapps.arrmatey.arr.api.model.QueuePage
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.api.model.toEditBody
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.filterValues
import com.dnfapps.arrmatey.client.mapValues
import com.dnfapps.arrmatey.extensions.isBetween
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ListenarrClient(
    override val instance: Instance,
    httpClient: HttpClient
): BaseArrClient(httpClient), ArrClient {

    override suspend fun getLibrary(): NetworkResult<List<Audiobook>> =
        get("library")

    override suspend fun getDetail(id: Long): NetworkResult<Audiobook> =
        get("library/$id")

    override suspend fun update(item: ArrMedia): NetworkResult<Audiobook> =
        put<MonitorBody, AudiobookEditResponse>("library/${item.id}", MonitorBody(item.monitored))
            .map { it.audiobook }
            .rebuild()

    override suspend fun edit(
        item: ArrMedia,
        moveFiles: Boolean
    ): NetworkResult<Unit> =
        (item as? Audiobook)?.let { audiobook ->
            put("library/${item.id}", audiobook.toEditBody())
        } ?: NetworkResult.Error(message = "Item must be an Audiobook")

    override suspend fun delete(
        id: Long,
        deleteFiles: Boolean,
        addImportListExclusion: Boolean
    ): NetworkResult<Unit> =
        delete(
            endpoint = "library/$id",
            params = mapOf(
                "deleteFiles" to deleteFiles,
                "deleteFolder" to deleteFiles
            )
        )

    override suspend fun setMonitorStatus(
        id: Long,
        monitorStatus: Boolean
    ): NetworkResult<List<MonitoredResponse>> {
        val detailResult = getDetail(id)
        if (detailResult is NetworkResult.Success) {
            val updated = detailResult.data.copy(monitored = monitorStatus)
            val updateResult = update(updated)
            if (updateResult is NetworkResult.Success) {
                return NetworkResult.Success(emptyList())
            }
        }
        return NetworkResult.Error(message = "Failed to update monitor status")
    }

    override suspend fun lookup(params: LookupParams): NetworkResult<List<SearchAudiobook>> =
        post("search", buildJsonObject {
            put("title", params.query)
            put("language", params.language ?: "english")
            put("region", params.region ?: "us")
            put("mode", "Advanced")
            put("pagination", buildJsonObject {
                put("page", 1)
                put("limit", 100)
            })
        })

    override suspend fun addItemToLibrary(item: ArrMedia): NetworkResult<Audiobook> =
        NetworkResult.Error(message = "Use addNewAudiobook instead")

    suspend fun addNewAudiobook(body: AddAudiobookBody): NetworkResult<AddAudiobookResponse> =
        post("library/add", body)


    override suspend fun performAutomaticSearch(id: Long): NetworkResult<Any> =
        post<Map<String, Long>, ListenarrCommandResponse>("download/search-and-download", mapOf("audiobookId" to id))

    override suspend fun getReleases(params: ReleaseParams): NetworkResult<List<ListenarrRelease>> {
        val query = (params as? ReleaseParams.Audiobook)?.query
            ?: return NetworkResult.Error(message = "Query can't be empty")
        return get<List<ListenarrRelease>>("search/indexers", mapOf("query" to query))
    }

    override suspend fun downloadRelease(payload: DownloadReleasePayload): NetworkResult<Any> =
        post("download/send", payload)

    override suspend fun getRootFolders(): NetworkResult<List<RootFolder>> =
        get("rootfolders")

    override suspend fun getItemHistory(
        id: Long,
        page: Int,
        pageSize: Int,
        altId: Long?
    ): NetworkResult<List<HistoryItem>> =
        get("history/audiobook/$id")

    override suspend fun getCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<Audiobook>> =
        get<List<Audiobook>>("library")
            .filterValues { it.publishedDate.isBetween(start, end) }

    override suspend fun command(payload: CommandPayload): NetworkResult<Any> =
        when (payload) {
            is CommandPayload.Audiobook -> post<CommandPayload.Audiobook, ListenarrCommandResponse>("download/search-and-download", payload)
            else -> super.command(payload)
        }

    override suspend fun fetchActivityTasks(page: Int, pageSize: Int): NetworkResult<QueuePage> =
        get<ListenarrQueueResponse>("download/queue")
            .map {
                QueuePage(page, pageSize, it.items.size, it.items)
                    .setInstance(instance.id, instance.label)
            }

    suspend fun getEnabledIndexers(): NetworkResult<List<ListenarrIndexer>> =
        get("indexers/enabled")

    suspend fun getConfigurationSettings(): NetworkResult<ListenarrConfiguration> =
        get("configuration/settings")

    suspend fun getMetadata(asin: String, region: String): NetworkResult<AudiobookMetadataResponse> =
        get("metadata/$asin", mapOf("region" to region))

    suspend fun getPreviewPath(rootPath: String, metadata: AudiobookMetadataBody): NetworkResult<AudiobookPreviewPaths> =
        post("library/preview-path", PreviewPathBody(rootPath, metadata))

    suspend fun moveFiles(
        id: Long,
        moveFiles: Boolean,
        sourcePath: String,
        destinationPath: String
    ): NetworkResult<Unit> =
        post("library/$id/move", buildJsonObject {
            put("moveFiles", moveFiles)
            put("deleteEmptySources", true)
            put("sourcePath", sourcePath)
            put("destinationPath", destinationPath)
        })
}
