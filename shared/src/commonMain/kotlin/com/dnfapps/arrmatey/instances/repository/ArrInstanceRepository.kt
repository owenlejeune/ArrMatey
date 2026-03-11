package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ArrClient
import com.dnfapps.arrmatey.arr.api.client.LidarrClient
import com.dnfapps.arrmatey.arr.api.client.RadarrClient
import com.dnfapps.arrmatey.arr.api.client.SonarrClient
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.DownloadReleasePayload
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.LidarrTrack
import com.dnfapps.arrmatey.arr.api.model.LidarrTrackFile
import com.dnfapps.arrmatey.arr.api.model.MonitoredResponse
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.client.NetworkResult
import com.dnfapps.arrmatey.client.OperationStatus
import com.dnfapps.arrmatey.client.onError
import com.dnfapps.arrmatey.client.onSuccess
import com.dnfapps.arrmatey.arr.state.DownloadState
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import io.ktor.client.HttpClient
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArrInstanceRepository(
    override val instance: Instance,
    private val httpClient: HttpClient
): InstanceScopedRepository {
    val client: ArrClient = createClient()

    val sonarrClient: SonarrClient
        get() = client as? SonarrClient ?: throw IllegalStateException("Client is not a SonarrClient instance")

    val radarrClient: RadarrClient
        get() = client as? RadarrClient ?: throw IllegalStateException("Client is not a RadarrClient instance")

    val lidarrClient: LidarrClient
        get() = client as? LidarrClient ?: throw IllegalStateException("Client is not a LidarrClient instance")

    private fun createClient(): ArrClient = when (instance.type) {
        InstanceType.Sonarr -> SonarrClient(instance, httpClient)
        InstanceType.Radarr -> RadarrClient(instance, httpClient)
        InstanceType.Lidarr -> LidarrClient(instance, httpClient)
        else -> TODO()
    }

    private val _library = MutableStateFlow<NetworkResult<List<ArrMedia>>?>(null)
    val library: StateFlow<NetworkResult<List<ArrMedia>>?> = _library.asStateFlow()

    private val _lookupResults = MutableStateFlow<NetworkResult<List<ArrMedia>>?>(null)
    val lookupResults: StateFlow<NetworkResult<List<ArrMedia>>?> = _lookupResults.asStateFlow()

    private val _lastAddedItemId = MutableStateFlow<Long?>(null)
    val lastAddedItemId: StateFlow<Long?> = _lastAddedItemId.asStateFlow()

    private val _releases = MutableStateFlow<NetworkResult<List<ArrRelease>>?>(null)
    val releases: StateFlow<NetworkResult<List<ArrRelease>>?> = _releases.asStateFlow()

    private val _historyCache = MutableStateFlow<Map<Long, List<HistoryItem>>>(emptyMap())
    val historyCache: StateFlow<Map<Long, List<HistoryItem>>> = _historyCache.asStateFlow()

    private val _mediaDetailsCache = MutableStateFlow<Map<Long, ArrMedia>>(emptyMap())


    private val _qualityProfiles = MutableStateFlow<List<QualityProfile>>(emptyList())
    val qualityProfiles: StateFlow<List<QualityProfile>> = _qualityProfiles.asStateFlow()

    private val _rootFolders = MutableStateFlow<List<RootFolder>>(emptyList())
    val rootFolders: StateFlow<List<RootFolder>> = _rootFolders.asStateFlow()

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    private val _softwareStatus = MutableStateFlow<ArrSoftwareStatus?>(null)
    val softwareStatus: StateFlow<ArrSoftwareStatus?> = _softwareStatus.asStateFlow()

    private val _diskSpace = MutableStateFlow<List<ArrDiskSpace>>(emptyList())
    val diskSpace: StateFlow<List<ArrDiskSpace>> = _diskSpace.asStateFlow()

    private val _health = MutableStateFlow<List<ArrHealth>>(emptyList())
    val health: StateFlow<List<ArrHealth>> = _health.asStateFlow()

    private val _activityTasks = MutableStateFlow<List<QueueItem>>(emptyList())
    val activityTasks: StateFlow<List<QueueItem>> = _activityTasks.asStateFlow()


    private val _addItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val addItemStatus: StateFlow<OperationStatus> = _addItemStatus.asStateFlow()

    private val _editItemStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val editItemStatus: StateFlow<OperationStatus> = _editItemStatus.asStateFlow()

    private val _searchStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val searchStatus: StateFlow<OperationStatus> = _searchStatus.asStateFlow()

    private val _downloadStatus = MutableStateFlow<DownloadState>(DownloadState.Initial)
    val downloadStatus: StateFlow<DownloadState> = _downloadStatus.asStateFlow()

    private val _monitorStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val monitorStatus: StateFlow<OperationStatus> = _monitorStatus.asStateFlow()

    private val _historyStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val historyStatus: StateFlow<OperationStatus> = _historyStatus.asStateFlow()

    // Sonarr-specific
    private val _episodes = MutableStateFlow<Map<Long, List<Episode>>>(emptyMap())
    val episodes: StateFlow<Map<Long, List<Episode>>> = _episodes.asStateFlow()

    // Radarr-specific
    private val _movieExtraFiles = MutableStateFlow<Map<Long, List<ExtraFile>>>(emptyMap())
    val movieExtraFiles: StateFlow<Map<Long, List<ExtraFile>>> = _movieExtraFiles.asStateFlow()

    // Lidarr-specific
    private val _artistAlbums = MutableStateFlow<Map<Long, List<ArrAlbum>>>(emptyMap())
    val artistAlbums: StateFlow<Map<Long, List<ArrAlbum>>> = _artistAlbums.asStateFlow()

    private val _artistTracks = MutableStateFlow<Map<Long, Map<Long, List<LidarrTrack>>>>(emptyMap())
    val artistTracks: StateFlow<Map<Long, Map<Long, List<LidarrTrack>>>> = _artistTracks.asStateFlow()

    private val _artistTrackFiles = MutableStateFlow<Map<Long, Map<Long, List<LidarrTrackFile>>>>(emptyMap())
    val artistTrackFiles: StateFlow<Map<Long, Map<Long, List<LidarrTrackFile>>>> = _artistTrackFiles.asStateFlow()

    override suspend fun testConnection(): NetworkResult<Unit> {
        return client.testConnection()
    }

    suspend fun refreshLibrary() {
        _library.value = NetworkResult.Loading
        _library.value = client.getLibrary()
    }

    suspend fun getMediaDetails(id: Long): NetworkResult<ArrMedia> {
        return client.getDetail(id)
            .onSuccess { media ->
                val currentCache = _mediaDetailsCache.value.toMutableMap()
                currentCache[id] = media
                _mediaDetailsCache.value = currentCache
            }
    }

    suspend fun refreshQualityProfiles() {
        client.getQualityProfiles()
            .onSuccess { _qualityProfiles.value = it }
    }

    suspend fun refreshRootFolders() {
        client.getRootFolders()
            .onSuccess { _rootFolders.value = it }
    }

    suspend fun refreshTags() {
        client.getTags()
            .onSuccess { _tags.value = it }
    }

    suspend fun refreshStatus() {
        client.getStatus()
            .onSuccess { _softwareStatus.value = it }
    }

    suspend fun refreshDiskSpace() {
        client.getDiskSpace()
            .onSuccess { _diskSpace.value = it }
    }

    suspend fun refreshHealth() {
        client.getHealth()
            .onSuccess { _health.value = it }
            .onError { i, string, throwable -> print("ERROR - $i, $string, $throwable") }
    }

    suspend fun refreshAllMetadata() {
        coroutineScope {
            launch { refreshQualityProfiles() }
            launch { refreshRootFolders() }
            launch { refreshTags() }
        }
    }

    suspend fun refreshInstanceStatuses() {
        coroutineScope {
            launch { refreshStatus() }
            launch { refreshDiskSpace() }
            launch { refreshHealth() }
        }
    }

    suspend fun refreshActivityTasks(page: Int = 1, pageSize: Int = 100) {
        client.fetchActivityTasks(page, pageSize)
            .onSuccess { queue ->
                _activityTasks.value = queue.records
            }
    }

    suspend fun performLookup(query: String) {
        if (query.isBlank()) {
            _lookupResults.value = null
            return
        }

        _lookupResults.value = NetworkResult.Loading

        client.lookup(query)
            .onSuccess { results ->
                _lookupResults.value = NetworkResult.Success(results)
            }
            .onError { code, message, cause ->
                _lookupResults.value = NetworkResult.Error(code, message, cause)
            }
    }

    fun clearLookup() {
        _lookupResults.value = null
    }

    suspend fun addItem(item: ArrMedia) {
        _addItemStatus.value = OperationStatus.InProgress

        client.addItemToLibrary(item)
            .onSuccess { addedItem ->
                _addItemStatus.value = OperationStatus.Success("Item added successfully")
                addedItem.id?.let {
                    val newMap = _mediaDetailsCache.value.toMutableMap()
                    newMap[it] = addedItem
                    _mediaDetailsCache.value = newMap
                }
                _lastAddedItemId.value = addedItem.id
                refreshLibrary()
            }
            .onError { code, error, cause ->
                _addItemStatus.value = OperationStatus.Error(code, error, cause)
            }
            .also {
                _addItemStatus.value = OperationStatus.Idle
                _lastAddedItemId.value = null
            }
    }

    suspend fun getReleases(params: ReleaseParams) {
        _releases.value = NetworkResult.Loading

        client.getReleases(params)
            .onSuccess { releases ->
                _releases.value = NetworkResult.Success(releases)
            }
            .onError { code, message, cause ->
                _releases.value = NetworkResult.Error(code, message, cause)
            }
    }

    suspend fun downloadRelease(
        payload: DownloadReleasePayload
    ): NetworkResult<Any> {
        _downloadStatus.value = DownloadState.Loading(payload.guid)

        return client.downloadRelease(payload)
            .onSuccess {
                _downloadStatus.value = DownloadState.Success
            }
            .onError { code, error, cause ->
                _downloadStatus.value = DownloadState.Error
            }
            .also {
                _downloadStatus.value = DownloadState.Initial
            }
    }

    suspend fun deleteActivityTask(
        releaseId: Int,
        removeFromClient: Boolean,
        addToBlocklist: Boolean,
        skipRedownload: Boolean
    ): NetworkResult<Unit> {
        return client.deleteActivityTask(releaseId, removeFromClient, addToBlocklist, skipRedownload)
    }

    suspend fun executeAutomaticSearch(itemId: Long) {
        _searchStatus.value = OperationStatus.InProgress

        client.performAutomaticSearch(itemId)
            .onSuccess {
                _searchStatus.value = OperationStatus.Success("Search initiated")
            }
            .onError { code, error, cause ->
                _searchStatus.value = OperationStatus.Error(code, error, cause)
            }
            .also {
                _searchStatus.value = OperationStatus.Idle
            }
    }

    suspend fun executeCommand(payload: CommandPayload): NetworkResult<Any> {
        return client.command(payload)
    }

    suspend fun getItemHistory(itemId: Long, page: Int = 1, pageSize: Int = 100): NetworkResult<List<HistoryItem>> {
        _historyStatus.value = OperationStatus.InProgress

        return client.getItemHistory(itemId, page, pageSize)
            .onSuccess { history ->
                val currentCache = _historyCache.value.toMutableMap()
                currentCache[itemId] = history
                _historyCache.value = currentCache
                _historyStatus.value = OperationStatus.Success()
            }
            .onError { code, message, cause ->
                _historyStatus.value = OperationStatus.Error(code, message, cause)
            }
            .also {
                _historyStatus.value = OperationStatus.Idle
            }
    }

    suspend fun editMediaItem(item: ArrMedia, moveFiles: Boolean): NetworkResult<Unit> {
        _editItemStatus.value = OperationStatus.InProgress
        return client.edit(item, moveFiles)
            .onSuccess {
                val id = item.id ?: return@onSuccess
                val currentCache = _mediaDetailsCache.value.toMutableMap()
                currentCache[id] = item
                _mediaDetailsCache.value = currentCache

                updateItemInLibraryCache(item)
                _editItemStatus.value = OperationStatus.Success("Item edited successfully")
//                _editItemStatus.value = OperationStatus.Idle
            }
            .onError { code, message, cause ->
                _editItemStatus.value = OperationStatus.Error(code, message, cause)
//                _editItemStatus.value = OperationStatus.Idle
            }
//            .also {
//                _editItemStatus.value = OperationStatus.Idle
//            }
    }

    suspend fun updateMediaItem(item: ArrMedia): NetworkResult<ArrMedia> {
        _monitorStatus.value = OperationStatus.InProgress

        return client.update(item)
            .onSuccess { updateItem ->
                _monitorStatus.value = OperationStatus.Success("Item updated successfully")

                val id = updateItem.id ?: return@onSuccess
                val currentCache = _mediaDetailsCache.value.toMutableMap()
                currentCache[id] = updateItem
                _mediaDetailsCache.value = currentCache

                updateItemInLibraryCache(updateItem)
            }
            .onError { code, message, cause ->
                _monitorStatus.value = OperationStatus.Error(code, message, cause)
            }.also {
                _monitorStatus.value = OperationStatus.Idle
            }
    }

    suspend fun delete(
        id: Long,
        deleteFiles: Boolean,
        addImportExclusion: Boolean
    ): NetworkResult<Unit> =
        client.delete(id, deleteFiles, addImportExclusion)
            .onSuccess {
                val currentCache = _mediaDetailsCache.value.toMutableMap()
                currentCache.remove(id)
                _mediaDetailsCache.value = currentCache
            }

    private fun updateItemInLibraryCache(updatedItem: ArrMedia) {
        val currentLibrary = _library.value
        if (currentLibrary is NetworkResult.Success) {
            val updatedItems = currentLibrary.data.map { item ->
                if (item.id == updatedItem.id) updatedItem else item
            }
            _library.value = NetworkResult.Success(
                updatedItems
            )
        }
    }

    suspend fun setMonitorState(id: Long, status: Boolean): NetworkResult<MonitoredResponse?> {
        _monitorStatus.value = OperationStatus.InProgress

        val result = client.setMonitorStatus(id, status)

        return result
            .map { it.firstOrNull() }
            .onSuccess { response ->
                _monitorStatus.value = OperationStatus.Success(
                    if (status) "Monitored" else "Unmonitored"
                )
                response?.let { updateMonitoredInCache(it.id, it.monitored) }
            }
            .onError { code, message, cause ->
                println("error setting monitor status")
            }.also {
                _monitorStatus.value = OperationStatus.Idle
            }
    }

    suspend fun toggleSeasonMonitor(id: Long, seasonNumber: Int): NetworkResult<ArrMedia> {
        _monitorStatus.value = OperationStatus.InProgress

        val currentSeries = _mediaDetailsCache.value[id] as? ArrSeries
        if (currentSeries == null) {
            _monitorStatus.value = OperationStatus.Error(message = "Series not found in cache")
            return NetworkResult.Error(message = "Series not found in cache")
        }

        val updatedSeason = currentSeries.seasons.map { season ->
            if (season.seasonNumber == seasonNumber) {
                season.copy(monitored = !season.monitored)
            } else {
                season
            }
        }

        val updatedSeries = currentSeries.copy(seasons = updatedSeason)

        return client.update(updatedSeries)
            .onSuccess { resultSeries ->
                _monitorStatus.value = OperationStatus.Success("Season monitor toggled")

                val currentCache = _mediaDetailsCache.value.toMutableMap()
                currentCache[id] = resultSeries
                _mediaDetailsCache.value = currentCache

                updateSeriesInLibraryCache(resultSeries)
            }.also {
                _monitorStatus.value = OperationStatus.Idle
            }
    }

    suspend fun toggleEpisodeMonitor(episode: Episode): NetworkResult<Episode> {
        _monitorStatus.value = OperationStatus.InProgress

        val updatedEpisode = episode.copy(monitored = !episode.monitored)

        if (instance.type != InstanceType.Sonarr) {
            _monitorStatus.value = OperationStatus.Error(message = "Not a Sonarr instance")
            return NetworkResult.Error(message = "Not a Sonarr instance")
        }

        return (client as SonarrClient).updateEpisode(updatedEpisode)
            .onSuccess { resultEpisode ->
                _monitorStatus.value = OperationStatus.Success(
                    if (resultEpisode.monitored) "Episode monitored" else "Episode unmonitored"
                )
                updateEpisodeInCache(resultEpisode.copy(
                    images = episode.images,
                    episodeFile = episode.episodeFile
                ))
            }
            .onError { code, message, cause ->
                _monitorStatus.value = OperationStatus.Error(code, message, cause)
            }
            .also {
                _monitorStatus.value = OperationStatus.Idle
            }
    }

    private fun updateSeriesInLibraryCache(series: ArrMedia) {
        val currentLibrary = _library.value
        if (currentLibrary is NetworkResult.Success) {
            val updatedItems = currentLibrary.data.map { item ->
                if (item.id == series.id) series else item
            }
            _library.value = NetworkResult.Success(
                updatedItems
            )
        }
    }

    private fun updateEpisodeInCache(episode: Episode) {
        val currentEpisodes = _episodes.value.toMutableMap()
        currentEpisodes.forEach { (seriesId, episodeList) ->
            val index = episodeList.indexOfFirst { it.id == episode.id }
            if (index != -1) {
                val updatedList = episodeList.toMutableList()
                updatedList[index] = episode
                currentEpisodes[seriesId] = updatedList
            }
        }
        _episodes.value = currentEpisodes
    }

    suspend fun toggleAlbumMonitor(album: ArrAlbum): NetworkResult<ArrAlbum> {
        _monitorStatus.value = OperationStatus.InProgress

        if (instance.type != InstanceType.Lidarr) {
            _monitorStatus.value = OperationStatus.Error(message = "Not a Lidarr instance")
            return NetworkResult.Error(message = "Not a Lidarr instance")
        }

        return (client as LidarrClient).toggleMonitored(album)
            .onSuccess { resultAlbum ->
                _monitorStatus.value = OperationStatus.Success(
                    if (resultAlbum.monitored) "Album monitored" else "Album unmonitored"
                )
                updateAlbumInCache(resultAlbum.copy(images = album.images))
                _monitorStatus.value = OperationStatus.Idle
            }
            .onError { code, message, cause ->
                _monitorStatus.value = OperationStatus.Error(code, message, cause)
                _monitorStatus.value = OperationStatus.Idle
            }
    }

    private fun updateAlbumInCache(album: ArrAlbum) {
        _artistAlbums.update { currentMap ->
            val updatedMap = currentMap.toMutableMap()
            updatedMap.forEach { (artistId, albumList) ->
                val index = albumList.indexOfFirst { it.id == album.id }
                if (index != -1) {
                    val updatedList = albumList.toMutableList()
                    updatedList[index] = album
                    updatedMap[artistId] = updatedList
                }
            }
            updatedMap
        }
    }

    private fun updateMonitoredInCache(id: Long, status: Boolean) {
        val libraryState = _library.value
        if (libraryState is NetworkResult.Success) {
            val updatedItems = libraryState.data.map { item ->
                if (item.id == id) {
                    when (item) {
                        is ArrSeries -> item.copy(monitored = status)
                        is ArrMovie -> item.copy(monitored = status)
                        is Arrtist -> item.copy(monitored = status)
                    }
                } else {
                    item
                }
            }
            _library.value = NetworkResult.Success(
                updatedItems
            )
        }

        val currentDetailsCache = _mediaDetailsCache.value
        currentDetailsCache[id]?.let { item ->
            val updatedMedia = when (item) {
                is ArrSeries -> item.copy(monitored = status)
                is ArrMovie -> item.copy(monitored = status)
                is Arrtist -> item.copy(monitored = status)
            }
            val updatedCache = currentDetailsCache.toMutableMap()
            updatedCache[id] = updatedMedia
            _mediaDetailsCache.value = updatedCache
        }
    }

    fun clearReleases() {
        _releases.value = null
    }

    fun observeMediaDetails(id: Long): Flow<NetworkResult<ArrMedia>> = flow {
        emit(NetworkResult.Loading)

        val result = client.getDetail(id)
        when (result) {
            is NetworkResult.Success -> {
                val currentCache = _mediaDetailsCache.value.toMutableMap()
                currentCache[id] = result.data
                _mediaDetailsCache.value = currentCache
            }

            is NetworkResult.Error -> {
                emit(result)
                return@flow
            }

            is NetworkResult.Loading -> {}
        }

        _mediaDetailsCache
            .map { cache ->
                cache[id]?.let { NetworkResult.Success(it) }
                    ?: NetworkResult.Error(message = "Media not found in cache")
            }
            .collect { emit(it) }
    }

    fun observeItemHistory(itemId: Long): Flow<List<HistoryItem>> {
        return historyCache.map { cache ->
            cache[itemId] ?: emptyList()
        }
    }

    // Sonarr-specific
    suspend fun getEpisodes(
        seriesId: Long,
        seasonNumber: Int? = null
    ): NetworkResult<List<Episode>> =
        safePerformSonarr { client ->
            client.getEpisodes(seriesId, seasonNumber)
                .onSuccess { episodes ->
                    val currentMap = _episodes.value.toMutableMap()
                    currentMap[seriesId] = episodes
                    _episodes.value = currentMap
                }
        }

    suspend fun deleteSeasonFiles(
        seriesId: Long,
        seasonNumber: Int
    ): NetworkResult<Unit> =
        safePerformSonarr {
            val episodes = _episodes.value[seriesId]?.filter { it.seasonNumber == seasonNumber } ?: emptyList()
            deleteEpisodes(seriesId, episodes)
                .onSuccess {
                    getMediaDetails(seriesId)
                }
        }

    suspend fun deleteEpisodes(
        seriesId: Long,
        episodes: List<Episode>
    ): NetworkResult<Unit> =
        safePerformSonarr { client ->
            val fileIds = episodes
                .filter { it.episodeFile != null }
                .mapNotNull { it.episodeFileId }
            client.deleteEpisodes(fileIds)
                .onSuccess {
                    val currentMap = _episodes.value.toMutableMap()
                    val currentEpisodes = currentMap[seriesId] ?: emptyList()
                    currentMap[seriesId] = currentEpisodes.filter { !fileIds.contains(it.id) }
                    _episodes.value = currentMap
                }
        }

    suspend fun deleteEpisodeFile(
        seriesId: Long,
        fileId: Long
    ): NetworkResult<Unit> =
        safePerformSonarr { client ->
            client.deleteEpisode(fileId)
                .onSuccess {
                    getEpisodes(seriesId)
                }
        }

    // Radarr-specific
    suspend fun getMovieExtraFiles(movieId: Long): NetworkResult<List<ExtraFile>> =
        safePerformRadarr { client ->
            client.getMovieExtraFile(movieId)
                .onSuccess { files ->
                    val currentMap = _movieExtraFiles.value.toMutableMap()
                    currentMap[movieId] = files
                    _movieExtraFiles.value = currentMap
                }
        }

    // Lidarr-specific
    suspend fun getArtistAlbums(artistId: Long): NetworkResult<List<ArrAlbum>> =
        safePerformLidarr { client ->
            client.getAlbums(artistId)
                .onSuccess { albums ->
                    val currentMap = _artistAlbums.value.toMutableMap()
                    currentMap[artistId] = albums.sortedByDescending { it.releaseDate }
                    _artistAlbums.value = currentMap
                }
        }

    suspend fun getArtistTracks(artistId: Long): NetworkResult<List<LidarrTrack>> =
        safePerformLidarr { client ->
            client.getTracks(artistId = artistId)
                .onSuccess { tracks ->
                    val currentMap = _artistTracks.value.toMutableMap()
                    currentMap[artistId] = tracks.groupBy { it.albumId }
                    _artistTracks.value = currentMap
                }
        }

    suspend fun getArtistTrackFiles(artistId: Long): NetworkResult<List<LidarrTrackFile>> =
        safePerformLidarr { client ->
            client.getTrackFiles(artistId = artistId)
                .onSuccess { trackFiles ->
                    val currentMap = _artistTrackFiles.value.toMutableMap()
                    currentMap[artistId] = trackFiles.groupBy { it.albumId }
                    _artistTrackFiles.value = currentMap
                }
        }

    suspend fun deleteAlbumFiles(
        artistId: Long,
        albumId: Long
    ): NetworkResult<Unit> =
        safePerformLidarr { client ->
            val files = (_artistTrackFiles.value[artistId] ?: emptyMap())[albumId] ?: emptyList()
            deleteTrackFiles(files)
                .onSuccess {
                    getArtistAlbums(artistId)
                }
        }

    suspend fun deleteTrackFiles(
        tracks: List<LidarrTrackFile>
    ): NetworkResult<Unit> =
        safePerformLidarr { client ->
            val fileIds = tracks.map { it.id }
            client.deleteTracks(fileIds)
                .onSuccess {
                    _artistTrackFiles.update { currentMap ->
                        currentMap.mapValues { (_, albumMap) ->
                            albumMap.mapValues { (_, tracks) ->
                                tracks.filterNot { trackFile ->
                                    fileIds.contains(trackFile.id)
                                }
                            }.filterValues { it.isNotEmpty() }
                        }.filterValues { it.isNotEmpty() }
                    }
                }
        }

    // Helpers
    private suspend inline fun <reified T> safePerformSonarr(
        operation: suspend (SonarrClient) -> NetworkResult<T>
    ): NetworkResult<T> {
        val client = client as? SonarrClient ?: return NetworkResult.Error(message = "Not a Sonarr instance")
        return operation(client)
    }

    private suspend inline fun <reified T> safePerformRadarr(
        operation: suspend (RadarrClient) -> NetworkResult<T>
    ): NetworkResult<T> {
        val client = client as? RadarrClient ?: return NetworkResult.Error(message = "Not a Radarr instance")
        return operation(client)
    }

    private suspend inline fun <reified T> safePerformLidarr(
        operation: suspend (LidarrClient) -> NetworkResult<T>
    ): NetworkResult<T> {
        val client = client as? LidarrClient ?: return NetworkResult.Error(message = "Not a Lidarr instance")
        return operation(client)
    }

}
