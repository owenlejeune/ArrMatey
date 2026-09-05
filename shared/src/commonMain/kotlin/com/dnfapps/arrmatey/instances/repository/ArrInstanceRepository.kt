package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ArrClient
import com.dnfapps.arrmatey.arr.api.client.BookshelfClient
import com.dnfapps.arrmatey.arr.api.client.LidarrClient
import com.dnfapps.arrmatey.arr.api.client.ListenarrClient
import com.dnfapps.arrmatey.arr.api.client.RadarrClient
import com.dnfapps.arrmatey.arr.api.client.SonarrClient
import com.dnfapps.arrmatey.arr.api.model.ArrAlbum
import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrRelease
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.AudiobookFile
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataBody
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataResponse
import com.dnfapps.arrmatey.arr.api.model.AudiobookPreviewPaths
import com.dnfapps.arrmatey.arr.api.model.Book
import com.dnfapps.arrmatey.arr.api.model.BookEdition
import com.dnfapps.arrmatey.arr.api.model.BookFile
import com.dnfapps.arrmatey.arr.api.model.BookSeries
import com.dnfapps.arrmatey.arr.api.model.CommandPayload
import com.dnfapps.arrmatey.arr.api.model.CustomFilter
import com.dnfapps.arrmatey.arr.api.model.DownloadReleasePayload
import com.dnfapps.arrmatey.arr.api.model.Episode
import com.dnfapps.arrmatey.arr.api.model.ExtraFile
import com.dnfapps.arrmatey.arr.api.model.HistoryItem
import com.dnfapps.arrmatey.arr.api.model.LidarrTrack
import com.dnfapps.arrmatey.arr.api.model.LidarrTrackFile
import com.dnfapps.arrmatey.arr.api.model.ListenarrConfiguration
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.QueueItem
import com.dnfapps.arrmatey.arr.api.model.ReleaseParams
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.state.DownloadState
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.NetworkResult
import dev.shivathapaa.logger.api.Logger
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf

