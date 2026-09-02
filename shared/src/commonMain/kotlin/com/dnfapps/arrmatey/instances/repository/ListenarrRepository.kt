package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ListenarrClient
import com.dnfapps.arrmatey.arr.api.model.AddAudiobookBody
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.Audiobook
import com.dnfapps.arrmatey.arr.api.model.AudiobookFile
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataBody
import com.dnfapps.arrmatey.arr.api.model.AudiobookMetadataResponse
import com.dnfapps.arrmatey.arr.api.model.AudiobookPreviewPaths
import com.dnfapps.arrmatey.arr.api.model.ListenarrConfiguration
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.model.OperationStatus
import com.dnfapps.networking.NetworkResult
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import io.ktor.client.HttpClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.milliseconds

class ListenarrRepository(
    instance: Instance,
    httpClient: HttpClient,
    logger: Logger,
) : ArrInstanceRepository(instance, httpClient, logger) {
    val listenarrClient: ListenarrClient = client as? ListenarrClient ?: ListenarrClient(instance, httpClient)

    private val _audiobookFiles = MutableStateFlow<Map<Long, List<AudiobookFile>>>(emptyMap())
    override val audiobookFiles: StateFlow<Map<Long, List<AudiobookFile>>> = _audiobookFiles.asStateFlow()

    private val _listenarrConfiguration = MutableStateFlow(ListenarrConfiguration())
    override val listenarrConfiguration: StateFlow<ListenarrConfiguration> = _listenarrConfiguration.asStateFlow()

    override suspend fun refreshLibrary() {
        libraryRepository.refreshLibrary(
            onListenarrConfigUpdate = {
                listenarrClient.getConfigurationSettings().onSuccess {
                    _listenarrConfiguration.value = it
                }
            },
        )
    }

    override suspend fun performLookup(query: String) {
        val language = _listenarrConfiguration.value.defaultSearchLanguage
        val region = _listenarrConfiguration.value.defaultSearchRegion
        libraryRepository.performLookup(query, language, region)
    }

    override suspend fun directLookup(query: String): NetworkResult<List<ArrMedia>> {
        val language = _listenarrConfiguration.value.defaultSearchLanguage
        val region = _listenarrConfiguration.value.defaultSearchRegion
        return libraryRepository.directLookup(query, language, region)
    }

    override suspend fun getAudiobookFiles(audiobookId: Long): NetworkResult<List<AudiobookFile>> =
        listenarrClient
            .getDetail(audiobookId)
            .onSuccess { result ->
                val currentMap = _audiobookFiles.value.toMutableMap()
                currentMap[audiobookId] = result.files
                _audiobookFiles.value = currentMap
            }.map { it.files }

    override suspend fun getMetadata(
        asin: String,
        region: String,
    ): NetworkResult<AudiobookMetadataResponse> = listenarrClient.getMetadata(asin, region)

    override suspend fun getPreviewPath(
        rootPath: String,
        body: AudiobookMetadataBody,
    ): NetworkResult<AudiobookPreviewPaths> = listenarrClient.getPreviewPath(rootPath, body)

    override suspend fun addNewAudiobook(
        item: SearchAudiobook,
        metadata: AudiobookMetadataBody,
        searchOnAdd: Boolean,
    ) {
        libraryRepository.setEditItemStatus(OperationStatus.InProgress)
        delay(100.milliseconds)

        val path =
            item.rootFolderPath
                ?.trimEnd('/')
                ?.plus("/")
                ?.plus(item.path?.trimStart('/')) ?: ""
        val body =
            AddAudiobookBody(
                autoSearch = searchOnAdd,
                destinationPath = path,
                monitored = item.monitored,
                metadata = metadata,
            )
        val result =
            listenarrClient
                .addNewAudiobook(body)
                .map { it.audiobook as ArrMedia }

        libraryRepository.processAddResult(result, false)
    }

    override suspend fun moveAudiobookFiles(
        id: Long,
        sourcePath: String,
        destinationPath: String,
    ): NetworkResult<Unit> = listenarrClient.moveFiles(id, true, sourcePath, destinationPath)

    override suspend fun toggleAudiobookMonitor(audiobook: Audiobook): NetworkResult<Audiobook> {
        libraryRepository.setMonitorStatus(OperationStatus.InProgress)

        val updatedAudiobook = audiobook.copy(monitored = !audiobook.monitored)

        return updateMediaItem(updatedAudiobook)
            .map { it as Audiobook }
    }
}
