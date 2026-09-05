package com.dnfapps.arrmatey.instances.repository

import com.dnfapps.arrmatey.arr.api.client.ArrClient
import com.dnfapps.arrmatey.arr.api.model.ArrDiskSpace
import com.dnfapps.arrmatey.arr.api.model.ArrHealth
import com.dnfapps.arrmatey.arr.api.model.ArrSoftwareStatus
import com.dnfapps.arrmatey.arr.api.model.CustomFilter
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.networking.onError
import com.dnfapps.networking.onSuccess
import dev.shivathapaa.logger.api.Logger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

class MetadataRepository(
    private val client: ArrClient,
    private val logger: Logger,
) {
    private val _qualityProfiles = MutableStateFlow<List<QualityProfile>>(emptyList())
    val qualityProfiles: StateFlow<List<QualityProfile>> = _qualityProfiles.asStateFlow()

    private val _rootFolders = MutableStateFlow<List<RootFolder>>(emptyList())
    val rootFolders: StateFlow<List<RootFolder>> = _rootFolders.asStateFlow()

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    private val _customFilters = MutableStateFlow<List<CustomFilter>>(emptyList())
    val customFilters: StateFlow<List<CustomFilter>> = _customFilters.asStateFlow()

    private val _softwareStatus = MutableStateFlow<ArrSoftwareStatus?>(null)
    val softwareStatus: StateFlow<ArrSoftwareStatus?> = _softwareStatus.asStateFlow()

    private val _diskSpace = MutableStateFlow<List<ArrDiskSpace>>(emptyList())
    val diskSpace: StateFlow<List<ArrDiskSpace>> = _diskSpace.asStateFlow()

    private val _health = MutableStateFlow<List<ArrHealth>>(emptyList())
    val health: StateFlow<List<ArrHealth>> = _health.asStateFlow()

    suspend fun refreshQualityProfiles() {
        client
            .getQualityProfiles()
            .onSuccess { _qualityProfiles.value = it }
    }

    suspend fun refreshRootFolders() {
        client
            .getRootFolders()
            .onSuccess { _rootFolders.value = it }
            .onError { code, message, cause ->
                logger.error(cause) { "Error refreshing root folders: $message (code=$code)" }
            }
    }

    suspend fun refreshTags() {
        client
            .getTags()
            .onSuccess { _tags.value = it }
    }

    suspend fun refreshCustomFilters() {
        client
            .getCustomFilters()
            .onSuccess { _customFilters.value = it }
            .onError { code, message, cause ->
                logger.error(cause) { "Error refreshing custom filters: $message (code=$code)" }
            }
    }

    suspend fun refreshStatus() {
        client
            .getStatus()
            .onSuccess { _softwareStatus.value = it }
    }

    suspend fun refreshDiskSpace() {
        client
            .getDiskSpace()
            .onSuccess { _diskSpace.value = it }
    }

    suspend fun refreshHealth() {
        client
            .getHealth()
            .onSuccess { _health.value = it }
    }

    private val metadataMutex = Mutex()
    private var lastMetadataRefreshTime: Long = 0L
    private val refreshThresholdMs: Long = 30_000L

    suspend fun refreshAllMetadata(force: Boolean = false) {
        metadataMutex.withLock {
            val now = Clock.System.now().toEpochMilliseconds()
            if (!force && (now - lastMetadataRefreshTime) < refreshThresholdMs) {
                return
            }
            lastMetadataRefreshTime = now
            coroutineScope {
                launch { refreshQualityProfiles() }
                launch { refreshRootFolders() }
                launch { refreshTags() }
                launch { refreshCustomFilters() }
            }
        }
    }

    suspend fun refreshInstanceStatuses() {
        coroutineScope {
            launch { refreshStatus() }
            launch { refreshDiskSpace() }
            launch { refreshHealth() }
        }
    }
}
