package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.QualityProfile
import com.dnfapps.arrmatey.arr.api.model.RootFolder
import com.dnfapps.arrmatey.arr.api.model.Tag
import com.dnfapps.arrmatey.arr.service.ActivityQueueService
import com.dnfapps.arrmatey.datastore.InstancePreferences
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.instances.usecase.GetArrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetBazarrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.GetSeerrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveInstancePreferencesUseCase
import com.dnfapps.arrmatey.instances.usecase.ObserveScopedReposByTypeUseCase
import com.dnfapps.arrmatey.instances.usecase.UpdateInstancePreferencesUseCase
import com.dnfapps.arrmatey.model.AddSheetUiState
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedMediaDetailsInstanceHandler(
    initialForcedInstanceId: Long?,
    private val arrId: Long?,
    private val resolvedInstanceType: InstanceType?,
    private val resolvedRequestType: RequestType?,
    private val scope: CoroutineScope,
    private val getArrInstanceRepositoryUseCase: GetArrInstanceRepositoryUseCase,
    getSeerrInstanceRepositoryUseCase: GetSeerrInstanceRepositoryUseCase,
    getBazarrInstanceRepositoryUseCase: GetBazarrInstanceRepositoryUseCase,
    observeInstancePreferencesUseCase: ObserveInstancePreferencesUseCase,
    private val updateInstancePreferencesUseCase: UpdateInstancePreferencesUseCase,
    observeScopedReposByTypeUseCase: ObserveScopedReposByTypeUseCase,
    preferencesStore: PreferencesStore,
    private val activityQueueService: ActivityQueueService,
) {
    var initialInstanceId: Long? = initialForcedInstanceId
        private set

    private val _selectedInstanceId = MutableStateFlow<Long?>(initialForcedInstanceId)
    val selectedInstanceId: StateFlow<Long?> = _selectedInstanceId.asStateFlow()

    private val _instancePresencesMap = MutableStateFlow<Map<Long, ArrMedia?>>(emptyMap())
    val instancePresencesMap: StateFlow<Map<Long, ArrMedia?>> = _instancePresencesMap.asStateFlow()

    fun updatePresencesMap(newMap: Map<Long, ArrMedia?>) {
        _instancePresencesMap.value = newMap
    }

    private val _addSheetUiState = MutableStateFlow(AddSheetUiState())
    val addSheetUiState: StateFlow<AddSheetUiState> = _addSheetUiState.asStateFlow()

    fun updateAddSheetUiState(transform: (AddSheetUiState) -> AddSheetUiState) {
        _addSheetUiState.update(transform)
    }

    private val _qualityProfiles = MutableStateFlow<List<QualityProfile>>(emptyList())
    val qualityProfiles: StateFlow<List<QualityProfile>> = _qualityProfiles.asStateFlow()

    fun updateQualityProfiles(profiles: List<QualityProfile>) {
        _qualityProfiles.value = profiles
    }

    private val _rootFolders = MutableStateFlow<List<RootFolder>>(emptyList())
    val rootFolders: StateFlow<List<RootFolder>> = _rootFolders.asStateFlow()

    fun updateRootFolders(folders: List<RootFolder>) {
        _rootFolders.value = folders
    }

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    fun updateTags(tags: List<Tag>) {
        _tags.value = tags
    }

    val allArrReposFlow: Flow<List<ArrInstanceRepository>> =
        if (resolvedInstanceType != null) {
            observeScopedReposByTypeUseCase(resolvedInstanceType)
                .map { it.filterIsInstance<ArrInstanceRepository>() }
        } else {
            flowOf(emptyList())
        }

    val availableInstances: StateFlow<List<Instance>> =
        allArrReposFlow
            .map { repos -> repos.map { it.instance } }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val defaultSelectedArrRepoFlow: Flow<ArrInstanceRepository?> =
        if (resolvedInstanceType != null) {
            getArrInstanceRepositoryUseCase.observeSelected(resolvedInstanceType)
        } else {
            flowOf(null)
        }

    val activeArrRepoFlow: Flow<ArrInstanceRepository?> =
        combine(
            allArrReposFlow,
            defaultSelectedArrRepoFlow,
            _selectedInstanceId,
        ) { allRepos, defaultRepo, selectedId ->
            if (selectedId != null) {
                allRepos.firstOrNull { it.instance.id == selectedId }
                    ?: getArrInstanceRepositoryUseCase(selectedId)
                    ?: defaultRepo
            } else {
                defaultRepo ?: allRepos.firstOrNull()
            }
        }

    val seerrRepositoryFlow: Flow<SeerrInstanceRepository?> =
        combine(
            getSeerrInstanceRepositoryUseCase.observeSelected(),
            preferencesStore.combineSeerrArrMedia,
        ) { repo, combine ->
            if (!combine && arrId != null) null else repo
        }

    val bazarrRepositoryFlow: Flow<BazarrInstanceRepository?> =
        combine(
            getBazarrInstanceRepositoryUseCase.observeSelected(),
            preferencesStore.bazarrDetailsIntegration,
        ) { repo, enabled ->
            if (enabled) repo else null
        }

    val activeInstance: StateFlow<Instance?> =
        activeArrRepoFlow
            .map { it?.instance }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val activeSeerrInstance: StateFlow<Instance?> =
        seerrRepositoryFlow
            .map { it?.instance }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null,
            )

    val isSeerrConfigured: StateFlow<Boolean> =
        seerrRepositoryFlow
            .map { repo ->
                repo != null && (resolvedRequestType == RequestType.Movie || resolvedRequestType == RequestType.Tv)
            }.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )

    val isArrConfigured: StateFlow<Boolean> =
        activeArrRepoFlow
            .map { it != null }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)

    private val targetOrActiveRepoFlow: Flow<ArrInstanceRepository?> =
        combine(
            activeArrRepoFlow,
            _addSheetUiState,
        ) { activeRepo, addSheetState ->
            val targetId = addSheetState.targetInstance?.id
            if (targetId != null) {
                getArrInstanceRepositoryUseCase(targetId) ?: activeRepo
            } else {
                activeRepo
            }
        }

    val preferences: StateFlow<InstancePreferences> =
        targetOrActiveRepoFlow
            .filterNotNull()
            .flatMapLatest { repo ->
                observeInstancePreferencesUseCase(repo.instance.id)
            }.stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = InstancePreferences(),
            )

    fun setSelectedInstanceId(id: Long?) {
        _selectedInstanceId.value = id
    }

    fun setInitialInstanceId(id: Long?) {
        initialInstanceId = id
    }

    fun selectInstance(
        instanceId: Long,
        currentUiState: UnifiedMediaDetailsUiState,
        onUiStateUpdated: (UnifiedMediaDetailsUiState) -> Unit,
        onIsMonitoredUpdated: (Boolean) -> Unit,
    ) {
        if (_selectedInstanceId.value == instanceId) return
        _selectedInstanceId.value = instanceId

        val currentSuccess = currentUiState as? UnifiedMediaDetailsUiState.Success
        if (currentSuccess != null) {
            val targetMedia =
                _instancePresencesMap.value[instanceId]
                    ?: currentSuccess.instancePresences.firstOrNull { it.instance.id == instanceId }?.arrMedia
            val hasTargetArrId = targetMedia?.let { it.id != null && it.id != 0L } ?: false
            onIsMonitoredUpdated(targetMedia?.monitored == true)
            onUiStateUpdated(
                currentSuccess.copy(
                    arrMedia = targetMedia,
                    selectedInstanceId = instanceId,
                    queueItems = if (targetMedia == null) emptyList() else currentSuccess.queueItems,
                    seerrMedia = if (!currentSuccess.combineSeerrArrMedia && hasTargetArrId) null else currentSuccess.seerrMedia,
                ),
            )
            val targetInst =
                availableInstances.value.firstOrNull { it.id == instanceId }
                    ?: currentSuccess.availableInstances.firstOrNull { it.id == instanceId }
                    ?: getArrInstanceRepositoryUseCase(instanceId)?.instance
            if (targetMedia == null && targetInst != null) {
                setAddSheetTargetInstance(targetInst)
            }
        }
        scope.launch {
            activityQueueService.manualRefresh()
        }
    }

    fun setAddSheetTargetInstance(instance: Instance?) {
        _addSheetUiState.update { it.copy(targetInstance = instance) }
        if (instance != null) {
            val repo = getArrInstanceRepositoryUseCase(instance.id)
            if (repo != null) {
                _addSheetUiState.update {
                    it.copy(
                        qualityProfiles = repo.qualityProfiles.value,
                        rootFolders = repo.rootFolders.value,
                        tags = repo.tags.value,
                    )
                }
                scope.launch {
                    repo.refreshQualityProfiles()
                    _addSheetUiState.update { it.copy(qualityProfiles = repo.qualityProfiles.value) }
                }
                scope.launch {
                    repo.refreshRootFolders()
                    _addSheetUiState.update { it.copy(rootFolders = repo.rootFolders.value) }
                }
                scope.launch {
                    repo.refreshTags()
                    _addSheetUiState.update { it.copy(tags = repo.tags.value) }
                }
            }
        }
    }

    fun updatePreferences(preferences: InstancePreferences) {
        scope.launch {
            val targetInstanceId =
                _addSheetUiState.value.targetInstance?.id
                    ?: activeInstance.value?.id
            if (targetInstanceId != null) {
                updateInstancePreferencesUseCase(targetInstanceId, preferences)
            }
        }
    }

    suspend fun getActiveArrRepository(): ArrInstanceRepository? {
        val selectedId = _selectedInstanceId.value
        if (selectedId != null) {
            getArrInstanceRepositoryUseCase(selectedId)?.let { return it }
        }
        return activeArrRepoFlow.firstOrNull()
    }

    suspend fun getSeerrRepository(): SeerrInstanceRepository? = seerrRepositoryFlow.firstOrNull()

    fun getEffectiveArrMedia(uiState: UnifiedMediaDetailsUiState): ArrMedia? {
        val selectedId = _selectedInstanceId.value
        if (selectedId != null) {
            _instancePresencesMap.value[selectedId]?.let { return it }
        }
        val stateMedia = (uiState as? UnifiedMediaDetailsUiState.Success)?.arrMedia
        if (stateMedia != null) return stateMedia
        val activeId = activeInstance.value?.id ?: initialInstanceId
        if (activeId != null) {
            _instancePresencesMap.value[activeId]?.let { return it }
        }
        return null
    }

    fun getEffectiveArrId(uiState: UnifiedMediaDetailsUiState): Long? {
        val media = getEffectiveArrMedia(uiState)
        val mediaId = media?.id?.takeIf { it != 0L }
        if (mediaId != null) return mediaId

        val activeId = _selectedInstanceId.value ?: activeInstance.value?.id ?: initialInstanceId
        return if (activeId == initialInstanceId) arrId else null
    }
}
