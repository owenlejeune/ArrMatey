package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ArrClient
import com.dnfapps.arrmatey.arr.api.client.LookupParams
import com.dnfapps.arrmatey.arr.api.client.SonarrClient
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.BlocklistItem
import com.dnfapps.arrmatey.arr.api.model.ComicVolume
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.DownloadReleasePayload
import com.dnfapps.arrmatey.arr.api.model.FilesystemItem
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.ManualImportFile
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.api.model.WantedMediaItem
import com.dnfapps.arrmatey.arr.state.DownloadState
import com.dnfapps.arrmatey.extensions.mergeWithLibrary
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.shared.item_added_successfully
import com.dnfapps.arrmatey.shared.item_edited_successfully
import com.dnfapps.arrmatey.shared.item_updated_successfully
import com.dnfapps.arrmatey.shared.media_not_found_in_cache
import com.dnfapps.arrmatey.shared.monitoring_updated_successfully
import com.dnfapps.arrmatey.shared.not_a_sonarr_instance
import com.dnfapps.arrmatey.shared.search_initiated
import com.dnfapps.arrmatey.shared.unknown_error
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.asSuccess
import com.dnfapps.networking.mapValues
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class LibraryRepository(
    private val client: ArrClient,
    private val logger: Logger,
    private val mokoStrings: MokoStrings,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
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

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private val _blocklist = MutableStateFlow<List<BlocklistItem>>(emptyList())
    val blocklist: StateFlow<List<BlocklistItem>> = _blocklist.asStateFlow()

    private val _isBlocklistLoading = MutableStateFlow(false)
    val isBlocklistLoading: StateFlow<Boolean> = _isBlocklistLoading.asStateFlow()

    private val _hasBlocklistLoaded = MutableStateFlow(false)
    val hasBlocklistLoaded: StateFlow<Boolean> = _hasBlocklistLoaded.asStateFlow()

    private val _missing = MutableStateFlow<List<WantedMediaItem>>(emptyList())
    val missing: StateFlow<List<WantedMediaItem>> = _missing.asStateFlow()

    private val _isMissingLoading = MutableStateFlow(false)
    val isMissingLoading: StateFlow<Boolean> = _isMissingLoading.asStateFlow()

    private val _hasMissingLoaded = MutableStateFlow(false)
    val hasMissingLoaded: StateFlow<Boolean> = _hasMissingLoaded.asStateFlow()

    private val _cutoffUnmet = MutableStateFlow<List<WantedMediaItem>>(emptyList())
    val cutoffUnmet: StateFlow<List<WantedMediaItem>> = _cutoffUnmet.asStateFlow()

    private val _isCutoffLoading = MutableStateFlow(false)
    val isCutoffLoading: StateFlow<Boolean> = _isCutoffLoading.asStateFlow()

    private val _hasCutoffLoaded = MutableStateFlow(false)
    val hasCutoffLoaded: StateFlow<Boolean> = _hasCutoffLoaded.asStateFlow()

    private val _mediaDetailsCache = MutableStateFlow<Map<Long, ArrMedia>>(emptyMap())
    val mediaDetailsCache: StateFlow<Map<Long, ArrMedia>> = _mediaDetailsCache.asStateFlow()

    fun updateMissingCache(items: List<WantedMediaItem>) {
        _missing.value = items
        _hasMissingLoaded.value = true
    }

    fun updateCutoffCache(items: List<WantedMediaItem>) {
        _cutoffUnmet.value = items
        _hasCutoffLoaded.value = true
    }

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

    suspend fun refreshLibrary(
        onBookLibraryUpdate: (suspend () -> Unit)? = null,
        onListenarrConfigUpdate: (suspend () -> Unit)? = null,
    ) {
        if (_library.value == null) {
            _library.value = NetworkResult.Loading
        }
        val result = withContext(Dispatchers.IO) { client.getLibrary() }
        _library.value = result
        onBookLibraryUpdate?.invoke()
        onListenarrConfigUpdate?.invoke()
    }

    suspend fun getMediaDetails(id: Long): NetworkResult<ArrMedia> = client
        .getDetail(id)
        .onSuccess { media ->
            logger.info { "Media details for $id: $media" }
            val currentCache = _mediaDetailsCache.value.toMutableMap()
            currentCache[id] = media
            _mediaDetailsCache.value = currentCache
        }.onError { code, message, cause ->
            logger.error(cause) { "Error getting media details for $id: $message" }
        }

    suspend fun refreshActivityTasks(
        page: Int = 1,
        pageSize: Int = 100,
    ) {
        client
            .fetchActivityTasks(page, pageSize)
            .onSuccess { queue ->
                _activityTasks.value = queue.records
            }
    }

    suspend fun performLookup(
        query: String,
        defaultSearchLanguage: String? = null,
        defaultSearchRegion: String? = null,
    ) {
        if (query.isBlank()) {
            _lookupResults.value = null
            return
        }

        _lookupResults.value = NetworkResult.Loading

        val queryParams = LookupParams(query, defaultSearchLanguage, defaultSearchRegion)
        client
            .lookup(queryParams)
            .onSuccess { results ->
                logger.info { "Lookup results: $results" }
                val libraryItems = library.value?.asSuccess()?.data ?: emptyList()
                _lookupResults.value = NetworkResult.Success(results.mergeWithLibrary(libraryItems))
            }.onError { code, message, cause ->
                logger.error(cause) { "Error performing lookup: $message" }
                _lookupResults.value = NetworkResult.Error(code, message, cause)
            }
    }

    fun clearLookup() {
        _lookupResults.value = null
    }

    suspend fun directLookup(
        query: String,
        defaultSearchLanguage: String? = null,
        defaultSearchRegion: String? = null,
    ): NetworkResult<List<ArrMedia>> {
        if (query.isBlank()) return NetworkResult.Success(emptyList())
        val queryParams = LookupParams(query, defaultSearchLanguage, defaultSearchRegion)
        val result = client.lookup(queryParams)
        return if (result is NetworkResult.Success) {
            val libraryItems = library.value?.asSuccess()?.data ?: emptyList()
            NetworkResult.Success(result.data.mergeWithLibrary(libraryItems))
        } else {
            result
        }
    }

    suspend fun addItem(
        item: ArrMedia,
        searchOnAdd: Boolean,
    ) {
        _addItemStatus.value = OperationStatus.InProgress
        delay(100.milliseconds)

        val result = withContext(Dispatchers.IO) { client.addItemToLibrary(item) }
        processAddResult(result, searchOnAdd)
    }

    suspend fun processAddResult(
        result: NetworkResult<ArrMedia>,
        searchOnAdd: Boolean,
    ) {
        result
            .onSuccess { addedItem ->
                _addItemStatus.value = OperationStatus.Success(mokoStrings.getString(MR.strings.item_added_successfully))
                addedItem.id?.let {
                    val newMap = _mediaDetailsCache.value.toMutableMap()
                    newMap[it] = addedItem
                    _mediaDetailsCache.value = newMap
                }
                _lastAddedItemId.value = addedItem.id

                val currentLibrary = (_library.value as? NetworkResult.Success)?.data
                if (currentLibrary != null) {
                    _library.value = NetworkResult.Success(listOf(addedItem) + currentLibrary)
                }

                val itemId = addedItem.id
                if (searchOnAdd && itemId != null) {
                    scope.launch {
                        executeAutomaticSearch(itemId)
                    }
                }

                scope.launch {
                    refreshLibrary()
                }

                delay(200.milliseconds)
                _addItemStatus.value = OperationStatus.Idle
                _lastAddedItemId.value = null
            }.onError { code, error, cause ->
                _addItemStatus.value = OperationStatus.Error(code, error, cause)
                delay(1500.milliseconds)
                _addItemStatus.value = OperationStatus.Idle
                _lastAddedItemId.value = null
            }
    }

    suspend fun getReleases(params: ReleaseParams) {
        _releases.value = NetworkResult.Loading

        client
            .getReleases(params)
            .mapValues { it.apply { mediaId = params.mediaId } }
            .onSuccess { releases ->
                logger.info { "Got releases: $releases" }
                _releases.value = NetworkResult.Success(releases)
            }.onError { code, message, cause ->
                logger.error(cause) { "Error getting releases: $message" }
                _releases.value = NetworkResult.Error(code, message, cause)
            }
    }

    suspend fun downloadRelease(payload: DownloadReleasePayload): NetworkResult<Any> {
        _downloadStatus.value = DownloadState.Loading(payload.guid)

        return client
            .downloadRelease(payload)
            .onSuccess {
                logger.info { "Download release success: $it" }
                _downloadStatus.value = DownloadState.Success
            }.onError { code, error, cause ->
                logger.error(cause) { "Download release error: $error" }
                _downloadStatus.value = DownloadState.Error
            }
    }

    fun resetDownloadStatus() {
        _downloadStatus.value = DownloadState.Initial
    }

    suspend fun deleteActivityTask(
        releaseId: Int,
        removeFromClient: Boolean,
        addToBlocklist: Boolean,
        skipRedownload: Boolean,
    ): NetworkResult<Unit> = client
        .deleteActivityTask(releaseId, removeFromClient, addToBlocklist, skipRedownload)
        .onSuccess {
            _activityTasks.value = _activityTasks.value.filter { it.id != releaseId }
        }

    suspend fun executeAutomaticSearch(itemId: Long) {
        _searchStatus.value = OperationStatus.InProgress

        client
            .performAutomaticSearch(itemId)
            .onSuccess {
                logger.info { "Search initiated: $it" }
                _searchStatus.value = OperationStatus.Success(mokoStrings.getString(MR.strings.search_initiated))
            }.onError { code, error, cause ->
                logger.error(cause) { "Search error: $error" }
                _searchStatus.value = OperationStatus.Error(code, error, cause)
            }.also {
                _searchStatus.value = OperationStatus.Idle
            }
    }

    suspend fun executeCommand(payload: CommandPayload): NetworkResult<Any> = client.command(payload)

    suspend fun getManualImportFiles(
        downloadId: String?,
        folder: String?,
        filterExistingFiles: Boolean = true,
    ): NetworkResult<List<ManualImportFile>> = client.getManualImportFiles(downloadId, folder, filterExistingFiles)

    suspend fun executeManualImport(
        files: List<ManualImportFile>,
        importMode: String = "auto",
    ): NetworkResult<Any> = client.command(CommandPayload.ManualImport(files, importMode))

    suspend fun getItemHistory(
        itemId: Long,
        altIt: Long? = null,
        page: Int = 1,
        pageSize: Int = 100,
    ): NetworkResult<List<HistoryItem>> {
        _historyStatus.value = OperationStatus.InProgress

        return client
            .getItemHistory(itemId, page, pageSize, altIt)
            .onSuccess { history ->
                val currentCache = _historyCache.value.toMutableMap()
                currentCache[itemId] = history
                _historyCache.value = currentCache
                _historyStatus.value = OperationStatus.Success()
            }.onError { code, message, cause ->
                _historyStatus.value = OperationStatus.Error(code, message, cause)
            }.also {
                _historyStatus.value = OperationStatus.Idle
            }
    }

    suspend fun getSeriesHistory(seriesId: Long): NetworkResult<List<HistoryItem>> {
        _historyStatus.value = OperationStatus.InProgress

        return (client as? SonarrClient)
            ?.getSeriesHistory(seriesId)
            ?.onSuccess { history ->
                val currentCache = _historyCache.value.toMutableMap()
                currentCache[seriesId] = history
                _historyCache.value = currentCache
                _historyStatus.value = OperationStatus.Success()
            }?.onError { code, message, cause ->
                _historyStatus.value = OperationStatus.Error(code, message, cause)
            }?.also {
                _historyStatus.value = OperationStatus.Idle
            } ?: NetworkResult.Error(message = mokoStrings.getString(MR.strings.not_a_sonarr_instance))
    }

    suspend fun refreshHistory(
        page: Int = 1,
        pageSize: Int = 100,
    ): NetworkResult<List<HistoryItem>> = client.getHistory(page, pageSize).onSuccess {
        _history.value = it
    }

    suspend fun refreshBlocklist(
        page: Int = 1,
        pageSize: Int = 100,
    ): NetworkResult<List<BlocklistItem>> {
        val remoteResult = client.getBlocklist(page, pageSize)
        if (remoteResult is NetworkResult.Success) {
            _blocklist.value = remoteResult.data.records
            _hasBlocklistLoaded.value = true
            return NetworkResult.Success(remoteResult.data.records)
        }
        val empty = emptyList<BlocklistItem>()
        _blocklist.value = empty
        _hasBlocklistLoaded.value = true
        return NetworkResult.Success(empty)
    }

    suspend fun refreshMissing(
        instance: Instance,
        page: Int = 1,
        pageSize: Int = 100,
    ): NetworkResult<List<WantedMediaItem>> {
        val useLibraryFilter = instance.type in listOf(
            InstanceType.Lidarr,
            InstanceType.Bookshelf,
            InstanceType.Chaptarr,
            InstanceType.Listenarr,
            InstanceType.Kapowarr,
        )

        if (!useLibraryFilter) {
            val remoteResult = client.getWantedMissing(page, pageSize)
            if (remoteResult is NetworkResult.Success) {
                val items = remoteResult.data.records.map { WantedMediaItem(it, instance.id, instance.label, instance.type) }
                _missing.value = items
                _hasMissingLoaded.value = true
                return NetworkResult.Success(items)
            }
        }

        val libResult = client.getLibrary()
        return when (libResult) {
            is NetworkResult.Success -> {
                val missingMedia = libResult.data.filter { it.isMissing }
                val wantedItems = missingMedia.map { WantedMediaItem(it, instance.id, instance.label, instance.type) }
                _missing.value = wantedItems
                _hasMissingLoaded.value = true
                NetworkResult.Success(wantedItems)
            }
            is NetworkResult.Error -> NetworkResult.Error(libResult.code, libResult.message, libResult.cause)
            else -> NetworkResult.Error(message = mokoStrings.getString(MR.strings.unknown_error))
        }
    }

    suspend fun refreshCutoff(
        instance: Instance,
        page: Int = 1,
        pageSize: Int = 100,
    ): NetworkResult<List<WantedMediaItem>> {
        val useLibraryFilter = instance.type in listOf(
            InstanceType.Lidarr,
            InstanceType.Bookshelf,
            InstanceType.Chaptarr,
            InstanceType.Listenarr,
            InstanceType.Kapowarr,
        )

        if (!useLibraryFilter) {
            val remoteResult = client.getWantedCutoff(page, pageSize)
            if (remoteResult is NetworkResult.Success) {
                val items = remoteResult.data.records.map { WantedMediaItem(it, instance.id, instance.label, instance.type) }
                _cutoffUnmet.value = items
                _hasCutoffLoaded.value = true
                return NetworkResult.Success(items)
            }
        }

        val empty = emptyList<WantedMediaItem>()
        _cutoffUnmet.value = empty
        _hasCutoffLoaded.value = true
        return NetworkResult.Success(empty)
    }

    suspend fun getFilesystem(path: String?): NetworkResult<List<FilesystemItem>> = client.getFilesystem(path)

    suspend fun deleteBlocklistItem(id: Long): NetworkResult<Unit> = client
        .deleteBlocklistItem(id)
        .onSuccess {
            _blocklist.value = _blocklist.value.filter { it.id != id }
        }

    suspend fun editMediaItem(
        item: ArrMedia,
        moveFiles: Boolean,
    ): NetworkResult<Unit> {
        _editItemStatus.value = OperationStatus.InProgress
        return client
            .edit(item, moveFiles)
            .onSuccess {
                val id = item.id ?: return@onSuccess
                val currentCache = _mediaDetailsCache.value.toMutableMap()
                currentCache[id] = item
                _mediaDetailsCache.value = currentCache

                updateItemInLibraryCache(item)
                _editItemStatus.value = OperationStatus.Success(mokoStrings.getString(MR.strings.item_edited_successfully))
            }.onError { code, message, cause ->
                _editItemStatus.value = OperationStatus.Error(code, message, cause)
            }
    }

    suspend fun updateMediaItem(item: ArrMedia): NetworkResult<ArrMedia> {
        _monitorStatus.value = OperationStatus.InProgress

        return client
            .update(item)
            .onSuccess { updateItem ->
                _monitorStatus.value = OperationStatus.Success(mokoStrings.getString(MR.strings.item_updated_successfully))

                val id = updateItem.id ?: return@onSuccess
                val currentCache = _mediaDetailsCache.value.toMutableMap()
                currentCache[id] = updateItem
                _mediaDetailsCache.value = currentCache

                updateItemInLibraryCache(updateItem)
            }.onError { code, message, cause ->
                _monitorStatus.value = OperationStatus.Error(code, message, cause)
            }.also {
                _monitorStatus.value = OperationStatus.Idle
            }
    }

    suspend fun delete(
        id: Long,
        deleteFiles: Boolean,
        addImportExclusion: Boolean,
    ): NetworkResult<Unit> = client
        .delete(id, deleteFiles, addImportExclusion)
        .onSuccess {
            val currentCache = _mediaDetailsCache.value.toMutableMap()
            currentCache.remove(id)
            _mediaDetailsCache.value = currentCache

            removeItemFromLibraryCache(id)
        }

    fun removeItemFromLibraryCache(id: Long) {
        val currentLibrary = _library.value
        if (currentLibrary is NetworkResult.Success) {
            _library.value =
                NetworkResult.Success(
                    currentLibrary.data.filterNot { it.id == id },
                )
        }
    }

    fun updateItemInLibraryCache(updatedItem: ArrMedia) {
        val currentLibrary = _library.value
        if (currentLibrary is NetworkResult.Success) {
            val updatedItems =
                currentLibrary.data.map { item ->
                    if (item.id == updatedItem.id) updatedItem else item
                }
            _library.value =
                NetworkResult.Success(
                    updatedItems,
                )
        }
    }

    suspend fun updateMonitoring(
        ids: List<Long>,
        monitor: Any,
    ): NetworkResult<Unit> {
        _monitorStatus.value = OperationStatus.InProgress

        return client
            .updateMonitoring(ids, monitor)
            .onSuccess {
                _monitorStatus.value = OperationStatus.Success(mokoStrings.getString(MR.strings.monitoring_updated_successfully))
            }.onError { code, message, cause ->
                _monitorStatus.value = OperationStatus.Error(code, message, cause)
            }.also {
                _monitorStatus.value = OperationStatus.Idle
            }
    }

    fun updateMonitoredInCache(
        id: Long,
        status: Boolean,
    ) {
        val libraryState = _library.value
        if (libraryState is NetworkResult.Success) {
            val updatedItems =
                libraryState.data.map { item ->
                    if (item.id == id) {
                        when (item) {
                            is ArrSeries -> item.copy(monitored = status)
                            is ArrMovie -> item.copy(monitored = status)
                            is Arrtist -> item.copy(monitored = status)
                            is Author -> item.copy(monitored = status)
                            is Audiobook -> item.copy(monitored = status)
                            is ComicVolume -> item.copy(monitored = status)
                            is SearchAudiobook -> item
                            is MockMedia -> item
                        }
                    } else {
                        item
                    }
                }
            _library.value =
                NetworkResult.Success(
                    updatedItems,
                )
        }

        val currentDetailsCache = _mediaDetailsCache.value
        currentDetailsCache[id]?.let { item ->
            val updatedMedia =
                when (item) {
                    is ArrSeries -> item.copy(monitored = status)
                    is ArrMovie -> item.copy(monitored = status)
                    is Arrtist -> item.copy(monitored = status)
                    is Author -> item.copy(monitored = status)
                    is Audiobook -> item.copy(monitored = status)
                    is ComicVolume -> item.copy(monitored = status)
                    is SearchAudiobook -> item
                    is MockMedia -> item
                }
            val updatedCache = currentDetailsCache.toMutableMap()
            updatedCache[id] = updatedMedia
            _mediaDetailsCache.value = updatedCache
        }
    }

    fun clearReleases() {
        _releases.value = null
    }

    fun observeCacheMediaDetails(id: Long): Flow<ArrMedia?> = _mediaDetailsCache.map {
        it[id]
    }

    fun getCacheMediaDetails(id: Long): ArrMedia? = _mediaDetailsCache.value[id]

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
            .map { cache -> cache[id] }
            .distinctUntilChanged()
            .collect { cached ->
                emit(
                    cached?.let { NetworkResult.Success(it) }
                        ?: NetworkResult.Error(message = mokoStrings.getString(MR.strings.media_not_found_in_cache)),
                )
            }
    }

    fun observeItemHistory(itemId: Long): Flow<List<HistoryItem>> = historyCache.map { cache ->
        cache[itemId] ?: emptyList()
    }

    fun setMonitorStatus(status: OperationStatus) {
        _monitorStatus.value = status
    }

    fun setEditItemStatus(status: OperationStatus) {
        _editItemStatus.value = status
    }

    fun updateMediaDetailsCache(
        id: Long,
        media: ArrMedia,
    ) {
        val currentCache = _mediaDetailsCache.value.toMutableMap()
        currentCache[id] = media
        _mediaDetailsCache.value = currentCache
    }
}
