package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrImage
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.arr.api.model.CalendarItem
import com.dnfapps.arrmatey.arr.api.model.ComicVolume
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.CoverType
import com.dnfapps.arrmatey.arr.api.model.DownloadReleasePayload
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.KapowarrAboutResponse
import com.dnfapps.arrmatey.arr.api.model.KapowarrAddVolumeBody
import com.dnfapps.arrmatey.arr.api.model.KapowarrDownloadBody
import com.dnfapps.arrmatey.arr.api.model.KapowarrHistoryResponse
import com.dnfapps.arrmatey.arr.api.model.KapowarrQueueResponse
import com.dnfapps.arrmatey.arr.api.model.KapowarrReleasesResponse
import com.dnfapps.arrmatey.arr.api.model.KapowarrRootFolderResponse
import com.dnfapps.arrmatey.arr.api.model.KapowarrSearchResponse
import com.dnfapps.arrmatey.arr.api.model.KapowarrUpdateVolumeBody
import com.dnfapps.arrmatey.arr.api.model.MetadataProfile
import com.dnfapps.arrmatey.arr.api.model.MonitoredResponse
import com.dnfapps.arrmatey.arr.api.model.MonitoringScheme
import com.dnfapps.arrmatey.arr.api.model.QueuePage
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SpecialVersion
import com.dnfapps.arrmatey.arr.api.model.VolumeDetailsResponse
import com.dnfapps.arrmatey.arr.api.model.VolumesResponse
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.networking.NetworkResult
import io.ktor.client.HttpClient
import kotlinx.datetime.LocalDate

