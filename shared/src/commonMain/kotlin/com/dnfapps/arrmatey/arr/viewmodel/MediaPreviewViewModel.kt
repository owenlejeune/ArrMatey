package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.state.MediaPreviewUiState
import com.dnfapps.arrmatey.arr.usecase.AddMediaItemUseCase
import com.dnfapps.arrmatey.arr.usecase.GetAudiobookMetadataUseCase
import com.dnfapps.arrmatey.arr.usecase.GetAudiobookPreviewPathUseCase
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveInstancePreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@OptIn(ExperimentalCoroutinesApi::class)
class MediaPreviewViewModel(
    private val preview: ArrMedia,
    private val instanceType: InstanceType,
    getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    private val addMediaUseCase: AddMediaItemUseCase,
    private val getAudiobookMetadataUseCase: GetAudiobookMetadataUseCase,
    private val getAudiobookPreviewPathUseCase: GetAudiobookPreviewPathUseCase,
    observeInstancePreferencesUseCase: ObserveInstancePreferencesUseCase,
    private val updateInstancePreferencesUseCase: UpdateInstancePreferencesUseCase,
): ViewModel() {

    private val selectedRepository = getArrInstanceRepositoryUseCase
        .observeSelected(instanceType)
        .filterNotNull()
        .distinctUntilChanged { old, new -> old.instance.id == new.instance.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    private val metadataResponse = selectedRepository
        .filterNotNull()
        .flatMapLatest { repository ->
            (preview as? SearchAudiobook)?.asin?.let { asin ->
                getAudiobookMetadataUseCase(asin, repository)
            } ?: flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    private val defaultRootFolder = selectedRepository
        .flatMapLatest { repository ->
            repository?.rootFolders?.map { folders ->
                folders.firstOrNull { it.isDefault }?.path
            } ?: flowOf(null)
        }

    private val previewPath: Flow<String> = combine(
        metadataResponse,
        defaultRootFolder
    ) { metadata, rootFolder ->
        if (rootFolder != null && metadata != null) {
            getAudiobookPreviewPathUseCase(rootFolder, metadata)
        } else {
            flowOf("")
        }
    }.flatMapLatest { it }


    private val preferences = observeInstancePreferencesUseCase(instanceType)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InstancePreferences()
        )


    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<MediaPreviewUiState> = selectedRepository
        .filterNotNull()
        .flatMapLatest { repository ->
            viewModelScope.launch {
                repository.refreshAllMetadata()
            }

            combine(
                combine(
                    repository.qualityProfiles,
                    repository.rootFolders,
                    repository.tags
                ) { qualityProfiles, rootFolders, tags ->
                    Triple(qualityProfiles, rootFolders, tags)
                },
                repository.addItemStatus,
                repository.lastAddedItemId,
                previewPath,
                preferences
            ) { (qualityProfiles, rootFolders, tags), addItemStatus, lastAddedItemId, previewPath, prefs ->
                MediaPreviewUiState(
                    qualityProfiles = qualityProfiles,
                    rootFolders = rootFolders,
                    tags = tags,
                    addItemStatus = addItemStatus,
                    lastAddedItemId = lastAddedItemId,
                    relativePath = previewPath,
                    preferences = prefs
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = MediaPreviewUiState()
        )

    fun addItem(item: ArrMedia, searchOnAdd: Boolean) {
        viewModelScope.launch {
            val metadata = metadataResponse.value
            addMediaUseCase(instanceType, item, metadata, searchOnAdd)
        }
    }

    fun updatePreferences(preferences: InstancePreferences) {
        viewModelScope.launch {
            selectedRepository.value?.instance?.id?.let { id ->
                updateInstancePreferencesUseCase(id, preferences)
            }
        }
    }
}