package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CommandResponse
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
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

    override suspend fun getLibrary(): NetworkResult<List<ArrMedia>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDetail(id: Long): NetworkResult<ArrMedia> {
        TODO("Not yet implemented")
    }

    override suspend fun update(item: ArrMedia): NetworkResult<ArrMedia> {
        TODO("Not yet implemented")
    }

    override suspend fun edit(
        item: ArrMedia,
        moveFiles: Boolean
    ): NetworkResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(
        id: Long,
        deleteFiles: Boolean,
        addImportListExclusion: Boolean
    ): NetworkResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun setMonitorStatus(
        id: Long,
        monitorStatus: Boolean
    ): NetworkResult<List<MonitoredResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun lookup(query: String): NetworkResult<List<ArrMedia>> {
        TODO("Not yet implemented")
    }

    override suspend fun addItemToLibrary(item: ArrMedia): NetworkResult<ArrMedia> {
        TODO("Not yet implemented")
    }

    override suspend fun performAutomaticSearch(id: Long): NetworkResult<CommandResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun getReleases(params: ReleaseParams): NetworkResult<List<ArrRelease>> {
        TODO("Not yet implemented")
    }

    override suspend fun getItemHistory(
        id: Long,
        page: Int,
        pageSize: Int,
        altId: Long?
    ): NetworkResult<List<HistoryItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMovieCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<ArrMovie>> {
        TODO("Not yet implemented")
    }

    override suspend fun getEpisodeCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<Episode>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAlbumCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<ArrAlbum>> {
        TODO("Not yet implemented")
    }

    override suspend fun getBookCalendar(
        start: LocalDate,
        end: LocalDate
    ): NetworkResult<List<Book>> {
        TODO("Not yet implemented")
    }


}