package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CommandResponse
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.ListenarrRelease
import com.dnfapps.arrmatey.arr.api.model.MonitoredResponse
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.instances.model.Instance
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate

class ListenarrClient(
    override val instance: Instance,
    httpClient: HttpClient
): BaseArrClient(httpClient), ArrClient {

    override suspend fun getLibrary(): NetworkResult<List<Audiobook>> =
        get("library")

    override suspend fun getDetail(id: Long): NetworkResult<Audiobook> =
        get("library/$id")

    override suspend fun update(item: ArrMedia): NetworkResult<Audiobook> =
        put<ArrMedia, Audiobook>("library/${item.id}", item)

    override suspend fun edit(
        item: ArrMedia,
        moveFiles: Boolean
    ): NetworkResult<Unit> =
        put("library/${item.id}", item)

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

    override suspend fun lookup(query: String): NetworkResult<List<Audiobook>> =
        get("search/intelligent", mapOf("query" to query))

    override suspend fun addItemToLibrary(item: ArrMedia): NetworkResult<Audiobook> =
        post("library/add", item)

    override suspend fun performAutomaticSearch(id: Long): NetworkResult<CommandResponse> =
        post("download/search-and-download", mapOf("audiobookId" to id))

    override suspend fun getReleases(params: ReleaseParams): NetworkResult<List<ListenarrRelease>> {
        val audiobookId = (params as? ReleaseParams.Book)?.bookId
        val query = when (params) {
            is ReleaseParams.Book -> {
                val detail = getDetail(params.bookId)
                if (detail is NetworkResult.Success) {
                    "${detail.data.authors.firstOrNull() ?: ""} ${detail.data.title}"
                } else ""
            }
            else -> ""
        }
        return get<List<ListenarrRelease>>("search/indexers", mapOf("query" to query))
            .map { releases -> releases.map { it.copy(audiobookId = audiobookId) } }
    }

    override suspend fun getItemHistory(
        id: Long,
        page: Int,
        pageSize: Int,
        altId: Long?
    ): NetworkResult<List<HistoryItem>> =
        get("history/audiobook/$id")

    override suspend fun getMovieCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<ArrMovie>> = NetworkResult.Success(emptyList())

    override suspend fun getEpisodeCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<Episode>> = NetworkResult.Success(emptyList())

    override suspend fun getAlbumCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<ArrAlbum>> = NetworkResult.Success(emptyList())

    override suspend fun getBookCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<Book>> = NetworkResult.Success(emptyList())
}
