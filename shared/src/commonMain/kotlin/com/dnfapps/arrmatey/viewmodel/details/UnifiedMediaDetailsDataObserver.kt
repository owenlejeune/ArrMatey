package com.dnfapps.arrmatey.viewmodel.details

import com.dnfapps.arrmatey.arr.api.model.ArrMedia
import com.dnfapps.arrmatey.arr.api.model.ArrMovie
import com.dnfapps.arrmatey.arr.api.model.ArrSeries
import com.dnfapps.arrmatey.arr.usecase.GetInstancePresencesUseCase
import com.dnfapps.arrmatey.arr.usecase.GetUnifiedMediaDetailsUseCase
import com.dnfapps.arrmatey.datastore.PreferencesStore
import com.dnfapps.arrmatey.instances.model.Instance
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.instances.repository.ArrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.BazarrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SeerrInstanceRepository
import com.dnfapps.arrmatey.instances.repository.SonarrRepository
import com.dnfapps.arrmatey.instances.usecase.GetTracearrInstanceRepositoryUseCase
import com.dnfapps.arrmatey.model.UnifiedMediaDetailsUiState
import com.dnfapps.arrmatey.seerr.api.model.MovieDetails
import com.dnfapps.arrmatey.seerr.api.model.RequestType
import com.dnfapps.arrmatey.seerr.api.model.TvDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UnifiedMediaDetailsDataObserver(
    private val scope: CoroutineScope,
    private val arrId: Long?,
    private val tmdbId: Long?,
    private val tvdbId: Long?,
    private val resolvedInstanceType: InstanceType?,
    private val resolvedRequestType: RequestType?,
    private val getUnifiedMediaDetailsUseCase: GetUnifiedMediaDetailsUseCase,
    private val getInstancePresencesUseCase: GetInstancePresencesUseCase,
    private val preferencesStore: PreferencesStore,
    private val instanceHandler: UnifiedMediaDetailsInstanceHandler,
    private val arrActionsHandler: UnifiedMediaDetailsArrActionsHandler,
    private val seerrServiceHandler: UnifiedMediaDetailsSeerrServiceHandler,
    private val tracearrHandler: UnifiedMediaDetailsTracearrHandler,
    private val getTracearrInstanceRepositoryUseCase: GetTracearrInstanceRepositoryUseCase,
    private val onIsMonitoredUpdated: (Boolean) -> Unit,
) {
    private val dataFlow: Flow<
        Quint<
            ArrInstanceRepository?,
            List<ArrInstanceRepository>,
            SeerrInstanceRepository?,
            BazarrInstanceRepository?,
            Map<Long, ArrMedia?>,
            >,
        > =
        combine(
            instanceHandler.activeArrRepoFlow,
            instanceHandler.allArrReposFlow,
            instanceHandler.seerrRepositoryFlow,
            instanceHandler.bazarrRepositoryFlow,
            instanceHandler.instancePresencesMap,
        ) { activeRepo, allRepos, seerrRepo, bazarrRepo, presencesMap ->
            Quint(activeRepo, allRepos, seerrRepo, bazarrRepo, presencesMap)
        }.distinctUntilChanged { old, new ->
            // The body writes back into these flows, so compare the resolved id rather than raw presences.
            old.first === new.first &&
                old.second == new.second &&
                old.third === new.third &&
                old.fourth === new.fourth &&
                targetArrIdFor(old.first, old.fifth) == targetArrIdFor(new.first, new.fifth)
        }

    fun observeData(uiStateFlow: MutableStateFlow<UnifiedMediaDetailsUiState>) {
        scope.launch {
            instanceHandler.instancePresencesMap.collect { map ->
                val current = uiStateFlow.value as? UnifiedMediaDetailsUiState.Success ?: return@collect
                val updatedPresences =
                    getInstancePresencesUseCase.buildPresencesListFromInstances(
                        instances = current.availableInstances,
                        activeRepoId = current.selectedInstanceId,
                        activeArrMedia = current.arrMedia,
                        presencesMap = map,
                    )
                val effectiveArrMedia =
                    map[current.selectedInstanceId]?.takeIf { it.id != null && it.id != 0L }
                        ?: current.arrMedia?.takeIf { it.id != null && it.id != 0L }
                        ?: map.values.firstOrNull { it != null && it.id != null && it.id != 0L }
                uiStateFlow.value =
                    current.copy(
                        arrMedia = effectiveArrMedia,
                        instancePresences = updatedPresences,
                    )

                val filteredInstances =
                    current.availableInstances.filter { instance ->
                        val arrMedia = map[instance.id]
                        val isPresent = arrMedia?.let { it.id != null && it.id != 0L } ?: false
                        !isPresent
                    }
                val selectedId = current.selectedInstanceId ?: instanceHandler.selectedInstanceId.value
                val activeInst = current.availableInstances.find { it.id == selectedId }
                syncAddSheetTarget(
                    filteredInstances = filteredInstances,
                    activeInstance = activeInst,
                )
            }
        }

        seerrServiceHandler.observeSeerrRepo(
            seerrRepoFlow = instanceHandler.seerrRepositoryFlow,
            uiStateFlow = uiStateFlow,
        )

        tracearrHandler.observeTracearrData(
            scope = scope,
            uiStateFlow = uiStateFlow,
            getTracearrInstanceRepositoryUseCase = getTracearrInstanceRepositoryUseCase,
            preferencesStore = preferencesStore,
            initialTmdbId = tmdbId,
            initialRequestType = resolvedRequestType,
        )

        scope.launch {
            dataFlow.collectLatest { (activeRepo, allRepos, seerrRepo, bazarrRepo, map) ->
                val instances = allRepos.map { it.instance }
                val filteredInstances =
                    instances.filter { instance ->
                        val arrMedia = map[instance.id]
                        val isPresent = arrMedia?.let { it.id != null && it.id != 0L } ?: false
                        !isPresent
                    }
                val selectedId = instanceHandler.selectedInstanceId.value
                val activeInst = allRepos.find { it.instance.id == selectedId }?.instance ?: activeRepo?.instance
                syncAddSheetTarget(
                    filteredInstances = filteredInstances,
                    activeInstance = activeInst,
                )

                if (activeRepo != null) {
                    if (instanceHandler.selectedInstanceId.value == null) {
                        instanceHandler.setSelectedInstanceId(activeRepo.instance.id)
                        instanceHandler.setInitialInstanceId(activeRepo.instance.id)
                    }
                    activeRepo.resetEditItemStatus()
                    arrActionsHandler.resetEditStatus()
                    launch {
                        activeRepo.qualityProfiles.collect { instanceHandler.updateQualityProfiles(it) }
                    }
                    launch {
                        activeRepo.metadataProfiles.collect { instanceHandler.updateMetadataProfiles(it) }
                    }
                    launch {
                        activeRepo.rootFolders.collect { instanceHandler.updateRootFolders(it) }
                    }
                    launch {
                        activeRepo.tags.collect { instanceHandler.updateTags(it) }
                    }
                    launch {
                        activeRepo.addItemStatus.collect { arrActionsHandler.updateAddItemStatus(it) }
                    }
                    launch {
                        activeRepo.editItemStatus.collect { arrActionsHandler.updateEditStatus(it) }
                    }
                }

                val targetArrId = targetArrIdFor(activeRepo, map)

                val combineMedia = preferencesStore.combineSeerrArrMedia.first()
                val showBazarr = preferencesStore.bazarrDetailsIntegration.first()

                val effectiveSeerrRepo = if (!combineMedia && targetArrId != null) null else seerrRepo
                val effectiveArrRepo = if (!combineMedia && targetArrId == null && seerrRepo != null) null else activeRepo

                getUnifiedMediaDetailsUseCase(
                    arrId = targetArrId,
                    tmdbId = tmdbId,
                    tvdbId = tvdbId,
                    instanceType = resolvedInstanceType,
                    requestType = resolvedRequestType,
                    arrRepository = effectiveArrRepo,
                    seerrRepository = effectiveSeerrRepo,
                    bazarrRepository = if (showBazarr) bazarrRepo else null,
                ).collect { rawState ->
                    if (rawState is UnifiedMediaDetailsUiState.Success) {
                        val effectiveArrId = rawState.arrMedia?.id
                        if (effectiveArrId != null && effectiveArrId != 0L && activeRepo != null) {
                            launch {
                                if (activeRepo is SonarrRepository) {
                                    activeRepo.getSeriesHistory(effectiveArrId)
                                } else {
                                    activeRepo.getItemHistory(effectiveArrId)
                                }
                            }
                            launch {
                                activeRepo.observeItemHistory(effectiveArrId).collect { history ->
                                    val current = uiStateFlow.value as? UnifiedMediaDetailsUiState.Success ?: return@collect
                                    if (current.history != history) {
                                        uiStateFlow.value = current.copy(history = history)
                                    }
                                }
                            }
                        }

                        onIsMonitoredUpdated(rawState.arrMedia?.monitored ?: false)

                        val resolvedTvdbLookupId =
                            tvdbId
                                ?: (rawState.arrMedia as? ArrSeries)?.tvdbId?.takeIf { it > 0 }
                                ?: (rawState.seerrMedia as? TvDetails)?.externalIds?.tvdbId?.takeIf { it > 0 }

                        val resolvedLookupId =
                            tmdbId
                                ?: (rawState.arrMedia as? ArrMovie)?.tmdbId?.takeIf { it > 0 }
                                ?: (rawState.arrMedia as? ArrSeries)?.tmdbId?.takeIf { it > 0 }
                                ?: (rawState.seerrMedia as? MovieDetails)?.id
                                ?: (rawState.seerrMedia as? TvDetails)?.id

                        val query =
                            if (resolvedInstanceType == InstanceType.Sonarr || resolvedRequestType == RequestType.Tv) {
                                resolvedTvdbLookupId?.let { "tvdb:$it" } ?: resolvedLookupId?.let { "tmdb:$it" }
                            } else {
                                resolvedLookupId?.let { "tmdb:$it" } ?: resolvedTvdbLookupId?.let { "tvdb:$it" }
                            }

                        if (activeRepo != null && rawState.hasArrId && rawState.arrMedia != null) {
                            if (instanceHandler.instancePresencesMap.value[activeRepo.instance.id] != rawState.arrMedia) {
                                val updated = instanceHandler.instancePresencesMap.value.toMutableMap()
                                updated[activeRepo.instance.id] = rawState.arrMedia
                                instanceHandler.updatePresencesMap(updated)
                            }
                        }

                        if (allRepos.isNotEmpty() && query != null) {
                            val missingRepos =
                                allRepos.filter { repo ->
                                    instanceHandler.instancePresencesMap.value[repo.instance.id] == null
                                }
                            if (missingRepos.isNotEmpty()) {
                                launch {
                                    val updatedPresences =
                                        getInstancePresencesUseCase.fetchMissingPresences(
                                            repositories = missingRepos,
                                            query = query,
                                            resolvedTvdbLookupId = resolvedTvdbLookupId,
                                            resolvedLookupId = resolvedLookupId,
                                            existingPresences = instanceHandler.instancePresencesMap.value,
                                        )
                                    instanceHandler.updatePresencesMap(updatedPresences)
                                }
                            }
                        }

                        val presences =
                            getInstancePresencesUseCase.buildPresencesList(
                                repositories = allRepos,
                                activeRepoId = activeRepo?.instance?.id,
                                activeArrMedia = rawState.arrMedia,
                                presencesMap = instanceHandler.instancePresencesMap.value,
                            )

                        val effectiveArrMedia =
                            rawState.arrMedia?.takeIf { it.id != null && it.id != 0L }
                                ?: activeRepo?.instance?.id?.let { map[it] }?.takeIf { it.id != null && it.id != 0L }
                                ?: instanceHandler.selectedInstanceId.value?.let { map[it] }?.takeIf { it.id != null && it.id != 0L }
                                ?: map.values.firstOrNull { it != null && it.id != null && it.id != 0L }

                        uiStateFlow.value =
                            rawState.copy(
                                arrMedia = effectiveArrMedia,
                                availableInstances = if (!combineMedia && !rawState.hasArrId) emptyList() else allRepos.map { it.instance },
                                selectedInstanceId = activeRepo?.instance?.id ?: instanceHandler.selectedInstanceId.value,
                                instancePresences = if (!combineMedia && !rawState.hasArrId) emptyList() else presences,
                                combineSeerrArrMedia = combineMedia,
                                bazarrDetailsIntegration = showBazarr,
                            )
                    } else {
                        if (uiStateFlow.value !is UnifiedMediaDetailsUiState.Success) {
                            uiStateFlow.value = rawState
                        }
                    }
                }
            }
        }
    }

    private fun targetArrIdFor(
        activeRepo: ArrInstanceRepository?,
        presences: Map<Long, ArrMedia?>,
    ): Long? {
        val cachedArrMedia = activeRepo?.let { presences[it.instance.id] }
        val cachedId = cachedArrMedia?.id?.takeIf { it != 0L }
        val initialArrId = arrId?.takeIf { it != 0L }
        return if (activeRepo?.instance?.id == instanceHandler.initialInstanceId) {
            cachedId ?: initialArrId
        } else {
            cachedId
        }
    }

    private fun syncAddSheetTarget(
        filteredInstances: List<Instance>,
        activeInstance: Instance?,
    ) {
        instanceHandler.updateAddSheetUiState { it.copy(availableInstances = filteredInstances) }

        val currentTarget = instanceHandler.addSheetUiState.value.targetInstance
        val newTarget =
            if (currentTarget != null && filteredInstances.any { it.id == currentTarget.id }) {
                currentTarget
            } else if (activeInstance != null && filteredInstances.any { it.id == activeInstance.id }) {
                activeInstance
            } else {
                filteredInstances.firstOrNull()
            }
        if (newTarget?.id != currentTarget?.id ||
            instanceHandler.addSheetUiState.value.qualityProfiles
                .isEmpty()
        ) {
            instanceHandler.setAddSheetTargetInstance(newTarget)
        }
    }
}

private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
)