class KapowarrClient(
    override val instance: Instance,
    httpClient: HttpClient,
) : BaseArrClient(httpClient),
    ArrClient {

    override suspend fun getLibrary(): NetworkResult<List<ArrMedia>> {
        val response = get<VolumesResponse>("volumes")
        return when (response) {
            is NetworkResult.Success -> {
                if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    val apiKeyQuery = if (instance.apiKey.value.isNotBlank()) "?api_key=${instance.apiKey.value}" else ""
                    val items = response.data.result.orEmpty().map { volume ->
                        val coverUrl = volume.id?.let { id ->
                            "${instance.getEffectiveBaseUrl()}/api/volumes/$id/cover$apiKeyQuery"
                        }
                        val images = if (coverUrl != null) {
                            listOf(ArrImage(coverType = CoverType.Poster, url = coverUrl, remoteUrl = coverUrl))
                        } else {
                            emptyList()
                        }

                        volume.copy(images = images, instanceId = instance.id)
                    }
                    NetworkResult.Success(items)
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun getDetail(id: Long): NetworkResult<ArrMedia> {
        val response = get<VolumeDetailsResponse>("volumes/$id")
        return when (response) {
            is NetworkResult.Success -> {
                if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    val volume = response.data.result
                    if (volume != null) {
                        val apiKeyQuery = if (instance.apiKey.value.isNotBlank()) "?api_key=${instance.apiKey.value}" else ""
                        val coverUrl = volume.id?.let { volId ->
                            "${instance.getEffectiveBaseUrl()}/api/volumes/$volId/cover$apiKeyQuery"
                        }
                        val images = if (coverUrl != null) {
                            listOf(ArrImage(coverType = CoverType.Poster, url = coverUrl, remoteUrl = coverUrl))
                        } else {
                            emptyList()
                        }

                        NetworkResult.Success(volume.copy(images = images, instanceId = instance.id))
                    } else {
                        NetworkResult.Error(message = "Volume not found")
                    }
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun update(item: ArrMedia): NetworkResult<ArrMedia> {
        val volume = item as? ComicVolume ?: return NetworkResult.Error(message = "Item must be a ComicVolume")
        val volumeId = volume.id ?: return NetworkResult.Error(message = "Volume ID is missing")

        val specialVer = SpecialVersion.entries.firstOrNull { it.value.equals(volume.specialVersion, ignoreCase = true) } ?: SpecialVersion.Automatic

        val body = KapowarrUpdateVolumeBody(
            comicvineId = volume.comicvineId,
            rootFolder = volume.rootFolder?.toLong(),
            monitored = volume.monitored,
            monitoringScheme = volume.monitoringScheme ?: MonitoringScheme.All,
            monitorNewIssues = volume.monitorNewIssues,
            volumeFolder = volume.volumeFolder ?: volume.folder,
            specialVersion = specialVer,
            specialVersionLocked = volume.specialVersionLocked,
        )

        val response = put<KapowarrUpdateVolumeBody, VolumeDetailsResponse>("volumes/$volumeId", body)
        return when (response) {
            is NetworkResult.Success -> {
                val updatedVol = response.data.result
                if (updatedVol != null) {
                    val apiKeyQuery = if (instance.apiKey.value.isNotBlank()) "?api_key=${instance.apiKey.value}" else ""
                    val coverUrl = updatedVol.id?.let { volId ->
                        "${instance.getEffectiveBaseUrl()}/api/volumes/$volId/cover$apiKeyQuery"
                    }
                    val images = if (coverUrl != null) {
                        listOf(ArrImage(coverType = CoverType.Poster, url = coverUrl, remoteUrl = coverUrl))
                    } else {
                        emptyList()
                    }

                    NetworkResult.Success(updatedVol.copy(images = images, instanceId = instance.id))
                } else if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    NetworkResult.Error(message = "Failed to update volume")
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun edit(
        item: ArrMedia,
        moveFiles: Boolean,
    ): NetworkResult<Unit> = when (val result = update(item)) {
        is NetworkResult.Success -> NetworkResult.Success(Unit)
        is NetworkResult.Error -> result
        is NetworkResult.Loading -> result
    }

    override suspend fun delete(
        id: Long,
        deleteFiles: Boolean,
        addImportExclusion: Boolean,
    ): NetworkResult<Unit> = delete<Unit>(
        "volumes/$id",
        mapOf("delete_folder" to deleteFiles),
    )

    override suspend fun setMonitorStatus(
        id: Long,
        monitorStatus: Boolean,
    ): NetworkResult<List<MonitoredResponse>> {
        val body = KapowarrUpdateVolumeBody(monitored = monitorStatus)
        val response = put<KapowarrUpdateVolumeBody, VolumeDetailsResponse>("volumes/$id", body)
        return when (response) {
            is NetworkResult.Success -> {
                if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    NetworkResult.Success(listOf(MonitoredResponse(id = id, monitored = monitorStatus)))
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun lookup(params: LookupParams): NetworkResult<List<ArrMedia>> {
        val response = get<KapowarrSearchResponse>(
            "volumes/search",
            mapOf("query" to params.query),
        )
        return when (response) {
            is NetworkResult.Success -> {
                if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    val volumes = response.data.result.orEmpty().map { result ->
                        result.toComicVolume().copy(instanceId = instance.id)
                    }
                    NetworkResult.Success(volumes)
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun getMetadataProfiles(): NetworkResult<List<MetadataProfile>> = NetworkResult.Success(emptyList())

    override suspend fun getRootFolders(): NetworkResult<List<RootFolder>> {
        val response = get<KapowarrRootFolderResponse>("rootfolder")
        return when (response) {
            is NetworkResult.Success -> {
                if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    val rootFolders = response.data.result.orEmpty().map { it.toRootFolder() }
                    NetworkResult.Success(rootFolders)
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun addItemToLibrary(item: ArrMedia): NetworkResult<ArrMedia> {
        val volume = item as? ComicVolume ?: return NetworkResult.Error(message = "Item must be a ComicVolume")
        val comicvineId = volume.comicvineId ?: return NetworkResult.Error(message = "Comicvine ID is missing")
        val rootFolderId = volume.rootFolder?.toLong() ?: 1L
        val volFolder = volume.volumeFolder ?: volume.folder ?: ""

        val specialVer = SpecialVersion.entries.firstOrNull { it.value.equals(volume.specialVersion, ignoreCase = true) } ?: SpecialVersion.Automatic

        val body = KapowarrAddVolumeBody(
            comicvineId = comicvineId,
            rootFolderId = rootFolderId,
            monitor = volume.monitored,
            monitoringScheme = volume.monitoringScheme ?: MonitoringScheme.All,
            monitorNewIssues = volume.monitorNewIssues,
            volumeFolder = volFolder,
            specialVersion = specialVer,
            autoSearch = volume.searchOnAdd,
        )

        val response = post<KapowarrAddVolumeBody, VolumeDetailsResponse>("volumes", body)
        return when (response) {
            is NetworkResult.Success -> {
                val addedVol = response.data.result
                if (addedVol != null) {
                    NetworkResult.Success(addedVol.copy(instanceId = instance.id))
                } else if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    NetworkResult.Error(message = "Failed to add volume")
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun command(payload: CommandPayload): NetworkResult<Any> {
        return when (payload) {
            is CommandPayload.Volume -> {
                val id = payload.volumeIds.firstOrNull() ?: return NetworkResult.Success(Unit)
                post("volumes/$id/search", emptyMap<String, String>())
            }
            is CommandPayload.Issue -> {
                val id = payload.issueIds.firstOrNull() ?: return NetworkResult.Success(Unit)
                post("issues/$id/search", emptyMap<String, String>())
            }
            else -> super.command(payload)
        }
    }

    override suspend fun performAutomaticSearch(id: Long): NetworkResult<Any> = post("volumes/$id/search", emptyMap<String, String>())

    override suspend fun getReleases(params: ReleaseParams): NetworkResult<List<ArrRelease>> {
        val mediaId = params.mediaId ?: return NetworkResult.Success(emptyList())
        val endpoint = when (params) {
            is ReleaseParams.ComicVolume -> "volumes/$mediaId/manualsearch"
            else -> "issues/$mediaId/manualsearch"
        }
        val response = get<KapowarrReleasesResponse>(endpoint)
        return when (response) {
            is NetworkResult.Success -> {
                if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    val releases = response.data.result.orEmpty().map { release ->
                        release.apply { this.mediaId = mediaId }
                    }
                    NetworkResult.Success(releases)
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun getItemHistory(
        id: Long,
        page: Int,
        pageSize: Int,
        altId: Long?,
    ): NetworkResult<List<HistoryItem>> {
        val response = get<KapowarrHistoryResponse>("activity/history")
        return when (response) {
            is NetworkResult.Success -> {
                if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    val items = response.data.result.orEmpty()
                        .let { list ->
                            if (id > 0) {
                                list.filter { it.volumeId == id || it.issueId == id || (altId != null && (it.volumeId == altId || it.issueId == altId)) }
                            } else {
                                list
                            }
                        }
                        .map { it.copy(instanceId = instance.id, instanceName = instance.label, instanceType = instance.type) }
                    NetworkResult.Success(items)
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun getHistory(
        page: Int,
        pageSize: Int,
    ): NetworkResult<List<HistoryItem>> {
        val response = get<KapowarrHistoryResponse>("activity/history")
        return when (response) {
            is NetworkResult.Success -> {
                if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    val items = response.data.result.orEmpty().map {
                        it.copy(instanceId = instance.id, instanceName = instance.label, instanceType = instance.type)
                    }
                    NetworkResult.Success(items)
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun fetchActivityTasks(
        page: Int,
        pageSize: Int,
    ): NetworkResult<QueuePage> {
        val response = get<KapowarrQueueResponse>("activity/queue")
        return when (response) {
            is NetworkResult.Success -> {
                if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    val items = response.data.result.orEmpty().map {
                        it.copy(instanceId = instance.id, instanceName = instance.label, instanceType = instance.type)
                    }
                    NetworkResult.Success(QueuePage(page = page, pageSize = pageSize, totalRecords = items.size, records = items))
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }

    override suspend fun deleteActivityTask(
        id: Int,
        removeFromClient: Boolean,
        blocklist: Boolean,
        skipRedownload: Boolean,
    ): NetworkResult<Unit> = delete<Unit>(
        "activity/queue/$id",
        mapOf("blocklist" to blocklist),
    )

    override suspend fun downloadRelease(payload: DownloadReleasePayload): NetworkResult<Any> {
        val comicPayload = payload as? DownloadReleasePayload.Comic
            ?: return NetworkResult.Error(message = "Invalid payload type")
        val body = KapowarrDownloadBody(
            link = comicPayload.guid,
            indexerId = comicPayload.indexerId,
            forceMatch = comicPayload.forceMatch,
        )
        val endpoint = when {
            comicPayload.issueId != null && comicPayload.issueId > 0 -> "issues/${comicPayload.issueId}/download"
            comicPayload.volumeId != null && comicPayload.volumeId > 0 -> "volumes/${comicPayload.volumeId}/download"
            else -> return NetworkResult.Error(message = "No issue or volume ID specified for download")
        }
        return post(endpoint, body)
    }

    override suspend fun getCalendar(
        start: LocalDate,
        end: LocalDate,
    ): NetworkResult<List<CalendarItem>> {
        // not supported by kapowarr
        return NetworkResult.Success(emptyList())
    }

    override suspend fun getDiskSpace(): NetworkResult<List<ArrDiskSpace>> {
        // no supported by kapowarr
        return NetworkResult.Success(emptyList())
    }

    override suspend fun getStatus(): NetworkResult<ArrSoftwareStatus> {
        val response = get<KapowarrAboutResponse>("system/about")
        return when (response) {
            is NetworkResult.Success -> {
                if (!response.data.error.isNullOrBlank()) {
                    NetworkResult.Error(message = response.data.error)
                } else {
                    NetworkResult.Success(response.data.result?.toArrSoftwareStatus() ?: ArrSoftwareStatus(appName = "Kapowarr"))
                }
            }
            is NetworkResult.Error -> response
            is NetworkResult.Loading -> response
        }
    }
}
