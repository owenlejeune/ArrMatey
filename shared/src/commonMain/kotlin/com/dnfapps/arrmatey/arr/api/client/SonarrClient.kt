package com.dnfapps.arrmatey.arr.api.client

import com.dnfapps.arrmatey.arr.api.model.ApplyTags
import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.arr.api.model.ArrUpdate
import com.dnfapps.arrmatey.arr.api.model.BlocklistPage
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.CommandResponse
import com.dnfapps.arrmatey.arr.api.model.CustomFilter
import com.dnfapps.arrmatey.arr.api.model.DeleteEpisodeBody
import com.dnfapps.arrmatey.arr.api.model.DownloadReleasePayload
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.FilesystemItem
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.IdWrapper
import com.dnfapps.arrmatey.arr.api.model.Language
import com.dnfapps.arrmatey.arr.api.model.ManualImportFile
import com.dnfapps.arrmatey.arr.api.model.MediaStatus
import com.dnfapps.arrmatey.arr.api.model.MetadataProfile
import com.dnfapps.arrmatey.arr.api.model.MonitorNewItems
import com.dnfapps.arrmatey.arr.api.model.MonitoredResponse
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.QueuePage
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SeriesEditorBody
import com.dnfapps.arrmatey.arr.api.model.SeriesMonitorOption
import com.dnfapps.arrmatey.arr.api.model.SeriesMonitorType
import com.dnfapps.arrmatey.arr.api.model.SeriesMonitoringBody
import com.dnfapps.arrmatey.arr.api.model.SeriesRelease
import com.dnfapps.arrmatey.arr.api.model.SeriesType
import com.dnfapps.arrmatey.arr.api.model.SonarrHistoryItem
import com.dnfapps.arrmatey.arr.api.model.SonarrHistoryResponse
import com.dnfapps.arrmatey.arr.api.model.SonarrWantedPage
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.api.model.WantedPage
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.safeGet
import io.ktor.client.HttpClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

enum class SonarrApiVersion(val apiBase: String) {
    V3("api/v3"),
    V5("api/v5"),
}

@Serializable
private data class SonarrQueueBulkPayload(
    val ids: List<Int>,
)

