package com.dnfapps.arrmatey.arr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.SearchAudiobook
import com.dnfapps.arrmatey.arr.state.MediaPreviewUiState
import com.dnfapps.arrmatey.arr.usecase.AddMediaItemUseCase
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.usecase.GetInstancePresencesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetAudiobookMetadataUseCase
import com.dnfapps.arrmatey.arr.usecase.GetAudiobookPreviewPathUseCase
import com.dnfapps.arrmatey.datastore.InstancePreferences
import kotlinx.coroutines.flow.collectLatest
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveInstancePreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveScopedReposByTypeUseCase
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val observeScopedReposByTypeUseCase: ObserveScopedReposByTypeUseCase,
): ViewModel() {

    private val _selectedInstanceId = MutableStateFlow<Long?>(null)
    val selectedInstanceId: StateFlow<Long?> = _selectedInstanceId.asStateFlow()

    private val allArrReposFlow: Flow<List<ArrInstanceRepository>> =
        observeScopedReposByTypeUseCase(instanceType)
            .map { it.filterIsInstance<ArrInstanceRepository>() }

    private val _instancePresencesMap = MutableStateFlow<Map<Long, ArrMedia?>>(emptyMap())

    init {
        viewModelScope.launch {
            allArrReposFlow.collectLatest { repos ->
                val query = preview.title ?: ""
                val resolvedTvdbLookupId = (preview as? ArrSeries)?.tvdbId
                val resolvedLookupId = (preview as? ArrMovie)?.tmdbId

                val updatedMap = GetInstancePresencesUseCase().fetchMissingPresences(
                    repositories = repos,
                    query = query,
                    resolvedTvdbLookupId = resolvedTvdbLookupId,
                    resolvedLookupId = resolvedLookupId,
                    existingPresences = _instancePresencesMap.value
                )
                _instancePresencesMap.value = updatedMap
            }
        }
    }

    private val defaultSelectedArrRepoFlow: Flow<ArrInstanceRepository?> =
        getArrInstanceRepositoryUseCase.observeSelected(instanceType)

    private val activeArrRepoFlow: Flow<ArrInstanceRepository?> = combine(
        allArrReposFlow,
        defaultSelectedArrRepoFlow,
        _selectedInstanceId,
        _instancePresencesMap
    ) { allRepos, defaultRepo, selectedId, presencesMap ->
        val filteredRepos = allRepos.filter { repo ->
            val arrMedia = presencesMap[repo.instance.id]
            val isPresent = arrMedia?.let { it.id != null && it.id != 0L } ?: false
            !isPresent
        }

        if (selectedId != null) {
            allRepos.firstOrNull { it.instance.id == selectedId }
                ?: getArrInstanceRepositoryUseCase(selectedId)
                ?: defaultRepo
        } else {
            val isDefaultPresent = defaultRepo?.let { repo ->
                val arrMedia = presencesMap[repo.instance.id]
                arrMedia?.let { it.id != null && it.id != 0L } ?: false
            } ?: false

            if (defaultRepo != null && !isDefaultPresent) {
                defaultRepo
            } else {
                filteredRepos.firstOrNull() ?: defaultRepo ?: allRepos.firstOrNull()
            }
        }
    }

    private val selectedRepository = activeArrRepoFlow
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


    private val availableInstances: Flow<List<Instance>> = combine(
        allArrReposFlow,
        _instancePresencesMap
    ) { repos, presencesMap ->
        repos.filter { repo ->
            val arrMedia = presencesMap[repo.instance.id]
            val isPresent = arrMedia?.let { it.id != null && it.id != 0L } ?: false
            !isPresent
        }.map { it.instance }
    }

    private val selectedInstance: Flow<Instance?> = selectedRepository
        .map { it?.instance }

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
                combine(
                    repository.addItemStatus,
                    repository.lastAddedItemId,
                    previewPath
                ) { addItemStatus, lastAddedItemId, previewPath ->
                    Triple(addItemStatus, lastAddedItemId, previewPath)
                },
                combine(
                    preferences,
                    availableInstances,
                    selectedInstance
                ) { prefs, instances, selected ->
                    Triple(prefs, instances, selected)
                }
            ) { (qualityProfiles, rootFolders, tags), (addItemStatus, lastAddedItemId, previewPath), (prefs, instances, selected) ->
                MediaPreviewUiState(
                    qualityProfiles = qualityProfiles,
                    rootFolders = rootFolders,
                    tags = tags,
                    addItemStatus = addItemStatus,
                    lastAddedItemId = lastAddedItemId,
                    relativePath = previewPath,
                    preferences = prefs,
                    instances = instances,
                    selectedInstance = selected
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
            val targetId = selectedRepository.value?.instance?.id
            addMediaUseCase(instanceType, item, metadata, searchOnAdd, targetInstanceId = targetId)
        }
    }

    fun selectInstance(instance: Instance) {
        _selectedInstanceId.value = instance.id
    }

    fun updatePreferences(preferences: InstancePreferences) {
        viewModelScope.launch {
            selectedRepository.value?.instance?.id?.let { id ->
                updateInstancePreferencesUseCase(id, preferences)
            }
        }
    }
}