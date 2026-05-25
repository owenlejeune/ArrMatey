package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.CommandResponse
import com.dnfapps.arrmatey.arr.api.model.DownloadReleasePayload
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.MonitoredResponse
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.QueuePage
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.client.NetworkResult
import kotlinx.datetime.LocalDate

interface ArrClient {
    suspend fun testConnection(): NetworkResult<Unit>
    suspend fun getStatus(): NetworkResult<ArrSoftwareStatus>
    suspend fun getDiskSpace(): NetworkResult<List<ArrDiskSpace>>
    suspend fun getHealth(): NetworkResult<List<ArrHealth>>
    suspend fun getLibrary(): NetworkResult<List<ArrMedia>>
    suspend fun getDetail(id: Long): NetworkResult<ArrMedia>
    suspend fun update(item: ArrMedia): NetworkResult<ArrMedia>
    suspend fun edit(item: ArrMedia, moveFiles: Boolean = false): NetworkResult<Unit>
    suspend fun delete(id: Long, deleteFiles: Boolean, addImportListExclusion: Boolean): NetworkResult<Unit>
    suspend fun setMonitorStatus(id: Long, monitorStatus: Boolean): NetworkResult<List<MonitoredResponse>>
    suspend fun lookup(params: LookupParams): NetworkResult<List<ArrMedia>>
    suspend fun getQualityProfiles(): NetworkResult<List<QualityProfile>>
    suspend fun getRootFolders(): NetworkResult<List<RootFolder>>
    suspend fun getTags(): NetworkResult<List<Tag>>
    suspend fun addItemToLibrary(item: ArrMedia): NetworkResult<ArrMedia>
    suspend fun command(payload: CommandPayload): NetworkResult<Any>
    suspend fun performAutomaticSearch(id: Long): NetworkResult<Any>
    suspend fun getReleases(params: ReleaseParams): NetworkResult<List<ArrRelease>>
    suspend fun fetchActivityTasks(page: Int, pageSize: Int): NetworkResult<QueuePage>
    suspend fun deleteActivityTask(id: Int, removeFromClient: Boolean, blocklist: Boolean, skipRedownload: Boolean): NetworkResult<Unit>
    suspend fun getItemHistory(id: Long, page: Int, pageSize: Int, altId: Long? = null): NetworkResult<List<HistoryItem>>
    suspend fun downloadRelease(payload: DownloadReleasePayload): NetworkResult<Any>
    suspend fun getCalendar(start: LocalDate, end: LocalDate): NetworkResult<List<CalendarItem>>
}