class SonarrClient(
    override val instance: Instance,
    httpClient: HttpClient,
    initialApiVersion: SonarrApiVersion? = null,
) : BaseArrClient(httpClient),
    ArrClient {

    private var versionDetermined: Boolean = initialApiVersion != null

    private var _apiVersion: SonarrApiVersion = initialApiVersion ?: SonarrApiVersion.V3

    var apiVersion: SonarrApiVersion
        get() = _apiVersion
        set(value) {
            _apiVersion = value
            versionDetermined = true
        }

    override val apiBase: String
        get() = apiVersion.apiBase

    val isV5: Boolean
        get() = apiVersion == SonarrApiVersion.V5

    private val versionMutex = Mutex()

    private suspend fun fetchStatusForVersion(version: SonarrApiVersion): NetworkResult<ArrSoftwareStatus> {
        val url = "${instance.getEffectiveBaseUrl().trimEnd('/')}/${version.apiBase}/system/status"
        return httpClient.safeGet<ArrSoftwareStatus>(url) {
            appendInstanceApiKey()
        }
    }

    suspend fun determineApiVersion(): SonarrApiVersion {
        val v5Result = fetchStatusForVersion(SonarrApiVersion.V5)
        if (v5Result is NetworkResult.Success) {
            _apiVersion = SonarrApiVersion.V5
            versionDetermined = true
            return SonarrApiVersion.V5
        }

        versionDetermined = true
        _apiVersion = SonarrApiVersion.V3
        return SonarrApiVersion.V3
    }

    suspend fun ensureApiVersionDetermined() {
        if (versionDetermined) return
        versionMutex.withLock {
            if (!versionDetermined) {
                determineApiVersion()
            }
        }
    }

    override suspend fun testConnection(): NetworkResult<Unit> {
        determineApiVersion()
        return getStatus().map { }
    }

    override suspend fun getStatus(): NetworkResult<ArrSoftwareStatus> {
        ensureApiVersionDetermined()
        return super.getStatus()
    }

    override suspend fun getQualityProfiles(): NetworkResult<List<QualityProfile>> {
        ensureApiVersionDetermined()
        return super.getQualityProfiles()
    }

    override suspend fun getMetadataProfiles(): NetworkResult<List<MetadataProfile>> {
        ensureApiVersionDetermined()
        return super.getMetadataProfiles()
    }

    override suspend fun getRootFolders(): NetworkResult<List<RootFolder>> {
        ensureApiVersionDetermined()
        return super.getRootFolders()
    }

    override suspend fun getTags(): NetworkResult<List<Tag>> {
        ensureApiVersionDetermined()
        return super.getTags()
    }

    override suspend fun getCustomFilters(): NetworkResult<List<CustomFilter>> {
        ensureApiVersionDetermined()
        return super.getCustomFilters()
    }

    override suspend fun command(payload: CommandPayload): NetworkResult<Any> {
        ensureApiVersionDetermined()
        return super.command(payload)
    }

    override suspend fun getBlocklist(
        page: Int,
        pageSize: Int,
    ): NetworkResult<BlocklistPage> {
        ensureApiVersionDetermined()
        return super.getBlocklist(page, pageSize)
    }

    override suspend fun deleteBlocklistItem(id: Long): NetworkResult<Unit> {
        ensureApiVersionDetermined()
        return super.deleteBlocklistItem(id)
    }

    override suspend fun getFilesystem(path: String?): NetworkResult<List<FilesystemItem>> {
        ensureApiVersionDetermined()
        return super.getFilesystem(path)
    }

    override suspend fun downloadRelease(payload: DownloadReleasePayload): NetworkResult<Any> {
        ensureApiVersionDetermined()
        return super.downloadRelease(payload)
    }

    override suspend fun getDiskSpace(): NetworkResult<List<ArrDiskSpace>> {
        ensureApiVersionDetermined()
        return super.getDiskSpace()
    }

    override suspend fun getHealth(): NetworkResult<List<ArrHealth>> {
        ensureApiVersionDetermined()
        return super.getHealth()
    }

    override suspend fun getUpdates(): NetworkResult<List<ArrUpdate>> {
        ensureApiVersionDetermined()
        return super<BaseArrClient>.getUpdates()
    }

    override suspend fun getManualImportFiles(
        downloadId: String?,
        folder: String?,
        filterExistingFiles: Boolean,
    ): NetworkResult<List<ManualImportFile>> {
        ensureApiVersionDetermined()
        return super.getManualImportFiles(downloadId, folder, filterExistingFiles)
    }

    private fun buildSubresourceParams(
        v5Subresources: List<String>,
        v3Params: Map<String, Any> = emptyMap(),
        additionalParams: Map<String, Any> = emptyMap(),
    ): Map<String, Any> = buildMap {
        putAll(additionalParams)
        if (isV5) {
            put("includeSubresources", v5Subresources)
        } else {
            putAll(v3Params)
        }
    }

    override suspend fun getLibrary(): NetworkResult<List<ArrSeries>> {
        ensureApiVersionDetermined()
        return get<List<ArrSeries>>("series")
            .map { shows ->
                shows.map { series ->
                    series.withLocalImages(instance).copy(instanceId = instance.id)
                }
            }
    }

    override suspend fun getDetail(id: Long): NetworkResult<ArrSeries> {
        ensureApiVersionDetermined()
        return get("series/$id")
    }

    override suspend fun update(item: ArrMedia): NetworkResult<ArrSeries> {
        ensureApiVersionDetermined()
        return put("series/${item.id}", item)
    }

    override suspend fun edit(
        item: ArrMedia,
        moveFiles: Boolean,
    ): NetworkResult<Unit> {
        ensureApiVersionDetermined()
        val series =
            item as? ArrSeries
                ?: return NetworkResult.Error(message = "Item must be an ArrSeries")
        val id =
            series.id
                ?: return NetworkResult.Error(message = "Item id cannot be null")
        val body =
            SeriesEditorBody(
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
        addImportExclusion: Boolean,
    ): NetworkResult<Unit> {
        ensureApiVersionDetermined()
        return delete(
            endpoint = "series/$id",
            params =
            mapOf(
                "deleteFiles" to deleteFiles,
                "addImportListExclusion" to addImportExclusion,
            ),
        )
    }

    override suspend fun lookup(params: LookupParams): NetworkResult<List<ArrSeries>> {
        ensureApiVersionDetermined()
        return get("series/lookup", mapOf("term" to params.query))
    }

    override suspend fun addItemToLibrary(item: ArrMedia): NetworkResult<ArrSeries> {
        ensureApiVersionDetermined()
        return post<ArrMedia, ArrSeries>("series", item)
    }

    override suspend fun fetchActivityTasks(
        page: Int,
        pageSize: Int,
    ): NetworkResult<QueuePage> {
        ensureApiVersionDetermined()
        return get<QueuePage>(
            "queue",
            buildSubresourceParams(
                v5Subresources = listOf("Series", "Episodes"),
                v3Params = mapOf("includeSeries" to true, "includeEpisode" to true),
                additionalParams = mapOf("page" to page, "pageSize" to pageSize),
            ),
        ).map { it.setInstance(instance.id, instance.label, instance.type) }
    }

    override suspend fun deleteActivityTask(
        id: Int,
        removeFromClient: Boolean,
        blocklist: Boolean,
        skipRedownload: Boolean,
    ): NetworkResult<Unit> {
        ensureApiVersionDetermined()
        return if (isV5) {
            delete(
                endpoint = "queue/bulk",
                body = SonarrQueueBulkPayload(ids = listOf(id)),
                params = mapOf(
                    "removeFromClient" to removeFromClient,
                    "blocklist" to blocklist,
                    "skipRedownload" to skipRedownload,
                ),
            )
        } else {
            super.deleteActivityTask(id, removeFromClient, blocklist, skipRedownload)
        }
    }

    override suspend fun getReleases(params: ReleaseParams): NetworkResult<List<SeriesRelease>> {
        ensureApiVersionDetermined()
        if (params !is ReleaseParams.Series) {
            return NetworkResult.Error(message = "Non-series params type: $params")
        }

        val paramsMap =
            params.episodeId
                ?.let { epId -> mapOf("episodeId" to epId) }
                ?: buildMap<String, Any> {
                    params.seriesId?.let { put("seriesId", it) }
                    params.seasonNumber?.let { put("seasonNumber", it) }
                }

        return get("release", paramsMap)
    }

    override suspend fun setMonitorStatus(
        id: Long,
        monitorStatus: Boolean,
    ): NetworkResult<List<MonitoredResponse>> {
        ensureApiVersionDetermined()
        return put(
            "series/editor",
            mapOf(
                "monitored" to monitorStatus,
                "seriesIds" to listOf(id),
            ),
        )
    }

    override suspend fun getItemHistory(
        id: Long,
        page: Int,
        pageSize: Int,
        altId: Long?,
    ): NetworkResult<List<SonarrHistoryItem>> {
        ensureApiVersionDetermined()
        return get<SonarrHistoryResponse>(
            "history",
            buildSubresourceParams(
                v5Subresources = listOf("Series", "Episode"),
                v3Params = mapOf("includeSeries" to true, "includeEpisode" to true),
                additionalParams = mapOf("page" to page, "pageSize" to pageSize, "episodeId" to id),
            ),
        ).map { response ->
            response.records.map {
                it.copy(
                    instanceId = instance.id,
                    instanceName = instance.label,
                    instanceType = instance.type,
                    series = it.series?.withLocalImages(instance),
                    episode = it.episode?.copy(series = it.series)?.withLocalImages(instance),
                )
            }
        }
    }

    // Paged /history ignores seriesId (it only accepts seriesIds), so use the per-series endpoint.
    suspend fun getSeriesHistory(seriesId: Long): NetworkResult<List<SonarrHistoryItem>> {
        ensureApiVersionDetermined()
        return get<List<SonarrHistoryItem>>(
            "history/series",
            buildSubresourceParams(
                v5Subresources = listOf("Series", "Episode"),
                v3Params = mapOf("includeSeries" to true, "includeEpisode" to true),
                additionalParams = mapOf("seriesId" to seriesId),
            ),
        ).map { list ->
            list.map {
                it.copy(
                    instanceId = instance.id,
                    instanceName = instance.label,
                    instanceType = instance.type,
                    series = it.series?.withLocalImages(instance),
                    episode = it.episode?.copy(series = it.series)?.withLocalImages(instance),
                )
            }
        }
    }

    override suspend fun getHistory(
        page: Int,
        pageSize: Int,
    ): NetworkResult<List<HistoryItem>> {
        ensureApiVersionDetermined()
        return get<SonarrHistoryResponse>(
            "history",
            buildSubresourceParams(
                v5Subresources = listOf("Series", "Episode"),
                v3Params = mapOf("includeSeries" to true, "includeEpisode" to true),
                additionalParams = mapOf("page" to page, "pageSize" to pageSize),
            ),
        ).map { response ->
            response.records.map {
                it.copy(
                    instanceId = instance.id,
                    instanceName = instance.label,
                    instanceType = instance.type,
                    series = it.series?.withLocalImages(instance),
                    episode = it.episode?.copy(series = it.series)?.withLocalImages(instance),
                )
            }
        }
    }

    override suspend fun performAutomaticSearch(id: Long): NetworkResult<CommandResponse> {
        ensureApiVersionDetermined()
        return post("command", CommandPayload.Series(id))
    }

    override suspend fun getCalendar(
        start: LocalDate,
        end: LocalDate,
    ): NetworkResult<List<Episode>> {
        ensureApiVersionDetermined()
        return get<List<Episode>>(
            "calendar",
            buildSubresourceParams(
                v5Subresources = listOf("Series", "EpisodeFile", "EpisodeImages"),
                v3Params = mapOf(
                    "includeSeries" to true,
                    "includeEpisodeFile" to true,
                    "includeEpisodeImages" to true,
                ),
                additionalParams = mapOf(
                    "start" to start.toString(),
                    "end" to end.toString(),
                    "unmonitored" to true,
                ),
            ),
        ).map { it.map { ep -> ep.copy(instanceId = instance.id) } }
    }

    override suspend fun updateMonitoring(
        ids: List<Long>,
        monitor: Any,
    ): NetworkResult<Unit> {
        ensureApiVersionDetermined()
        return post(
            endpoint = "seasonPass",
            body =
            SeriesMonitoringBody(
                series = ids.map { IdWrapper(it) },
                monitoringOptions = SeriesMonitorOption(monitor as SeriesMonitorType),
            ),
        )
    }

    suspend fun updateEpisode(item: Episode): NetworkResult<Episode> {
        ensureApiVersionDetermined()
        return put("episode/${item.id}", item)
    }

    suspend fun getEpisodes(
        seriesId: Long,
        seasonNumber: Int? = null,
        includeEpisodeFile: Boolean = true,
        includeImages: Boolean = true,
    ): NetworkResult<List<Episode>> {
        ensureApiVersionDetermined()
        return get(
            "episode",
            buildMap {
                put("seriesId", seriesId)
                seasonNumber?.let { put("seasonNumber", it) }
                if (includeEpisodeFile) put("includeEpisodeFile", true)
                if (includeImages) put("includeImages", true)
            },
        )
    }

    suspend fun deleteEpisodes(fileIds: List<Long>): NetworkResult<Unit> {
        ensureApiVersionDetermined()
        return delete(
            endpoint = "episodefile/bulk",
            body = DeleteEpisodeBody(fileIds),
        )
    }

    suspend fun deleteEpisode(id: Long): NetworkResult<Unit> {
        ensureApiVersionDetermined()
        return delete("episodefile/$id")
    }

    override suspend fun getWantedMissing(
        page: Int,
        pageSize: Int,
    ): NetworkResult<WantedPage> {
        ensureApiVersionDetermined()
        return get<SonarrWantedPage>(
            "wanted/missing",
            buildSubresourceParams(
                v5Subresources = listOf("Series", "Episode"),
                v3Params = mapOf("includeSeries" to true, "includeEpisode" to true),
                additionalParams = mapOf("page" to page, "pageSize" to pageSize),
            ),
        ).map { pageRes ->
            WantedPage(
                page = pageRes.page,
                pageSize = pageRes.pageSize,
                totalRecords = pageRes.totalRecords,
                records = pageRes.records.map { ep ->
                    val series = ep.series?.withLocalImages(instance)
                    val episodeTitle = "${series?.title ?: "Series"} - S${ep.seasonNumber.toString().padStart(2, '0')}E${ep.episodeNumber.toString().padStart(2, '0')}${ep.title?.let { " - $it" } ?: ""}"
                    ArrSeries(
                        id = ep.id,
                        title = episodeTitle,
                        originalLanguage = series?.originalLanguage ?: Language(1, "English"),
                        year = ep.airDate?.year ?: series?.year ?: 2026,
                        qualityProfileId = series?.qualityProfileId ?: 1,
                        monitored = ep.monitored,
                        runtime = ep.runtime ?: series?.runtime ?: 30,
                        status = series?.status ?: MediaStatus.Continuing,
                        overview = ep.overview ?: series?.overview,
                        images = ep.images.ifEmpty { series?.images ?: emptyList() },
                        tvdbId = ep.tvdbId ?: series?.tvdbId ?: 0L,
                        ended = series?.ended ?: false,
                        seasonFolder = series?.seasonFolder ?: true,
                        monitorNewItems = series?.monitorNewItems ?: MonitorNewItems.All,
                        useSceneNumbering = series?.useSceneNumbering ?: false,
                        seriesType = series?.seriesType ?: SeriesType.Standard,
                        instanceId = instance.id,
                    )
                },
            )
        }
    }

    override suspend fun getWantedCutoff(
        page: Int,
        pageSize: Int,
    ): NetworkResult<WantedPage> {
        ensureApiVersionDetermined()
        return get<SonarrWantedPage>(
            "wanted/cutoff",
            buildSubresourceParams(
                v5Subresources = listOf("Series", "Episode"),
                v3Params = mapOf("includeSeries" to true, "includeEpisode" to true),
                additionalParams = mapOf("page" to page, "pageSize" to pageSize),
            ),
        ).map { pageRes ->
            WantedPage(
                page = pageRes.page,
                pageSize = pageRes.pageSize,
                totalRecords = pageRes.totalRecords,
                records = pageRes.records.map { ep ->
                    val series = ep.series?.withLocalImages(instance)
                    val episodeTitle = "${series?.title ?: "Series"} - S${ep.seasonNumber.toString().padStart(2, '0')}E${ep.episodeNumber.toString().padStart(2, '0')}${ep.title?.let { " - $it" } ?: ""}"
                    ArrSeries(
                        id = ep.id,
                        title = episodeTitle,
                        originalLanguage = series?.originalLanguage ?: Language(1, "English"),
                        year = ep.airDate?.year ?: series?.year ?: 2026,
                        qualityProfileId = series?.qualityProfileId ?: 1,
                        monitored = ep.monitored,
                        runtime = ep.runtime ?: series?.runtime ?: 30,
                        status = series?.status ?: MediaStatus.Continuing,
                        overview = ep.overview ?: series?.overview,
                        images = ep.images.ifEmpty { series?.images ?: emptyList() },
                        tvdbId = ep.tvdbId ?: series?.tvdbId ?: 0L,
                        ended = series?.ended ?: false,
                        seasonFolder = series?.seasonFolder ?: true,
                        monitorNewItems = series?.monitorNewItems ?: MonitorNewItems.All,
                        useSceneNumbering = series?.useSceneNumbering ?: false,
                        seriesType = series?.seriesType ?: SeriesType.Standard,
                        instanceId = instance.id,
                    )
                },
            )
        }
    }
}