open class ArrInstanceRepository(
    override val instance: Instance,
    protected val httpClient: HttpClient,
    protected val logger: Logger,
) : InstanceScopedRepository {
    val client: ArrClient = createClient()

    protected val libraryRepository = LibraryRepository(client, logger)
    protected val metadataRepository = MetadataRepository(client, logger)

    val library: StateFlow<NetworkResult<List<ArrMedia>>?> = libraryRepository.library
    val lookupResults: StateFlow<NetworkResult<List<ArrMedia>>?> = libraryRepository.lookupResults
    val lastAddedItemId: StateFlow<Long?> = libraryRepository.lastAddedItemId
    val releases: StateFlow<NetworkResult<List<ArrRelease>>?> = libraryRepository.releases
    val historyCache: StateFlow<Map<Long, List<HistoryItem>>> = libraryRepository.historyCache
    val mediaDetailsCache: StateFlow<Map<Long, ArrMedia>> = libraryRepository.mediaDetailsCache
    val activityTasks: StateFlow<List<QueueItem>> = libraryRepository.activityTasks
    val addItemStatus: StateFlow<OperationStatus> = libraryRepository.addItemStatus
    val editItemStatus: StateFlow<OperationStatus> = libraryRepository.editItemStatus
    val searchStatus: StateFlow<OperationStatus> = libraryRepository.searchStatus
    val downloadStatus: StateFlow<DownloadState> = libraryRepository.downloadStatus
    val monitorStatus: StateFlow<OperationStatus> = libraryRepository.monitorStatus
    val historyStatus: StateFlow<OperationStatus> = libraryRepository.historyStatus

    val qualityProfiles: StateFlow<List<QualityProfile>> = metadataRepository.qualityProfiles
    val rootFolders: StateFlow<List<RootFolder>> = metadataRepository.rootFolders
    val tags: StateFlow<List<Tag>> = metadataRepository.tags
    val customFilters: StateFlow<List<CustomFilter>> = metadataRepository.customFilters
    val softwareStatus: StateFlow<ArrSoftwareStatus?> = metadataRepository.softwareStatus
    val diskSpace: StateFlow<List<ArrDiskSpace>> = metadataRepository.diskSpace
    val health: StateFlow<List<ArrHealth>> = metadataRepository.health

    // Type-specific state forwarding
    open val episodes: StateFlow<Map<Long, List<Episode>>> get() = (this as? SonarrRepository)?.episodes ?: MutableStateFlow(emptyMap())
    open val movieExtraFiles: StateFlow<Map<Long, List<ExtraFile>>> get() =
        (this as? RadarrRepository)?.movieExtraFiles
            ?: MutableStateFlow(emptyMap())
    open val artistAlbums: StateFlow<Map<Long, List<ArrAlbum>>> get() =
        (this as? LidarrRepository)?.artistAlbums
            ?: MutableStateFlow(emptyMap())
    open val artistTracks: StateFlow<Map<Long, Map<Long, List<LidarrTrack>>>> get() =
        (this as? LidarrRepository)?.artistTracks
            ?: MutableStateFlow(emptyMap())
    open val artistTrackFiles: StateFlow<Map<Long, Map<Long, List<LidarrTrackFile>>>> get() =
        (this as? LidarrRepository)?.artistTrackFiles
            ?: MutableStateFlow(emptyMap())
    open val authorSeries: StateFlow<Map<Long, List<BookSeries>>> get() =
        (this as? ReadarrRepository)?.authorSeries
            ?: MutableStateFlow(emptyMap())
    open val authorBookFiles: StateFlow<Map<Long, List<BookFile>>> get() =
        (this as? ReadarrRepository)?.authorBookFiles
            ?: MutableStateFlow(emptyMap())
    open val booksLibrary: StateFlow<List<Book>> get() = (this as? ReadarrRepository)?.booksLibrary ?: MutableStateFlow(emptyList())
    open val authorBooks: Flow<Map<Long, List<Book>>> get() = (this as? ReadarrRepository)?.authorBooks ?: flowOf(emptyMap())
    open val audiobookFiles: StateFlow<Map<Long, List<AudiobookFile>>> get() =
        (this as? ListenarrRepository)?.audiobookFiles
            ?: MutableStateFlow(emptyMap())
    open val listenarrConfiguration: StateFlow<ListenarrConfiguration> get() =
        (this as? ListenarrRepository)?.listenarrConfiguration
            ?: MutableStateFlow(ListenarrConfiguration())

    private fun createClient(): ArrClient =
        when (instance.type) {
            InstanceType.Sonarr -> SonarrClient(instance, httpClient)
            InstanceType.Radarr -> RadarrClient(instance, httpClient)
            InstanceType.Lidarr -> LidarrClient(instance, httpClient)
            InstanceType.Bookshelf -> BookshelfClient(instance, httpClient)
            InstanceType.Listenarr -> ListenarrClient(instance, httpClient)
            else -> TODO()
        }

    override suspend fun testConnection(): NetworkResult<Unit> = client.testConnection()

    open suspend fun refreshLibrary() {
        libraryRepository.refreshLibrary()
    }

    open suspend fun getMediaDetails(id: Long): NetworkResult<ArrMedia> = libraryRepository.getMediaDetails(id)

    open suspend fun refreshQualityProfiles() {
        metadataRepository.refreshQualityProfiles()
    }

    open suspend fun refreshRootFolders() {
        metadataRepository.refreshRootFolders()
    }

    open suspend fun refreshTags() {
        metadataRepository.refreshTags()
    }

    open suspend fun refreshCustomFilters() {
        metadataRepository.refreshCustomFilters()
    }

    open suspend fun refreshStatus() {
        metadataRepository.refreshStatus()
    }

    open suspend fun refreshDiskSpace() {
        metadataRepository.refreshDiskSpace()
    }

    open suspend fun refreshHealth() {
        metadataRepository.refreshHealth()
    }

    open suspend fun refreshAllMetadata(force: Boolean = false) {
        metadataRepository.refreshAllMetadata(force)
    }

    open suspend fun refreshInstanceStatuses() {
        metadataRepository.refreshInstanceStatuses()
    }

    open suspend fun refreshActivityTasks(
        page: Int = 1,
        pageSize: Int = 100,
    ) {
        libraryRepository.refreshActivityTasks(page, pageSize)
    }

    open suspend fun performLookup(query: String) {
        libraryRepository.performLookup(query)
    }

    open fun clearLookup() {
        libraryRepository.clearLookup()
    }

    open suspend fun directLookup(query: String): NetworkResult<List<ArrMedia>> = libraryRepository.directLookup(query)

    open suspend fun addItem(
        item: ArrMedia,
        searchOnAdd: Boolean,
    ) {
        libraryRepository.addItem(item, searchOnAdd)
    }

    open suspend fun getReleases(params: ReleaseParams) {
        libraryRepository.getReleases(params)
    }

    open suspend fun downloadRelease(payload: DownloadReleasePayload): NetworkResult<Any> = libraryRepository.downloadRelease(payload)

    open fun resetDownloadStatus() {
        libraryRepository.resetDownloadStatus()
    }

    open fun resetEditItemStatus() {
        libraryRepository.setEditItemStatus(OperationStatus.Idle)
    }

    open suspend fun deleteActivityTask(
        releaseId: Int,
        removeFromClient: Boolean,
        addToBlocklist: Boolean,
        skipRedownload: Boolean,
    ): NetworkResult<Unit> = libraryRepository.deleteActivityTask(releaseId, removeFromClient, addToBlocklist, skipRedownload)

    open suspend fun executeAutomaticSearch(itemId: Long) {
        libraryRepository.executeAutomaticSearch(itemId)
    }

    open suspend fun executeCommand(payload: CommandPayload): NetworkResult<Any> = libraryRepository.executeCommand(payload)

    open suspend fun getItemHistory(
        itemId: Long,
        altIt: Long? = null,
        page: Int = 1,
        pageSize: Int = 100,
    ): NetworkResult<List<HistoryItem>> = libraryRepository.getItemHistory(itemId, altIt, page, pageSize)

    open suspend fun editMediaItem(
        item: ArrMedia,
        moveFiles: Boolean,
    ): NetworkResult<Unit> = libraryRepository.editMediaItem(item, moveFiles)

    open suspend fun updateMediaItem(item: ArrMedia): NetworkResult<ArrMedia> = libraryRepository.updateMediaItem(item)

    open suspend fun delete(
        id: Long,
        deleteFiles: Boolean,
        addImportExclusion: Boolean,
    ): NetworkResult<Unit> = libraryRepository.delete(id, deleteFiles, addImportExclusion)

    open suspend fun updateMonitoring(
        ids: List<Long>,
        monitor: Any,
    ): NetworkResult<Unit> = libraryRepository.updateMonitoring(ids, monitor)

    open fun updateMonitoredInCache(
        id: Long,
        status: Boolean,
    ) {
        libraryRepository.updateMonitoredInCache(id, status)
    }

    open fun clearReleases() {
        libraryRepository.clearReleases()
    }

    open fun observeCacheMediaDetails(id: Long): Flow<ArrMedia?> = libraryRepository.observeCacheMediaDetails(id)

    open fun getCacheMediaDetails(id: Long): ArrMedia? = libraryRepository.getCacheMediaDetails(id)

    open fun observeMediaDetails(id: Long): Flow<NetworkResult<ArrMedia>> = libraryRepository.observeMediaDetails(id)

    open fun observeItemHistory(itemId: Long): Flow<List<HistoryItem>> = libraryRepository.observeItemHistory(itemId)

    // Sonarr forwarded methods
    open suspend fun getEpisodes(
        seriesId: Long,
        seasonNumber: Int? = null,
    ): NetworkResult<List<Episode>> =
        (this as? SonarrRepository)?.getEpisodes(seriesId, seasonNumber) ?: NetworkResult.Error(message = "Not a Sonarr instance")

    open suspend fun toggleSeasonMonitor(
        id: Long,
        seasonNumber: Int,
    ): NetworkResult<ArrMedia> =
        (this as? SonarrRepository)?.toggleSeasonMonitor(id, seasonNumber) ?: NetworkResult.Error(message = "Not a Sonarr instance")

    open suspend fun toggleEpisodeMonitor(episode: Episode): NetworkResult<Episode> =
        (this as? SonarrRepository)?.toggleEpisodeMonitor(episode) ?: NetworkResult.Error(message = "Not a Sonarr instance")

    open suspend fun deleteSeasonFiles(
        seriesId: Long,
        seasonNumber: Int,
    ): NetworkResult<Unit> =
        (this as? SonarrRepository)?.deleteSeasonFiles(seriesId, seasonNumber) ?: NetworkResult.Error(message = "Not a Sonarr instance")

    open suspend fun deleteEpisodes(
        seriesId: Long,
        episodes: List<Episode>,
    ): NetworkResult<Unit> =
        (this as? SonarrRepository)?.deleteEpisodes(seriesId, episodes) ?: NetworkResult.Error(message = "Not a Sonarr instance")

    open suspend fun deleteEpisodeFile(
        seriesId: Long,
        fileId: Long,
    ): NetworkResult<Unit> =
        (this as? SonarrRepository)?.deleteEpisodeFile(seriesId, fileId) ?: NetworkResult.Error(message = "Not a Sonarr instance")

    // Radarr forwarded methods
    open suspend fun getMovieExtraFiles(movieId: Long): NetworkResult<List<ExtraFile>> =
        (this as? RadarrRepository)?.getMovieExtraFiles(movieId) ?: NetworkResult.Error(message = "Not a Radarr instance")

    open suspend fun deleteMovieFile(movieId: Long): NetworkResult<Unit> =
        (this as? RadarrRepository)?.deleteMovieFile(movieId) ?: NetworkResult.Error(message = "Not a Radarr instance")

    // Lidarr forwarded methods
    open suspend fun getArtistAlbums(artistId: Long): NetworkResult<List<ArrAlbum>> =
        (this as? LidarrRepository)?.getArtistAlbums(artistId) ?: NetworkResult.Error(message = "Not a Lidarr instance")

    open suspend fun getArtistTracks(artistId: Long): NetworkResult<List<LidarrTrack>> =
        (this as? LidarrRepository)?.getArtistTracks(artistId) ?: NetworkResult.Error(message = "Not a Lidarr instance")

    open suspend fun getArtistTrackFiles(artistId: Long): NetworkResult<List<LidarrTrackFile>> =
        (this as? LidarrRepository)?.getArtistTrackFiles(artistId) ?: NetworkResult.Error(message = "Not a Lidarr instance")

    open suspend fun deleteAlbumFiles(
        artistId: Long,
        albumId: Long,
    ): NetworkResult<Unit> =
        (this as? LidarrRepository)?.deleteAlbumFiles(artistId, albumId) ?: NetworkResult.Error(message = "Not a Lidarr instance")

    open suspend fun deleteTrackFiles(tracks: List<LidarrTrackFile>): NetworkResult<Unit> =
        (this as? LidarrRepository)?.deleteTrackFiles(tracks) ?: NetworkResult.Error(message = "Not a Lidarr instance")

    open suspend fun toggleAlbumMonitor(album: ArrAlbum): NetworkResult<ArrAlbum> =
        (this as? LidarrRepository)?.toggleAlbumMonitor(album) ?: NetworkResult.Error(message = "Not a Lidarr instance")

    open suspend fun updateAlbum(album: ArrAlbum): NetworkResult<ArrAlbum> =
        (this as? LidarrRepository)?.updateAlbum(album) ?: NetworkResult.Error(message = "Not a Lidarr instance")

    // Readarr forwarded methods
    open suspend fun getAuthorSeries(authorId: Long): NetworkResult<List<BookSeries>> =
        (this as? ReadarrRepository)?.getAuthorSeries(authorId) ?: NetworkResult.Error(message = "Not a Readarr instance")

    open suspend fun getAuthorBookFiles(authorId: Long): NetworkResult<List<BookFile>> =
        (this as? ReadarrRepository)?.getAuthorBookFiles(authorId) ?: NetworkResult.Error(message = "Not a Readarr instance")

    open suspend fun deleteBookFiles(bookFilesIds: List<Long>): NetworkResult<Unit> =
        (this as? ReadarrRepository)?.deleteBookFiles(bookFilesIds) ?: NetworkResult.Error(message = "Not a Readarr instance")

    open suspend fun toggleBookMonitor(book: Book): NetworkResult<Book> =
        (this as? ReadarrRepository)?.toggleBookMonitor(book) ?: NetworkResult.Error(message = "Not a Readarr instance")

    open suspend fun getBookEditions(bookId: Long): NetworkResult<List<BookEdition>> =
        (this as? ReadarrRepository)?.getBookEditions(bookId) ?: NetworkResult.Error(message = "Not a Readarr instance")

    // Listenarr forwarded methods
    open suspend fun getAudiobookFiles(audiobookId: Long): NetworkResult<List<AudiobookFile>> =
        (this as? ListenarrRepository)?.getAudiobookFiles(audiobookId) ?: NetworkResult.Error(message = "Not a Listenarr instance")

    open suspend fun getMetadata(
        asin: String,
        region: String,
    ): NetworkResult<AudiobookMetadataResponse> =
        (this as? ListenarrRepository)?.getMetadata(asin, region) ?: NetworkResult.Error(message = "Not a Listenarr instance")

    open suspend fun getPreviewPath(
        rootPath: String,
        body: AudiobookMetadataBody,
    ): NetworkResult<AudiobookPreviewPaths> =
        (this as? ListenarrRepository)?.getPreviewPath(rootPath, body) ?: NetworkResult.Error(message = "Not a Listenarr instance")

    open suspend fun addNewAudiobook(
        item: SearchAudiobook,
        metadata: AudiobookMetadataBody,
        searchOnAdd: Boolean,
    ) {
        (this as? ListenarrRepository)?.addNewAudiobook(item, metadata, searchOnAdd)
    }

    open suspend fun moveAudiobookFiles(
        id: Long,
        sourcePath: String,
        destinationPath: String,
    ): NetworkResult<Unit> =
        (this as? ListenarrRepository)?.moveAudiobookFiles(id, sourcePath, destinationPath)
            ?: NetworkResult.Error(message = "Not a Listenarr instance")

    open suspend fun toggleAudiobookMonitor(audiobook: Audiobook): NetworkResult<Audiobook> =
        (this as? ListenarrRepository)?.toggleAudiobookMonitor(audiobook) ?: NetworkResult.Error(message = "Not a Listenarr instance")
}
