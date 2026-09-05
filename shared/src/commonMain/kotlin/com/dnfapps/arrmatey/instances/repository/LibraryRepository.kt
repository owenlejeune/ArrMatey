package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ArrClient
import com.dnfapps.arrmatey.arr.api.client.LookupParams
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.api.model.Arrtist
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.Author
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.DownloadReleasePayload
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.MockMedia
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.state.DownloadState
import com.dnfapps.arrmatey.extensions.mergeWithLibrary
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.asSuccess
import com.dnfapps.networking.mapValues
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.milliseconds

class LibraryRepository(
    private val client: ArrClient,
    private val logger: Logger,
) {
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
    val mediaDetailsCache: StateFlow<Map<Long, ArrMedia>> = _mediaDetailsCache.asStateFlow()

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
        _library.value = NetworkResult.Loading
        _library.value = client.getLibrary()
        onBookLibraryUpdate?.invoke()
        onListenarrConfigUpdate?.invoke()
    }

    suspend fun getMediaDetails(id: Long): NetworkResult<ArrMedia> =
        client
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

        val result = client.addItemToLibrary(item)
        processAddResult(result, searchOnAdd)
    }

    suspend fun processAddResult(
        result: NetworkResult<ArrMedia>,
        searchOnAdd: Boolean,
    ) {
        result
            .onSuccess { addedItem ->
                _addItemStatus.value = OperationStatus.Success("Item added successfully")
                addedItem.id?.let {
                    val newMap = _mediaDetailsCache.value.toMutableMap()
                    newMap[it] = addedItem
                    _mediaDetailsCache.value = newMap

                    if (searchOnAdd) {
                        executeAutomaticSearch(it)
                    }
                }
                _lastAddedItemId.value = addedItem.id
                refreshLibrary()
                delay(1500.milliseconds)
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
    ): NetworkResult<Unit> = client.deleteActivityTask(releaseId, removeFromClient, addToBlocklist, skipRedownload)

    suspend fun executeAutomaticSearch(itemId: Long) {
        _searchStatus.value = OperationStatus.InProgress

        client
            .performAutomaticSearch(itemId)
            .onSuccess {
                logger.info { "Search initiated: $it" }
                _searchStatus.value = OperationStatus.Success("Search initiated")
            }.onError { code, error, cause ->
                logger.error(cause) { "Search error: $error" }
                _searchStatus.value = OperationStatus.Error(code, error, cause)
            }.also {
                _searchStatus.value = OperationStatus.Idle
            }
    }

    suspend fun executeCommand(payload: CommandPayload): NetworkResult<Any> = client.command(payload)

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
                _editItemStatus.value = OperationStatus.Success("Item edited successfully")
            }.onError { code, message, cause ->
                _editItemStatus.value = OperationStatus.Error(code, message, cause)
            }
    }

    suspend fun updateMediaItem(item: ArrMedia): NetworkResult<ArrMedia> {
        _monitorStatus.value = OperationStatus.InProgress

        return client
            .update(item)
            .onSuccess { updateItem ->
                _monitorStatus.value = OperationStatus.Success("Item updated successfully")

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
    ): NetworkResult<Unit> =
        client
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
                _monitorStatus.value = OperationStatus.Success("Monitoring updated successfully")
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

    fun observeCacheMediaDetails(id: Long): Flow<ArrMedia?> =
        _mediaDetailsCache.map {
            it[id]
        }

    fun getCacheMediaDetails(id: Long): ArrMedia? = _mediaDetailsCache.value[id]

    fun observeMediaDetails(id: Long): Flow<NetworkResult<ArrMedia>> =
        flow {
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
                }.collect { emit(it) }
        }

    fun observeItemHistory(itemId: Long): Flow<List<HistoryItem>> =
        historyCache.map { cache ->
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
